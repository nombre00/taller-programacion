import api from "../config/api";

/**
 * Servicio para gestión de Recetas
 * Ruta base: /catalogo/recetas
 */

export const obtenerTodasRecetas = async () => {
  try {
    const response = await api.get("/catalogo/recetas");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener recetas:", error);
    throw error;
  }
};

export const obtenerRecetaPorId = async (id) => {
  try {
    const response = await api.get(`/catalogo/recetas/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener receta ${id}:`, error);
    throw error;
  }
};

export const obtenerRecetaPorProducto = async (idProducto) => {
  try {
    const response = await api.get(`/catalogo/recetas/producto/${idProducto}`);
    return response.data;
  } catch (error) {
    if (error.response?.status === 404) return null;
    console.error(`Error al obtener receta del producto ${idProducto}:`, error);
    throw error;
  }
};

export const crearReceta = async (data) => {
  try {
    const response = await api.post("/catalogo/recetas", data);
    return response.data;
  } catch (error) {
    console.error("Error al crear receta:", error);
    throw error;
  }
};

export const actualizarReceta = async (id, data) => {
  try {
    const response = await api.put(`/catalogo/recetas/${id}`, data);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar receta ${id}:`, error);
    throw error;
  }
};

export const desactivarReceta = async (id) => {
  try {
    const response = await api.delete(`/catalogo/recetas/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al desactivar receta ${id}:`, error);
    throw error;
  }
};