import { Route, Routes } from 'react-router-dom'
import InicioPag from './pages/client/InicioPag';
import CatalogoPag from './pages/client/CatalogoPag';
import NosotrosPag from './pages/client/NosotrosPag';
import ContactoPag from './pages/client/ContactoPag';
import Login from './pages/client/Login';
import MiPerfilPag from './pages/client/MiPerfilPag';
import Dashboard from './pages/admin/dashboard';
import GestionCatalogo from './pages/admin/GestionCatalogo';
import GestionPedidos from './pages/admin/gestionPedidos';
import GestionClientes from './pages/admin/gestionClientes';
import GestionTrabajadores from './pages/admin/gestionTrabajadores';
import NuevoUsuario from './pages/admin/nuevoUsuario';
import GestionVenta from './pages/admin/gestionVenta';
import GestionContacto from './pages/admin/gestionContacto';
import GestionTurnos from './pages/admin/GestionTurnos';

import PagoExitoPage from './pages/client/PagoExitoPage';
import PagoFalloPage from './pages/client/PagoFalloPage';
import PagoPendientePage from './pages/client/PagoPendientePage';

import CarroPag from './pages/client/CarroPag';
import NuevoCliente from './pages/admin/nuevoCliente';
import CheckOut from './pages/client/CheckOut';
import ProtectedRoute from './components/ProtectedRoute';
import './App.css'

function App() {
  return (
    <div className="App">
      <Routes>

        <Route path="/" element={<InicioPag />} />  
        <Route path="/login" element={<Login />} />
        <Route path="/inicio" element={<InicioPag />} />
        <Route path="/catalogo" element={<CatalogoPag />} />
        <Route path="/nosotros" element={<NosotrosPag />} />
        <Route path="/contacto" element={<ContactoPag />} />
        <Route path="/carrito" element={<CarroPag />} />
        <Route path="/checkout" element={<CheckOut />} />
        <Route path="/mi-perfil" element={<ProtectedRoute element={<MiPerfilPag />} />} />
        <Route path="/pago/exito" element={<PagoExitoPage />} />
        <Route path="/pago/fallo" element={<PagoFalloPage />} />
        <Route path="/pago/pendiente" element={<PagoPendientePage />} />
                  
        <Route path="/admin/dashboard" element={<ProtectedRoute element={<Dashboard />} requiredRole="ADMIN" />} />
        <Route path="/admin/gestion-pedidos" element={<ProtectedRoute element={<GestionPedidos />} requiredRole="ADMIN" />} />
        <Route path="/admin/gestion-productos" element={<ProtectedRoute element={<GestionCatalogo />} requiredRole="ADMIN" />} />
        <Route path="/admin/gestion-clientes" element={<ProtectedRoute element={<GestionClientes />} requiredRole="ADMIN" />} />
        <Route path="/admin/gestion-trabajadores" element={<ProtectedRoute element={<GestionTrabajadores />} requiredRole="ADMIN" />} />
        <Route path="/admin/nuevo-usuario" element={<ProtectedRoute element={<NuevoUsuario />} requiredRole="ADMIN" />} />
        <Route path="/admin/nuevo-cliente" element={<ProtectedRoute element={<NuevoCliente />} requiredRole="ADMIN" />} />
        <Route path="/admin/gestion-venta" element={<ProtectedRoute element={<GestionVenta />} requiredRole="ADMIN" />} />
        <Route path="/admin/gestion-contacto" element={<ProtectedRoute element={<GestionContacto />} requiredRole="ADMIN" />} />
        <Route path="/admin/gestion-turnos" element={<ProtectedRoute element={<GestionTurnos />} requiredRole="ADMIN" />} />
              
      </Routes>
    </div>
  );
}

export default App;