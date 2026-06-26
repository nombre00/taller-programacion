import React from 'react';
import { Navigate } from 'react-router-dom';

/**
 * Componente ProtectedRoute para proteger rutas que requieren autenticación
 * @param {React.ReactNode} element - El componente a renderizar si el usuario está autenticado
 * @param {string} requiredRole - El rol requerido (opcional). Si no se proporciona, solo requiere autenticación
 * @returns {React.ReactNode} El elemento si el usuario está autenticado y tiene el rol requerido, o <Navigate> a login
 */
export default function ProtectedRoute({ element, requiredRole = null }) {
  const isLoggedIn = localStorage.getItem('isLoggedIn') === 'true';
  const userRole = localStorage.getItem('userRole'); // "Admin", "Trabajador", "Cliente"

  console.log("ProtectedRoute - isLoggedIn:", isLoggedIn, "userRole:", userRole, "requiredRole:", requiredRole);

  // Si el usuario no está autenticado, redirigir a login
  if (!isLoggedIn) {
    console.log("No autenticado, redirigiendo a login");
    return <Navigate to="/login" replace />;
  }

  // Si se requiere un rol específico
  if (requiredRole) {
    // Comparar roles (backend devuelve "Admin", "Trabajador", "Cliente")
    const isAuthorized = 
      (requiredRole === "ADMIN" && (userRole === "Admin" || userRole === "Trabajador")) ||
      (requiredRole === "Trabajador" && userRole === "Trabajador") ||
      (requiredRole === "Cliente" && userRole === "Cliente");

    if (!isAuthorized) {
      console.log("Rol insuficiente, redirigiendo a inicio");
      return <Navigate to="/inicio" replace />;
    }
  }

  // Usuario autenticado y con permisos suficientes
  console.log("Acceso permitido");
  return element;
}
