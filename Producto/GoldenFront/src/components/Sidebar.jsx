import React from 'react';
import { NavLink, Link, useNavigate } from 'react-router-dom';
import '../styles/estilosAdmin.css';
import Nav from 'react-bootstrap/Nav';

export default function Sidebar({ adminName, onLogoutAdmin }) {
  const navigate = useNavigate();

  const handleVistaCliente = () => {
    navigate('/inicio');
  };

  const handleLogout = () => {
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    localStorage.removeItem('isLoggedIn');
    window.dispatchEvent(new Event('storage'));
    if (onLogoutAdmin) onLogoutAdmin();
    navigate('/');
  };

  return (
    <aside className="sidebar-admin"> 
      <div className="sidebar-header">
        <Link to="/admin/dashboard" className="sidebar-logo">
          <strong>
            <span>GOLDEN</span> 
            <span className="text-warning">BURGER</span>
          </strong>
        </Link>
        {adminName && <p className="sidebar-greeting">Bienvenido, {adminName}</p>}
      </div>

      <Nav className="sidebar-nav flex-column" variant="pills" defaultActiveKey="/admin/dashboard">

        <Nav.Link as={NavLink} to="/admin/dashboard">
          <i className="bi bi-speedometer2 me-2"></i>
          <span className="nav-link-text-full">Dashboard</span>
          <span className="nav-link-text-short">Dashboard</span>
        </Nav.Link>

        <Nav.Link as={NavLink} to="/admin/gestion-clientes">
          <i className="bi bi-person-check me-2"></i>
          <span className="nav-link-text-full">Gestión Clientes</span>
          <span className="nav-link-text-short">Clientes</span>
        </Nav.Link>

        <Nav.Link as={NavLink} to="/admin/gestion-trabajadores">
          <i className="bi bi-person-badge me-2"></i>
          <span className="nav-link-text-full">Gestión Trabajadores</span>
          <span className="nav-link-text-short">Trabajadores</span>
        </Nav.Link>
        
        <Nav.Link as={NavLink} to="/admin/gestion-pedidos">
          <i className="bi bi-receipt me-2"></i>
          <span className="nav-link-text-full">Gestión Pedidos</span>
          <span className="nav-link-text-short">Pedidos</span>
        </Nav.Link>
        
        <Nav.Link as={NavLink} to="/admin/gestion-productos">
          <i className="bi bi-box-seam me-2"></i>
          <span className="nav-link-text-full">Gestión Productos</span>
          <span className="nav-link-text-short">Productos</span>
        </Nav.Link>

        <Nav.Link as={NavLink} to="/admin/gestion-venta">
          <i className="bi bi-list-check me-2"></i>
          <span className="nav-link-text-full">Gestión Venta</span>
          <span className="nav-link-text-short">Ventas</span>
        </Nav.Link>

        <Nav.Link as={NavLink} to="/admin/gestion-turnos">
          <i className="bi bi-calendar-week me-2"></i>
          <span className="nav-link-text-full">Gestión Turnos</span>
          <span className="nav-link-text-short">Turnos</span>
        </Nav.Link>
        
        <Nav.Link as={NavLink} to="/admin/gestion-contacto">
          <i className="bi bi-envelope-paper me-2"></i>
          <span className="nav-link-text-full">Gestión Contacto</span>
          <span className="nav-link-text-short">Contacto</span>
        </Nav.Link>

      </Nav>

      <div className="sidebar-footer">
        <button className="btn btn-outline-warning text-warning fw-semibold w-100 mb-2" onClick={handleVistaCliente}>
          <i className="bi bi-house-door me-2"></i>
          Vista Cliente
        </button>
        <button className="btn btn-warning text-dark fw-semibold w-100" onClick={handleLogout}>
          <i className="bi bi-box-arrow-left me-2"></i>
          <span className="btn-text-stacked">
            <span>Cerrar</span>
            <span>Admin</span>
          </span>
        </button>
      </div>
    </aside>
  );
}