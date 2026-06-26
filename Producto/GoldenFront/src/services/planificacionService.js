// src/services/planificacionService.js
import api from "../config/api";

const BASE_CALENDARIOS = "/turnos/calendario-semanas";
const BASE_HORARIOS    = "/turnos/horarios";
const BASE_SEMANAS     = "/turnos/semanas-tipo";
const BASE_TRABAJADORES = "/turnos/trabajadores-local";

// — CalendarioSemana —
export const getCalendarios = () =>
  api.get(BASE_CALENDARIOS).then(r => r.data);

export const createCalendario = (data) =>
  api.post(BASE_CALENDARIOS, { ...data, repeticionAnual: false }).then(r => r.data);

export const updateCalendario = (id, data) =>
  api.put(`${BASE_CALENDARIOS}/${id}`, { ...data, repeticionAnual: false }).then(r => r.data);

export const deleteCalendario = (id) =>
  api.delete(`${BASE_CALENDARIOS}/${id}`);

// — HorarioTrabajador —
export const getHorariosPorCalendario = (idCalendario) =>
  api.get(`${BASE_HORARIOS}/calendario/${idCalendario}`).then(r => r.data);

export const asignarTrabajador = (idHorario, idTrabajador) =>
  api.put(`${BASE_HORARIOS}/${idHorario}/asignar`, { idTrabajador }).then(r => r.data);

export const deleteHorario = (idHorario) =>
  api.delete(`${BASE_HORARIOS}/${idHorario}`);

// — SemanaTipos (para el select) —
export const getSemanasParaSelect = () =>
  api.get(BASE_SEMANAS).then(r => r.data);

// — TrabajadoresLocal (para el select de asignación) —
export const getTrabajadoresActivos = () =>
  api.get(BASE_TRABAJADORES).then(r => r.data.filter(t => t.activo));