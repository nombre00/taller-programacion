import api from "../config/api";

/**
 * Servicio para gestión de pedidos
 * Endpoints del microservicio de Pedidos
 */

// Listar todos los pedidos
export const getPedidos = async () => {
  try {
    const response = await api.get("/pedidos");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener pedidos:", error);
    throw error;
  }
};

// Obtener pedido por ID
export const getPedidoPorId = async (id) => {
  try {
    const response = await api.get(`/pedidos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener pedido ${id}:`, error);
    throw error;
  }
};

// Listar pedidos por cliente
export const getPedidosPorCliente = async (clienteId) => {
  try {
    const response = await api.get(`/pedidos/cliente/${clienteId}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener pedidos del cliente ${clienteId}:`, error);
    throw error;
  }
};

// Crear nuevo pedido
export const crearPedido = async (pedido) => {
  try {
    const response = await api.post("/pedidos/completo", pedido);
    return response.data;
  } catch (error) {
    console.error("Error al crear pedido:", error);
    throw error;
  }
};

// Eliminar pedido
export const eliminarPedido = async (id) => {
  try {
    const response = await api.delete(`/pedidos/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar pedido ${id}:`, error);
    throw error;
  }
};

// Actualizar estado de pedido sin realizar venta
export const actualizarEstadoPedido = async (idPedido, idEstado) => {
  try {
    const response = await api.put(`/pedidos/cambiar-estado/${idPedido}/estado/${idEstado}`);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar estado del pedido ${idPedido}:`, error);
    throw error;
  }
};

/**
 * Actualizar pedido a PAGADO.
 *
 * - Si tiempoEspera es un número → envía el query param al backend,
 *   que confirmará el pedido Y enviará WhatsApp al cliente.
 * - Si tiempoEspera es null/undefined → no envía el query param,
 *   el backend solo confirma el pedido sin notificación WhatsApp.
 */
export const actualizarPedidoAPagado = async (idPedido, tiempoEspera = null) => {
  try {
    const config = tiempoEspera != null
      ? { params: { tiempoEspera } }
      : {};

    const response = await api.put(`/pedidos/procesar/${idPedido}`, null, config);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar pedido a pagado ${idPedido}:`, error);
    console.error("Response:", error.response?.data);
    throw error;
  }
};

// Obtener pedidos por cliente
export const getPedidosCliente = async (clienteId) => {
  try {
    const response = await api.get(`/pedidos/cliente/${clienteId}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener pedidos del cliente ${clienteId}:`, error);
    throw error;
  }
};

// Obtener detalles de un pedido de un cliente
export const getDetallesPedido = async (idCliente) => {
  try {
    const response = await api.get(`/pedidos/detalles/cliente/${idCliente}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener detalles del pedido ${idCliente}:`, error);
    throw error;
  }
};

/**
 * Actualizar un pedido existente con los datos finales del checkout
 * PUT /api/pedidos/{idPedido}/actualizar
 */
export const actualizarPedido = async (idPedido, datosActualizacion) => {
  try {
    console.log(`Actualizando pedido ${idPedido}...`);
    const response = await api.put(`/pedidos/${idPedido}/actualizar`, datosActualizacion);
    return response.data;
  } catch (error) {
    console.error("Error al actualizar pedido:", error);
    console.error("Respuesta:", error.response?.data);
    throw error;
  }
};