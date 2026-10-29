import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { CognitoUserPool, CognitoUserAttribute } from 'amazon-cognito-identity-js';
import api from '../api';
import { COGNITO_POOL_ID, COGNITO_CLIENT_ID } from '../config';
import { handleApiError } from '../utils/errorHandler';
import './Auth.css';

const poolData = {
    UserPoolId: COGNITO_POOL_ID,
    ClientId: COGNITO_CLIENT_ID
};
const userPool = new CognitoUserPool(poolData);

export default function Register() {
    const [formData, setFormData] = useState({ nombre: '', rut: '', gmail: '', password: '' });
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleChange = (e) => {
        setFormData({ ...formData, [e.target.name]: e.target.value });
    };

    const handleRegister = (e) => {
        e.preventDefault();
        setError('');

        const attributeList = [
            new CognitoUserAttribute({ Name: 'email', Value: formData.gmail })
        ];

        userPool.signUp(formData.gmail, formData.password, attributeList, null, async (err) => {
            if (err) {
                console.error(err);
                setError('Error en Cognito: ' + (err.message || 'Error desconocido'));
                return;
            }

            try {
                await api.post('/register', {
                    nombre: formData.nombre,
                    rut: formData.rut,
                    gmail: formData.gmail
                });
                alert('Registro exitoso en la nube. Por favor, inicia sesión.');
                navigate('/');
            } catch (backendErr) {
                console.error(backendErr);
                setError(handleApiError(backendErr));
            }
        });
    };

    return (
        <div className="auth-page">
            <div className="auth-card">
                <div className="auth-logo">
                    <span className="logo-icon">☁️</span>
                    <h1>Banco Cloud</h1>
                </div>
                <p className="auth-subtitle">Crea tu cuenta segura</p>

                {error && <p className="auth-error">{error}</p>}

                <form onSubmit={handleRegister} className="auth-form">
                    <label className="auth-label">
                        Nombre completo
                        <input type="text" name="nombre" placeholder="Tu nombre" onChange={handleChange} required />
                    </label>
                    <label className="auth-label">
                        RUT
                        <input type="text" name="rut" placeholder="12345678-9" onChange={handleChange} required />
                    </label>
                    <label className="auth-label">
                        Correo electrónico
                        <input type="email" name="gmail" placeholder="tucorreo@gmail.com" onChange={handleChange} required />
                    </label>
                    <label className="auth-label">
                        Contraseña
                        <input type="password" name="password" placeholder="••••••••" onChange={handleChange} required />
                    </label>
                    <button type="submit" className="auth-btn-primary">
                        Registrarme
                    </button>
                </form>

                <p className="auth-footer">
                    ¿Ya tienes cuenta? <Link to="/">Inicia sesión aquí</Link>
                </p>
            </div>
        </div>
    );
}
