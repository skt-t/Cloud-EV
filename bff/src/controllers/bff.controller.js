const microservices = require('../services/microservices.service');

const register = async (req, res) => {
    // 1. Extraemos la contraseña real que digitó el usuario para descartarla
    const { password, ...datosUsuario } = req.body;
    
    // 2. Le inyectamos una contraseña falsa a Java para que MySQL no arroje error
    const datos = { 
        ...datosUsuario, 
        password: "MANAGED_BY_COGNITO", 
        permisos: "Usuario" 
    };
    
    try {
        const data = await microservices.registrarUsuario(datos);
        return res.json(data);
    } catch (error) {
        console.error('Error real al registrar:', error.response?.data || error.message);
 
        const mensajeJava = error.response?.data?.message || '';
 
        let mensajeAmigable = 'Fallo al registrar. Revisa los datos.';
        if (mensajeJava.toLowerCase().includes('gmail')) {
            mensajeAmigable = 'Ese correo ya está registrado.';
        } else if (mensajeJava.toLowerCase().includes('rut')) {
            mensajeAmigable = 'Ese RUT ya está registrado.';
        }
 
        const status = error.response?.status || 400;
        return res.status(status).json({ error: mensajeAmigable });
    }
};

// NUEVO: intercambia identidad (verificada ya por Cognito en el frontend)
// por un token JWT interno emitido por back-sesion, firmado con la clave
// local HS256 y con el RUT como subject. Este es el token que
// back_contacto y back-trans-service saben validar.
const loginLocal = async (req, res) => {
    const { rut } = req.body;

    if (!rut) {
        return res.status(400).json({ error: 'Falta el RUT para crear la sesión interna' });
    }

    try {
        // La contraseña "MANAGED_BY_COGNITO" es la misma que se inyectó
        // al registrar; nunca la ve ni la escribe el usuario.
        const data = await microservices.loginUsuario(rut, 'MANAGED_BY_COGNITO');
        return res.json(data); // { token, idUsuario }
    } catch (error) {
        console.error('Error al crear sesión interna:', error.response?.data || error.message);
        return res.status(401).json({ error: 'No se pudo crear la sesión interna' });
    }
};

const getDashboardData = async (req, res) => {
    const { userId } = req.params;
    const token = req.headers.authorization;

     if (!token) {
         return res.status(401).json({ error: 'Alto ahí loca, falta el token' });
     }

     try {
         const [usuario, contactos, transferencias] = await Promise.all([
             microservices.obtenerUsuario(userId, token),
             microservices.obtenerContactos(userId, token),
             microservices.obtenerTransferencias(userId, token)
         ]);

         if (!usuario) {
             return res.status(404).json({ error: 'Usuario no encontrado' });
         }

         const responseBFF = {
             usuario: { id: usuario.idUsuario, nombre: usuario.nombre, rut: usuario.rut },
             metricas: { totalContactos: contactos.length, totalTransferencias: transferencias.length },
             detalles: { contactos, ultimasTransferencias: transferencias.slice(0, 5) }
         };

         return res.json(responseBFF);
     } catch (error) {
         console.error('Error crítico en el dashboard:', error);
         return res.status(500).json({ error: 'El BFF se fue a la B' });
     }
};

const hacerTransferencia = async (req, res) => {
    const token = req.headers.authorization;
    if (!token) return res.status(401).json({ error: 'Sin token no hay plata' });

    try {
        const resultado = await microservices.realizarTransferencia(req.body, token);
        return res.json(resultado);
    } catch (error) {
        return res.status(500).json({ error: 'Falló la transferencia' });
    }
};

const buscarUsuario = async (req, res) => {
    const { rut, gmail } = req.query;
    const token = req.headers.authorization;

    if (!rut && !gmail) {
        return res.status(400).json({ error: 'Debes mandar un rut o un gmail' });
    }

    try {
        const usuario = await microservices.buscarUsuario({ rut, gmail }, token);
        return res.json(usuario);
    } catch (error) {
        console.error('Error real al buscar usuario:', {
            status: error.response?.status,
            data: error.response?.data,
            message: error.message
        });

        const status = error.response?.status || 500;
        if (status === 404) {
            return res.status(404).json({ error: 'No se encontró ningún usuario con ese dato' });
        }
        return res.status(status).json({ error: 'Error al buscar el usuario. Revisa la consola del BFF.' });
    }
};
 
const crearContacto = async (req, res) => {
    const token = req.headers.authorization;
    if (!token) return res.status(401).json({ error: 'Sin token no se puede agregar contactos' });
 
    try {
        const contacto = await microservices.crearContacto(req.body, token);
        return res.json(contacto);
    } catch (error) {
        return res.status(400).json({ error: 'No se pudo guardar el contacto' });
    }
};

module.exports = { getDashboardData, hacerTransferencia, register, buscarUsuario, crearContacto, loginLocal };
