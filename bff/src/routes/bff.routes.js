const express = require('express');
const router = express.Router();
const bffController = require('../controllers/bff.controller');

// Health check endpoint
router.get('/health', (req, res) => res.json({ status: 'ok' }));

// Registro (público)
router.post('/register', bffController.register);

// Usuarios
router.get('/usuarios/buscar', bffController.buscarUsuario);
router.get('/usuarios/:id', bffController.obtenerUsuario);

// Contactos
router.post('/contactos', bffController.crearContacto);
router.get('/contactos/usuario/:idUsuario', bffController.obtenerContactosUsuario);
router.get('/contactos/:id', bffController.obtenerContactoPorId);

// Transferencias
router.post('/transferencias', bffController.hacerTransferencia);
router.get('/transferencias/usuario/:idUsuario', bffController.obtenerTransferenciasUsuario);

// Admin queues
router.get('/admin/queues', bffController.obtenerAdminQueues);

// Dashboard
router.get('/dashboard/:userId', bffController.getDashboardData);

module.exports = router;
