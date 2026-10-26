import { useEffect, useState } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import api from '../api'; // Reemplazamos axios por tu instancia configurada
import './dashboard.css';

const Dashboard = () => {
    const { userId } = useParams();
    const navigate = useNavigate();
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        // api.js ya se encarga de inyectar el token y usar la URL de AWS
        api.get(`/dashboard/${userId}`)
        .then(res => {
            setDashboardData(res.data);
            setLoading(false);
        })
        .catch(err => {
            console.error("Error al cargar el dashboard:", err);
            localStorage.removeItem('token');
            navigate('/');
        });
    }, [userId, navigate]);

    if (loading) {
        return <div className="loader">Cargando...</div>;
    }

    const { usuario, metricas, detalles } = dashboardData;

    return (
        <div className="dashboard-container">
            <header className="dashboard-header">
                <div className="header-logo">
                    <span className="logo-icon">☁️</span>
                    <h1>Banco Cloud</h1>
                </div>
                <div className="header-user">
                    <span>Hola, <strong>{usuario.nombre}</strong></span>
                    <button className="btn-primary" onClick={() => navigate(`/transferir/${userId}`)}>
                        + Nueva Transferencia
                    </button>
                    <button className="btn-logout" onClick={() => { localStorage.clear(); navigate('/'); }}>
                        Cerrar Sesión
                    </button>
                </div>
            </header>

            <div className="metrics-grid">
                <div className="metric-card transfer-card">
                    <h3>Total Transferencias</h3>
                    <p>{metricas.totalTransferencias}</p>
                </div>
                <div className="metric-card contact-card">
                    <h3>Contactos Guardados</h3>
                    <p>{metricas.totalContactos}</p>
                </div>
            </div>

            <div className="details-grid">
                <div className="detail-card">
                    <h2>Últimas Transferencias</h2>
                    {detalles.ultimasTransferencias.length > 0 ? (
                        <ul className="detail-list">
                            {detalles.ultimasTransferencias.map((t, i) => (
                                <li key={i} className="list-item">
                                    <span className="item-name">Para: {t.destinatario || 'Desconocido'}</span>
                                    <span className="item-amount">-${t.monto}</span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="empty-message">Aún no hay transferencias registradas.</p>
                    )}
                </div>

                <div className="detail-card">
                    <h2>
                        <span> Mis Contactos</span>
                        <button className="btn-add-contacto" onClick={() => navigate(`/agregar-contacto/${userId}`)}>
                            + Agregar
                        </button>
                    </h2>
                    {detalles.contactos.length > 0 ? (
                        <ul className="detail-list">
                            {detalles.contactos.map((c, i) => (
                                <li key={i} className="list-item flex-col">
                                    <span className="item-bold">{c.nombreContacto}</span>
                                    <span className="item-sub">{c.rut}</span>
                                </li>
                            ))}
                        </ul>
                    ) : (
                        <p className="empty-message">No tienes contactos agregados todavía.</p>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Dashboard;