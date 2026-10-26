import { BrowserRouter as Router, Routes, Route } from 'react-router-dom';
import Login from './pages/login';
import Register from './pages/register';
import Dashboard from './pages/dashboard';
import Transferir from './pages/transferir';
import AgregarContacto from './pages/agregarcontacto';


function App() {
  return (
    <Router>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/register" element={<Register />} />
        <Route path="/dashboard/:userId" element={<Dashboard />} />
        <Route path="/transferir/:userId" element={<Transferir />} /> 
        <Route path="/agregar-contacto/:userId" element={<AgregarContacto />} />
      </Routes>
    </Router>
  );
}

export default App;