import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api'; // Reemplazamos axios por api
import './transferir.css';

const Transferir = () => {
    const { userId } = useParams();
    const navigate = useNavigate();

    const [step, setStep] = useState('contacto');
    const [usuario, setUsuario] = useState(null);
    const [contactos, setContactos] = useState([]);
    const [contactoSeleccionado, setContactoSeleccionado] = useState(null);
    const [monto, setMonto] = useState('');
    const [loading, setLoading] = useState(true);
    const [enviando, setEnviando] = useState(false);
    const [error, setError] = useState('');

    useEffect(() => {
        api.get(`/dashboard/${userId}`)
        .then(res => {
            setUsuario(res.data.usuario);
            setContactos(res.data.detalles.contactos || []);
            setLoading(false);
        })
        .catch(err => {
            console.error("Error al cargar los contactos:", err);
            setError('No se pudieron cargar tus contactos.');
            setLoading(false);
        });
    }, [userId, navigate]);

    const elegirContacto = (contacto) => {
        setContactoSeleccionado(contacto);
        setError('');
        setStep('monto');
    };

    const volverAContactos = () => {
        setStep('contacto');
        setMonto('');
        setError('');
    };

    const confirmarTransferencia = async (e) => {
        e.preventDefault();
        setError('');

        const montoNumerico = Number(monto);
        if (!montoNumerico || montoNumerico <= 0) {
            setError('Ingresa un monto válido.');
            return;
        }

        setEnviando(true);

        try {
            await api.post('/transferencias', {
                idUsuario: Number(userId),
                rutOrigen: usuario?.rut,
                idContacto: contactoSeleccionado.idContacto ?? contactoSeleccionado.id,
                rutDestino: contactoSeleccionado.rut,
                monto: montoNumerico
            });

            navigate(`/dashboard/${userId}`);
        } catch (err) {
            console.error("Error al realizar la transferencia:", err);
            setError('No se pudo realizar la transferencia. Revisa los datos e intenta de nuevo.');
        } finally {
            setEnviando(false);
        }
    };

    if (loading) {
        return <div className="loader">Cargando...</div>;
    }

    return (
        <div className="transferir-container">
            <div className="transferir-card">
                <div className="transferir-header">
                    <button className="btn-back" onClick={() => step === 'monto' ? volverAContactos() : navigate(`/dashboard/${userId}`)}>
                        ← Volver
                    </button>
                    <h1>Nueva Transferencia</h1>
                </div>

                {error && <p className="transferir-error">{error}</p>}

                {step === 'contacto' && (
                    <>
                        <p className="transferir-subtitle">Elige a quién le vas a transferir</p>
                        {contactos.length > 0 ? (
                            <ul className="contacto-list">
                                {contactos.map((c, i) => (
                                    <li key={c.idContacto ?? c.id ?? i}>
                                        <button className="contacto-item" onClick={() => elegirContacto(c)}>
                                            <span className="contacto-avatar">{(c.nombreContacto || '?').charAt(0).toUpperCase()}</span>
                                            <span className="contacto-info">
                                                <span className="contacto-nombre">{c.nombreContacto}</span>
                                                <span className="contacto-rut">{c.rut}</span>
                                            </span>
                                            <span className="contacto-chevron">›</span>
                                        </button>
                                    </li>
                                ))}
                            </ul>
                        ) : (
                            <p className="empty-message">No tienes contactos guardados todavía.</p>
                        )}
                    </>
                )}

                {step === 'monto' && contactoSeleccionado && (
                    <>
                        <p className="transferir-subtitle">Transferir a</p>
                        <div className="contacto-resumen">
                            <span className="contacto-avatar">{(contactoSeleccionado.nombreContacto || '?').charAt(0).toUpperCase()}</span>
                            <span className="contacto-info">
                                <span className="contacto-nombre">{contactoSeleccionado.nombreContacto}</span>
                                <span className="contacto-rut">{contactoSeleccionado.rut}</span>
                            </span>
                        </div>

                        <form onSubmit={confirmarTransferencia} className="monto-form">
                            <label className="monto-label">
                                Monto a transferir
                                <div className="monto-input-wrapper">
                                    <span className="monto-prefix">$</span>
                                    <input
                                        type="number"
                                        min="1"
                                        step="1"
                                        placeholder="0"
                                        value={monto}
                                        onChange={(e) => setMonto(e.target.value)}
                                        autoFocus
                                        required
                                    />
                                </div>
                            </label>
                            <button type="submit" className="btn-primary btn-full" disabled={enviando}>
                                {enviando ? 'Enviando...' : 'Confirmar transferencia'}
                            </button>
                        </form>
                    </>
                )}
            </div>
        </div>
    );
};

export default Transferir;