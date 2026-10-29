import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { CognitoUserPool, CognitoUser, AuthenticationDetails } from 'amazon-cognito-identity-js';
import api from '../api';
import { COGNITO_POOL_ID, COGNITO_CLIENT_ID } from '../config';
import { handleApiError } from '../utils/errorHandler';
import './Auth.css';

const poolData = {
    UserPoolId: COGNITO_POOL_ID,
    ClientId: COGNITO_CLIENT_ID
};
const userPool = new CognitoUserPool(poolData);

export default function Login() {
    const [gmail, setGmail] = useState(''); 
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = (e) => {
        e.preventDefault();
        setError('');

        const authenticationDetails = new AuthenticationDetails({
            Username: gmail,
            Password: password,
        });

        const cognitoUser = new CognitoUser({
            Username: gmail,
            Pool: userPool
        });

        cognitoUser.authenticateUser(authenticationDetails, {
            onSuccess: async (result) => {
                try {
                    const idToken = result.getIdToken().getJwtToken();
                    localStorage.setItem('token', idToken);

                    const respuesta = await api.get(`/usuarios/buscar?gmail=${gmail}`);
                    const usuarioReal = respuesta.data;

                    localStorage.setItem('idUsuario', usuarioReal.idUsuario);
                    navigate(`/dashboard/${usuarioReal.idUsuario}`);
                } catch (err) {
                    console.error("Error al buscar usuario tras login en Cognito:", err);
                    setError(handleApiError(err));
                }
            },
            onFailure: (err) => {
                console.error(err);
                setError('Credenciales incorrectas en AWS Cognito. Intenta de nuevo.');
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
                <p className="auth-subtitle">Ingresa a tu cuenta con AWS Cognito</p>

                {error && <p className="auth-error">{error}</p>}

                <form onSubmit={handleLogin} className="auth-form">
                    <label className="auth-label">
                        Correo Electrónico
                        <input
                            type="email"
                            placeholder="admin@ejemplo.com"
                            value={gmail}
                            onChange={(e) => setGmail(e.target.value)}
                            required
                        />
                    </label>
                    <label className="auth-label">
                        Contraseña
                        <input
                            type="password"
                            placeholder="••••••••"
                            value={password}
                            onChange={(e) => setPassword(e.target.value)}
                            required
                        />
                    </label>
                    <button type="submit" className="auth-btn-primary">
                        Entrar a mi cuenta
                    </button>
                </form>

                <p className="auth-footer">
                    ¿No tienes cuenta? <Link to="/register">Regístrate aquí</Link>
                </p>
            </div>
        </div>
    );
}
