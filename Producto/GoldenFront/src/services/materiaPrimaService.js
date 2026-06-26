import api from "../config/api";

/**
 * Servicio para gestión de Materias Primas
 * Ruta base: /catalogo/materias-primas
 */

export const obtenerTodasMateriasPrimas = async () => {
  try {
    const response = await api.get("/catalogo/materias-primas");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener materias primas:", error);
    throw error;
  }
};

export const obtenerMateriaPrimaPorId = async (id) => {
  try {
    const response = await api.get(`/catalogo/materias-primas/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener materia prima ${id}:`, error);
    throw error;
  }
};

export const crearMateriaPrima = async (data) => {
  try {
    const response = await api.post("/catalogo/materias-primas", data);
    return response.data;
  } catch (error) {
    console.error("Error al crear materia prima:", error);
    throw error;
  }
};

export const actualizarMateriaPrima = async (id, data) => {
  try {
    const response = await api.put(`/catalogo/materias-primas/${id}`, data);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar materia prima ${id}:`, error);
    throw error;
  }
};

export const desactivarMateriaPrima = async (id) => {
  try {
    const response = await api.delete(`/catalogo/materias-primas/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al desactivar materia prima ${id}:`, error);
    throw error;
  }
};