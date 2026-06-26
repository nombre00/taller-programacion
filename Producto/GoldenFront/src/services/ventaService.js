import api from "../config/api";

/**
 * Servicio de gestión de ventas
 * Endpoints del microservicio GestiónVenta
 */

// Obtener todas las ventas (ADMIN / TRABAJADOR)
export const getVentas = async () => {
  try {
    const response = await api.get("/ventas");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener ventas:", error);
    throw error;
  }
};

// Obtener una venta por ID (ADMIN / TRABAJADOR)
export const getVentaPorId = async (id) => {
  try {
    const response = await api.get(`/ventas/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener venta ${id}:`, error);
    throw error;
  }
};

// Actualizar venta (ADMIN / TRABAJADOR)
export const actualizarVenta = async (id, venta) => {
  try {
    const response = await api.put(`/ventas/${id}`, venta);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar venta ${id}:`, error);
    throw error;
  }
};

// Eliminar venta (ADMIN)
export const eliminarVenta = async (id) => {
  try {
    await api.delete(`/ventas/${id}`);
    return true;
  } catch (error) {
    console.error(`Error al eliminar venta ${id}:`, error);
    throw error;
  }
};
