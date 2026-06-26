// src/services/calendarioTurnosService.js

import api from "../config/api";

/**
 * Devuelve todos los horarios del mes/año indicado.
 * Obtiene todos los horarios del backend y filtra por mes en frontend.
 * @param {number} anio
 * @param {number} mes  — basado en 1 (enero = 1)
 */
export const getHorariosPorMes = async (anio, mes) => {
  const response = await api.get("/turnos/horarios");
  return response.data.filter(h => {
    const fecha = new Date(h.fechaTrabajo + "T00:00:00");
    return fecha.getFullYear() === anio && fecha.getMonth() + 1 === mes;
  });
};