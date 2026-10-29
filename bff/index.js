require('dotenv').config();

const express = require('express');
const cors = require('cors');
const bffRoutes = require('./src/routes/bff.routes');

const app = express();
const PORT = process.env.PORT || 3000;
const CORS_ORIGIN = process.env.CORS_ORIGIN || '*';

const corsOptions = {
    origin: CORS_ORIGIN,
    methods: ['GET', 'POST', 'PUT', 'DELETE', 'OPTIONS'],
    allowedHeaders: ['Authorization', 'Content-Type'],
    optionsSuccessStatus: 204
};

app.use(cors(corsOptions));
app.use(express.json());

// Log simple por petición: método, ruta y código de respuesta
app.use((req, res, next) => {
    res.on('finish', () => {
        console.log(`${req.method} ${req.originalUrl} -> ${res.statusCode}`);
    });
    next();
});

// Health check global
app.get('/health', (req, res) => {
    res.json({ status: 'ok' });
});

app.use('/api/bff', bffRoutes);

app.listen(PORT, () => {
    console.log(`BFF Operativo en el puerto ${PORT}`);
});
