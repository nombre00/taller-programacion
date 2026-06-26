// src/services/posicionesService.js
import api from "../config/api";

const BASE = "/turnos/posiciones";

export const getPosiciones = () =>
  api.get(BASE).then(r => r.data);

export const createPosicion = (data) =>
  api.post(BASE, data).then(r => r.data);

export const updatePosicion = (id, data) =>
  api.put(`${BASE}/${id}`, data).then(r => r.data);

export const deletePosicion = (id) =>
  api.delete(`${BASE}/${id}`);