const microservices = require('../services/microservices.service');

const getDashboardData = async (req, res) => {
    const { userId } = req.params;

    try {
        const [usuario, contactos, transferencias] = await Promise.all([
            microservices.obtenerUsuario(userId),
            microservices.obtenerContactos(userId),
            microservices.obtenerTransferencias(userId)
        ]);

        if (!usuario) {
            return res.status(404).json({ error: 'Usuario no encontrado o servicio de usuarios caído' });
        }

        const responseBFF = {
            usuario: {
                id: usuario.id,
                nombre: usuario.nombre,
            },
            metricas: {
                totalContactos: contactos.length,
                totalTransferencias: transferencias.length
            },
            detalles: {
                contactos: contactos,
                ultimasTransferencias: transferencias.slice(0, 5) 
            }
        };

        return res.json(responseBFF);

    } catch (error) {
        console.error('Error crítico en el controlador del BFF:', error);
        return res.status(500).json({ error: 'Error procesando la solicitud en el BFF' });
    }
};

module.exports = {
    getDashboardData
};