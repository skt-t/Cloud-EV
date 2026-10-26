import axios from 'axios';

const api = axios.create({
    // Apuntamos al Gateway en la nube. Mantengo /api/bff asumiendo que tu Node usa ese prefijo.
    baseURL: 'https://414l26p1ab.execute-api.us-east-1.amazonaws.com/api/bff',
});

api.interceptors.request.use((config) => {
    const token = localStorage.getItem('token');
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

export default api;