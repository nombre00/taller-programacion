// src/components/client/HeaderComp.jsx

import React, { useState, useEffect } from 'react';
import { Link, NavLink, useNavigate } from 'react-router-dom';

// Importamos los componentes específicos de React-Bootstrap
import { Navbar, Nav, Container, NavDropdown, Button, Badge } from 'react-bootstrap';
import logo from '/src/assets/img/Logo.JPG';

export default function HeaderComp() {
  // --- 1. LÓGICA Y ESTADO (Esto no cambia) ---
  const [usuario, setUsuario] = useState(null);
  const [userRole, setUserRole] = useState(null);
// estado para el contador del carrito
  const [contadorCarrito, setContadorCarrito] = useState(0);
//===============================================================================
  const navigate = useNavigate();

  useEffect(() => {
    const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
    if (isLoggedIn) {
      const userName = localStorage.getItem('userName');
      const role = localStorage.getItem('userRole');
      setUsuario({ nombre: userName });
      setUserRole(role);
    }
  }, []);

  // Escuchar cambios en el carrito para actualizar el contador
  useEffect(() => {
    const actualizarContador = () => {
      const carritoGuardado = localStorage.getItem('carrito');
      if (carritoGuardado) {
        const carrito = JSON.parse(carritoGuardado);
        const totalProductos = carrito.reduce((total, item) => 
          total + item.cantidad, 0
        );
        setContadorCarrito(totalProductos);
      } else {
        setContadorCarrito(0);
      }
    };

    actualizarContador();
    window.addEventListener('storage', actualizarContador);
    return () => window.removeEventListener('storage', actualizarContador);
  }, []);
//===============================================================================

  // Función para cerrar sesión
  const handleLogout = () => {
    // Limpiar TODOS los datos de sesión del localStorage
    localStorage.removeItem('authToken');
    localStorage.removeItem('user');
    localStorage.removeItem('userId');
    localStorage.removeItem('userName');
    localStorage.removeItem('userRole');
    localStorage.removeItem('isLoggedIn');
    localStorage.removeItem('carrito');
    localStorage.removeItem('pedidoPendientePago');

    // Actualizar el estado
    setUsuario(null);
    setContadorCarrito(0);

    // Disparar evento storage para notificar a otros componentes
    window.dispatchEvent(new Event('storage'));

    // Redirigir al inicio
    navigate('/');
  };

  // --- 2. RENDERIZADO CON COMPONENTES DE REACT-BOOTSTRAP ---
  return (
    // Usamos el componente <Navbar> en lugar de <nav> y <header>
    // Las propiedades (props) como 'bg', 'variant', 'sticky' reemplazan a las clases CSS.
    <Navbar bg="dark" variant="dark" sticky="top" expand="lg" className="shadow-sm rounded-bottom-4">
      <Container>
        {/* <Navbar.Brand> es el reemplazo de 'navbar-brand' y lo convertimos en un Link */}
        <Navbar.Brand as={Link} to="/">
          <img src={logo} height="26" alt="Golden Burger" className="d-inline-block align-top me-2" />
          <strong>GOLDEN <span className="text-warning">BURGER</span></strong>
        </Navbar.Brand>

        {/* Estos componentes gestionan el menú hamburguesa en móviles automáticamente */}
        <Navbar.Toggle aria-controls="basic-navbar-nav" />
        <Navbar.Collapse id="basic-navbar-nav">
          
          
          <Nav className="mx-auto">
            
            <Nav.Link as={NavLink} to="/inicio" className="fw-bold">INICIO</Nav.Link>
            <Nav.Link as={NavLink} to="/catalogo" className="fw-bold">CATÁLOGO</Nav.Link>
            <Nav.Link as={NavLink} to="/nosotros" className="fw-bold">NOSOTROS</Nav.Link>
            <Nav.Link as={NavLink} to="/contacto" className="fw-bold">CONTACTO</Nav.Link>
          </Nav>

          {/* Sección derecha de la barra de navegación */}
          <Nav className="align-items-center">
            <Nav.Link as={Link} to="/carrito" className="fs-5 position-relative me-3">
              <i className="bi bi-cart"></i>
              {contadorCarrito > 0 && (
                <Badge 
                  bg="danger" 
                  pill 
                  className="position-absolute top-0 start-100 translate-middle"
                  style={{ fontSize: '0.7rem' }}
                >
                  {contadorCarrito}
                </Badge>
              )}
            </Nav.Link>

            {/* La lógica condicional de React se mantiene igual */}
            {usuario ? (
              // Usamos el componente <NavDropdown> para el menú de usuario. Es mucho más limpio.
              <NavDropdown title={`Hola, ${usuario.nombre}`} id="basic-nav-dropdown" menuVariant="dark">
                {/* Mi Perfil - Solo visible para Cliente */}
                {userRole === 'Cliente' && (
                  <NavDropdown.Item as={Link} to="/mi-perfil">
                    <i className="bi bi-person-circle me-2"></i>
                    Mi Perfil
                  </NavDropdown.Item>
                )}

                {/* Panel de Administración - Solo visible para Admin y Trabajador */}
                {(userRole === 'Admin' || userRole === 'Trabajador') && (
                  <NavDropdown.Item as={Link} to="/admin/dashboard">
                    <i className="bi bi-gear-fill me-2"></i>
                    Panel de Administración
                  </NavDropdown.Item>
                )}

                <NavDropdown.Divider />
                <NavDropdown.Item onClick={handleLogout}>
                  <i className="bi bi-box-arrow-right me-2"></i>
                  Cerrar sesión
                </NavDropdown.Item>
              </NavDropdown>
            ) : (
              // Usamos un componente <Button> para el login, que convertimos en un Link.
              <Button as={Link} to="/login" variant="warning" size="sm" className="text-dark fw-semibold">
                <i className="bi bi-box-arrow-in-right me-1"></i> Iniciar sesión
              </Button>
            )}
          </Nav>

        </Navbar.Collapse>
      </Container>
    </Navbar>
  );
}