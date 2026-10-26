import { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { CognitoUserPool, CognitoUser, AuthenticationDetails } from 'amazon-cognito-identity-js';
import api from '../api';
import './Auth.css';

// 1. Configuramos el acceso a tu Cognito en AWS
const poolData = {
    UserPoolId: 'us-east-1_uyCELHdgp',
    ClientId: '91nln9bvj99r9oa5s6tc8ppud'
};
const userPool = new CognitoUserPool(poolData);

export default function Login() {
    // 2. Cambiamos RUT por email para coincidir con AWS Cognito
    const [gmail, setGmail] = useState(''); 
    const [password, setPassword] = useState('');
    const [error, setError] = useState('');
    const navigate = useNavigate();

    const handleLogin = (e) => {
        e.preventDefault();

        // 3. Preparamos las credenciales para AWS
        const authenticationDetails = new AuthenticationDetails({
            Username: gmail,
            Password: password,
        });

        const cognitoUser = new CognitoUser({
            Username: gmail,
            Pool: userPool
        });

        // 4. Autenticamos directamente contra la nube
        cognitoUser.authenticateUser(authenticationDetails, {
            onSuccess: async (result) => {
                try {
                    // 1. Obtenemos el JWT oficial emitido por AWS (solo se usa para esta
                    // primera búsqueda; NO es el token que usaremos para contactos/transferencias)
                    const cognitoToken = result.getIdToken().getJwtToken();

                    // 2. Buscamos el perfil real en MySQL usando el endpoint del BFF.
                    // OJO: el campo correcto es "idUsuario", no "id" (así lo devuelve
                    // la entidad Usuario.java de back-sesion).
                    const respuesta = await api.get(`/usuarios/buscar?gmail=${gmail}`, {
                        headers: { Authorization: cognitoToken }
                    });

                    const usuarioReal = respuesta.data;

                    // 3. Intercambiamos el token de Cognito por un token INTERNO,
                    // firmado por back-sesion (JwtService, HS256, subject = RUT).
                    // Es el único token que back_contacto y back-trans-service
                    // saben validar (su JwtAuthenticationFilter usa la clave local,
                    // no las claves públicas de Cognito). Sin este paso, cualquier
                    // llamada a /contactos o /transferencias fallará silenciosamente.
                    // Esta llamada pasa por la ruta ANY /{proxy+} del Gateway,
                    // que SÍ exige el autorizador de Cognito -> hay que mandar
                    // el token de Cognito aquí explícitamente.
                    const sesionInterna = await api.post('/session',
                        { rut: usuarioReal.rut },
                        { headers: { Authorization: `Bearer ${cognitoToken}` } }
                    );

                    const { token: tokenInterno } = sesionInterna.data;

                    localStorage.setItem('token', tokenInterno);
                    localStorage.setItem('idUsuario', usuarioReal.idUsuario);
                    navigate(`/dashboard/${usuarioReal.idUsuario}`);

                } catch (err) {
                    console.error("Error al iniciar sesión interna:", err);
                    setError("Cognito te aceptó, pero falló la conexión con tu base de datos.");
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