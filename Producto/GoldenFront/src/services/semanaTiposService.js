// src/services/semanaTiposService.js
import api from "../config/api";

const BASE_SEMANAS     = "/turnos/semanas-tipo";
const BASE_ASIGNACIONES = "/turnos/asignaciones-turno";
const BASE_PLANTILLAS  = "/turnos/plantillas-turno";

// — SemanaTipo —
export const getSemanaTipos = () =>
  api.get(BASE_SEMANAS).then(r => r.data);

export const createSemanaTipo = (data) =>
  api.post(BASE_SEMANAS, data).then(r => r.data);

export const updateSemanaTipo = (id, data) =>
  api.put(`${BASE_SEMANAS}/${id}`, data).then(r => r.data);

export const deleteSemanaTipo = (id) =>
  api.delete(`${BASE_SEMANAS}/${id}`);

// — AsignacionTurno —
export const getAsignacionesPorSemana = (idSemana) =>
  api.get(`${BASE_ASIGNACIONES}/semana/${idSemana}`).then(r => r.data);

export const createAsignacion = (data) =>
  api.post(BASE_ASIGNACIONES, data).then(r => r.data);

export const deleteAsignacion = (id) =>
  api.delete(`${BASE_ASIGNACIONES}/${id}`);

// — Plantillas (para select) —
export const getPlantillasParaSelect = () =>
  api.get(BASE_PLANTILLAS).then(r => r.data);