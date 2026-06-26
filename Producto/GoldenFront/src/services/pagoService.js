// src/services/pagoService.js
// Servicio para pagos con Mercado Pago - Alineado con el backend Java

import api from "../config/api";

/**
 * Crear una preferencia de pago en Mercado Pago
 * POST /api/pagos/crear-preferencia
 * 
 * @param {Object} datosPago
 * @param {number} datosPago.idPedido - ID del pedido a pagar
 * @param {number} datosPago.montoPago - Monto total del pago
 * @param {string} datosPago.descripcion - Descripción del pago
 * @param {string} datosPago.emailPagador - Email del cliente
 * @param {string} datosPago.nombrePagador - Nombre del cliente
 * @returns {Promise<Object>} { idPago, urlPago, idPreferenciaMpos, etc. }
 */
export const crearPreferenciaPago = async (datosPago) => {
  try {
    console.log('💳 Creando preferencia de pago en Mercado Pago...');
    console.log('📤 Datos a enviar:', datosPago);

    // Llamar al endpoint del backend
    const response = await api.post('/pagos/crear-preferencia', datosPago);
    
    console.log('✅ Preferencia creada exitosamente:', response.data);
    
    // El backend retorna:
    // {
    //   idPago: 123,
    //   idPedido: 456,
    //   montoPago: 15000,
    //   estadoPago: 1,
    //   idPreferenciaMpos: "xxx-xxx-xxx",
    //   urlPago: "https://www.mercadopago.cl/checkout/v1/redirect?pref_id=xxx"
    // }
    return response.data;

  } catch (error) {
    console.error(' Error al crear preferencia de pago:', error);
    console.error(' Respuesta del servidor:', error.response?.data);
    throw error;
  }
};

/**
 * Actualizar estado de un pago
 * PUT /api/pagos/{id}/estado?estado={estadoId}&idPagoMpos={id}&respuestaMp={json}
 * 
 * @param {number} idPago - ID del pago
 * @param {number} estadoId - ID del estado: 1=Pendiente, 2=Aprobado, 3=Rechazado
 * @param {string} idPagoMpos - ID del pago en Mercado Pago (opcional)
 * @param {string} respuestaMp - Respuesta JSON de Mercado Pago (opcional)
 * @returns {Promise<Object>} Pago actualizado
 */
export const actualizarEstadoPago = async (idPago, estadoId, idPagoMpos = null, respuestaMp = null) => {
  try {
    console.log(`🔄 Actualizando estado del pago ${idPago} a estado ${estadoId}...`);
    
    // Construir los parámetros de consulta
    const params = new URLSearchParams();
    params.append('estado', estadoId);
    if (idPagoMpos) params.append('idPagoMpos', idPagoMpos);
    if (respuestaMp) params.append('respuestaMp', respuestaMp);
    
    const response = await api.put(`/pagos/${idPago}/estado?${params.toString()}`);
    
    console.log('✅ Estado actualizado:', response.data);
    return response.data;

  } catch (error) {
    console.error( 'Error al actualizar estado del pago:', error);
    throw error;
  }
};

/**
 * Obtener todos los pagos (solo ADMIN y TRABAJADOR)
 * GET /api/pagos
 */
export const obtenerTodosPagos = async () => {
  try {
    const response = await api.get('/pagos');
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error('Error al obtener pagos:', error);
    throw error;
  }
};

/**
 * Obtener un pago por su ID
 * GET /api/pagos/{id}
 */
export const obtenerPagoPorId = async (idPago) => {
  try {
    const response = await api.get(`/pagos/${idPago}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener pago ${idPago}:`, error);
    throw error;
  }
};

/**
 * Obtener todos los pagos de un pedido
 * GET /api/pagos/pedido/{idPedido}
 */
export const obtenerPagosPorPedido = async (idPedido) => {
  try {
    const response = await api.get(`/pagos/pedido/${idPedido}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener pagos del pedido ${idPedido}:`, error);
    throw error;
  }
};

/**
 * Cancelar un pago (solo ADMIN)
 * DELETE /api/pagos/{id}
 */
export const cancelarPago = async (idPago) => {
  try {
    console.log('Cancelando pago:', idPago);
    
    const response = await api.delete(`/pagos/${idPago}`);
    
    console.log('Pago cancelado:', response.data);
    return response.data;

  } catch (error) {
    console.error('Error al cancelar pago:', error);
    throw error;
  }
};