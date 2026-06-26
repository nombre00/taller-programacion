// src/services/calendarioTurnosService.mock.js
//
// Obtiene los horarios desde planificacionService.mock.js
// para mantener una única fuente de verdad en los mocks.

import { getTodosLosHorarios } from "./planificacionService.mock";

/**
 * Devuelve todos los horarios del mes/año indicado.
 * @param {number} anio
 * @param {number} mes  — basado en 1 (enero = 1)
 */
export const getHorariosPorMes = async (anio, mes) => {
  const todos = await getTodosLosHorarios();
  return todos.filter(h => {
    const fecha = new Date(h.fechaTrabajo + "T00:00:00");
    return fecha.getFullYear() === anio && fecha.getMonth() + 1 === mes;
  });
}; 