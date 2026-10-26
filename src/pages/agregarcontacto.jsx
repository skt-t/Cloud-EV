import { useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api'; // <-- IMPORTANTE: Usamos tu instancia configurada, no axios
import './agregarcontacto.css';

const AgregarContacto = () => {
    const { userId } = useParams();
    const navigate = useNavigate();

    const [busqueda, setBusqueda] = useState('');
    const [buscando, setBuscando] = useState(false);
    const [usuarioEncontrado, setUsuarioEncontrado] = useState(null);
    const [guardando, setGuardando] = useState(false);
    const [error, setError] = useState('');

    const esGmail = busqueda.includes('@');

    const buscarUsuario = async (e) => {
        e.preventDefault();
        setError('');
        setUsuarioEncontrado(null);
        setBuscando(true);

        try {
            const params = esGmail ? { gmail: busqueda.trim() } : { rut: busqueda.trim() };
            // El token y la URL base (https://414l...) se añaden automáticamente por api.js
            const { data } = await api.get('/usuarios/buscar', { params });
            setUsuarioEncontrado(data);
        } catch (err) {
            console.error('Error al buscar usuario:', err);
            setError('No se encontró ningún usuario con ese RUT o Gmail.');
        } finally {
            setBuscando(false);
        }
    };

    const guardarContacto = async () => {
        setError('');
        setGuardando(true);

        try {
            // Nuevamente, api.js se encarga de la ruta completa y el token
            await api.post('/contactos', {
                idUsuario: Number(userId),
                nombreContacto: usuarioEncontrado.nombre,
                rut: usuarioEncontrado.rut,
                gmail: usuarioEncontrado.gmail
            });

            navigate(`/dashboard/${userId}`);
        } catch (err) {
            console.error('Error al guardar el contacto:', err);
            setError('No se pudo guardar el contacto. Intenta de nuevo.');
        } finally {
            setGuardando(false);
        }
    };

    return (
        <div className="agregar-container">
            <div className="agregar-card">
                <div className="agregar-header">
                    <button className="btn-back" onClick={() => navigate(`/dashboard/${userId}`)}>
                        ← Volver
                    </button>
                    <h1>Agregar Contacto</h1>
                </div>

                <p className="agregar-subtitle">Busca por RUT o Gmail para agregarlo a tus contactos</p>

                {error && <p className="agregar-error">{error}</p>}

                <form onSubmit={buscarUsuario} className="buscar-form">
                    <label className="buscar-label">
                        RUT o Gmail
                        <input
                            type="text"
                            placeholder="12345678-9 o correo@gmail.com"
                            value={busqueda}
                            onChange={(e) => { setBusqueda(e.target.value); setUsuarioEncontrado(null); }}
                            required
                        />
                    </label>
                    <button type="submit" className="btn-primary" disabled={buscando || !busqueda.trim()}>
                        {buscando ? 'Buscando...' : 'Buscar'}
                    </button>
                </form>

                {usuarioEncontrado && (
                    <div className="resultado-card">
                        <div className="resultado-info">
                            <span className="contacto-avatar">
                                {(usuarioEncontrado.nombre || '?').charAt(0).toUpperCase()}
                            </span>
                            <span className="contacto-datos">
                                <span className="contacto-nombre">{usuarioEncontrado.nombre}</span>
                                <span className="contacto-sub">{usuarioEncontrado.rut}</span>
                                <span className="contacto-sub">{usuarioEncontrado.gmail}</span>
                            </span>
                        </div>
                        <button className="btn-primary btn-full" onClick={guardarContacto} disabled={guardando}>
                            {guardando ? 'Guardando...' : 'Guardar contacto'}
                        </button>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AgregarContacto;