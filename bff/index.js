require('dotenv').config();

const express = require('express');
const cors = require('cors');
const bffRoutes = require('./src/routes/bff.routes');

const app = express();
const PORT = process.env.PORT || 3000;

app.use(cors()); 
app.use(express.json()); 

app.use('/api/bff', bffRoutes);

app.listen(PORT, () => {
    console.log(`BFF Operativo en el puerto ${PORT} `);
});

