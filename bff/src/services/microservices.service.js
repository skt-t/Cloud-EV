const axios = require('axios');

const MS_USUARIOS_URL = process.env.MS_USUARIOS_URL || 'http://localhost:8081';
const MS_CONTACTOS_URL = process.env.MS_CONTACTOS_URL || 'http://localhost:8083';
const MS_TRANSFERENCIAS_URL = process.env.MS_TRANSFERENCIAS_URL || 'http://localhost:8082';
const MS_ADMIN_URL = process.env.MS_ADMIN_URL || 'http://localhost:8084';

const llamarMicroservicio = async (baseURL, method, endpoint, data = null, token = null, params = null) => {
    const config = {
        method,
        url: `${baseURL}${endpoint}`,
        headers: {}
    };
    if (token) {
        config.headers['Authorization'] = token;
    }
    if (data) {
        config.data = data;
    }
    if (params) {
        config.params = params;
    }

    try {
        const respuesta = await axios(config);
        return respuesta.data;
    } catch (error) {
        if (!error.response) {
            const err = new Error('SERVICIO_NO_DISPONIBLE');
            err.status = 502;
            err.data = { error: 'SERVICIO_NO_DISPONIBLE' };
            throw err;
        }
        const err = new Error(error.response.data?.message || error.message);
        err.status = error.response.status;
        err.data = error.response.data;
        throw err;
    }
};

const registrarUsuario = async (datosRegistro) => {
    return llamarMicroservicio(MS_USUARIOS_URL, 'POST', '/auth/register', datosRegistro);
};

const obtenerUsuario = async (userId, token) => {
    return llamarMicroservicio(MS_USUARIOS_URL, 'GET', `/api/usuarios/${userId}`, null, token);
};

const buscarUsuario = async ({ rut, gmail }, token) => {
    const params = rut ? { rut } : { gmail };
    return llamarMicroservicio(MS_USUARIOS_URL, 'GET', '/api/usuarios/buscar', null, token, params);
};

const crearContacto = async (datosContacto, token) => {
    return llamarMicroservicio(MS_CONTACTOS_URL, 'POST', '/api/contactos', datosContacto, token);
};

const obtenerContactosUsuario = async (userId, token) => {
    return llamarMicroservicio(MS_CONTACTOS_URL, 'GET', `/api/contactos/usuario/${userId}`, null, token);
};

const obtenerContactoPorId = async (id, token) => {
    return llamarMicroservicio(MS_CONTACTOS_URL, 'GET', `/api/contactos/${id}`, null, token);
};

const realizarTransferencia = async (datosTransferencia, token) => {
    return llamarMicroservicio(MS_TRANSFERENCIAS_URL, 'POST', '/api/transferencias', datosTransferencia, token);
};

const obtenerTransferenciasUsuario = async (userId, token) => {
    return llamarMicroservicio(MS_TRANSFERENCIAS_URL, 'GET', `/api/transferencias/usuario/${userId}`, null, token);
};

const obtenerAdminQueues = async (token) => {
    return llamarMicroservicio(MS_ADMIN_URL, 'GET', '/api/admin/queues', null, token);
};

module.exports = {
    registrarUsuario,
    obtenerUsuario,
    buscarUsuario,
    crearContacto,
    obtenerContactosUsuario,
    obtenerContactoPorId,
    realizarTransferencia,
    obtenerTransferenciasUsuario,
    obtenerAdminQueues
};
