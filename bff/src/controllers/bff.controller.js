const microservices = require('../services/microservices.service');

const getToken = (req) => req.headers.authorization;

const handleError = (error, res) => {
    if (error.status === 502 || error.message === 'SERVICIO_NO_DISPONIBLE') {
        return res.status(502).json({ error: 'SERVICIO_NO_DISPONIBLE' });
    }
    const status = error.status || 500;
    const data = error.data || { error: error.message };
    return res.status(status).json(data);
};

const register = async (req, res) => {
    const { nombre, rut, gmail } = req.body;
    const datos = { nombre, rut, gmail };
    
    try {
        const data = await microservices.registrarUsuario(datos);
        return res.json(data);
    } catch (error) {
        return handleError(error, res);
    }
};

const obtenerUsuario = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });
    const { id } = req.params;

    try {
        const usuario = await microservices.obtenerUsuario(id, token);
        return res.json(usuario);
    } catch (error) {
        return handleError(error, res);
    }
};

const buscarUsuario = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });
    const { rut, gmail } = req.query;

    if (!rut && !gmail) {
        return res.status(400).json({ error: 'Debes mandar un rut o un gmail' });
    }

    try {
        const usuario = await microservices.buscarUsuario({ rut, gmail }, token);
        return res.json(usuario);
    } catch (error) {
        return handleError(error, res);
    }
};

const crearContacto = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });

    try {
        const contacto = await microservices.crearContacto(req.body, token);
        return res.json(contacto);
    } catch (error) {
        return handleError(error, res);
    }
};

const obtenerContactosUsuario = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });
    const { idUsuario } = req.params;

    try {
        const contactos = await microservices.obtenerContactosUsuario(idUsuario, token);
        return res.json(contactos);
    } catch (error) {
        return handleError(error, res);
    }
};

const obtenerContactoPorId = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });
    const { id } = req.params;

    try {
        const contacto = await microservices.obtenerContactoPorId(id, token);
        return res.json(contacto);
    } catch (error) {
        return handleError(error, res);
    }
};

const hacerTransferencia = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });

    try {
        const resultado = await microservices.realizarTransferencia(req.body, token);
        return res.json(resultado);
    } catch (error) {
        return handleError(error, res);
    }
};

const obtenerTransferenciasUsuario = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });
    const { idUsuario } = req.params;

    try {
        const transferencias = await microservices.obtenerTransferenciasUsuario(idUsuario, token);
        return res.json(transferencias);
    } catch (error) {
        return handleError(error, res);
    }
};

const obtenerAdminQueues = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });

    try {
        const queues = await microservices.obtenerAdminQueues(token);
        return res.json(queues);
    } catch (error) {
        return handleError(error, res);
    }
};

const getDashboardData = async (req, res) => {
    const token = getToken(req);
    if (!token) return res.status(401).json({ error: 'Token requerido' });
    const { userId } = req.params;

    try {
        const [usuario, contactos, transferencias] = await Promise.all([
            microservices.obtenerUsuario(userId, token),
            microservices.obtenerContactosUsuario(userId, token),
            microservices.obtenerTransferenciasUsuario(userId, token)
        ]);

        if (!usuario) {
            return res.status(404).json({ error: 'Usuario no encontrado' });
        }

        const responseBFF = {
            usuario: { id: usuario.idUsuario || usuario.id, nombre: usuario.nombre, rut: usuario.rut },
            metricas: { totalContactos: contactos.length, totalTransferencias: transferencias.length },
            detalles: { contactos, ultimasTransferencias: transferencias.slice(0, 5) }
        };

        return res.json(responseBFF);
    } catch (error) {
        return handleError(error, res);
    }
};

module.exports = {
    register,
    obtenerUsuario,
    buscarUsuario,
    crearContacto,
    obtenerContactosUsuario,
    obtenerContactoPorId,
    hacerTransferencia,
    obtenerTransferenciasUsuario,
    obtenerAdminQueues,
    getDashboardData
};
