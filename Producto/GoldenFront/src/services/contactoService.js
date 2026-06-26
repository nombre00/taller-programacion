// src/services/contactoService.js

import api from "../config/api";

/**
 * ================================
 *   FORMULARIO PÚBLICO → POST
 * ================================
 * Guarda un mensaje enviado desde el front público
 * No requiere autenticación
 */
export const enviarMensajeContacto = async (mensaje) => {
  try {
    const response = await api.post("/mensajes", mensaje);
    return response.data;
  } catch (error) {
    console.error("Error al enviar mensaje:", error);
    throw error;
  }
};

/**
 * ================================
 *   ADMIN → GET (requiere token)
 * ================================
 * Obtiene todos los mensajes
 */
export const listarMensajesContacto = async () => {
  try {
    const response = await api.get("/mensajes");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al listar mensajes:", error);
    throw error;
  }
};

/**
 * ================================
 *   ADMIN → GET POR ID
 * ================================
 */
export const obtenerMensajePorId = async (id) => {
  try {
    const response = await api.get(`/mensajes/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener mensaje ${id}:`, error);
    throw error;
  }
};

/**
 * ================================
 *   ADMIN → FILTRAR POR ESTADO
 *  estado: 0=no leído, 1=leído, 2=respondido
 * ================================
 */
export const listarMensajesPorEstado = async (estado) => {
  try {
    const response = await api.get(`/mensajes/estado/${estado}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al filtrar por estado (${estado}):`, error);
    throw error;
  }
};

/**
 * ================================
 *   ADMIN → CAMBIAR ESTADO
 * ================================
 */
export const actualizarEstadoMensaje = async (idMensaje, nuevoEstado) => {
  try {
    const response = await api.put(`/mensajes/${idMensaje}/estado/${nuevoEstado}`);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar estado del mensaje ${idMensaje}:`, error);
    throw error;
  }
};

/**
 * ================================
 *   ADMIN → ELIMINAR
 * ================================
 */
export const eliminarMensajeContacto = async (idMensaje) => {
  try {
    const response = await api.delete(`/mensajes/${idMensaje}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar mensaje ${idMensaje}:`, error);
    throw error;
  }
};
