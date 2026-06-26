// src/services/plantillasService.js
import api from "../config/api";

const BASE_PLANTILLAS = "/turnos/plantillas-turno";
const BASE_SLOTS      = "/turnos/slots-turno";

export const getPlantillas = () =>
  api.get(BASE_PLANTILLAS).then(r => r.data);

export const createPlantilla = (data) =>
  api.post(BASE_PLANTILLAS, data).then(r => r.data);

export const updatePlantilla = (id, data) =>
  api.put(`${BASE_PLANTILLAS}/${id}`, data).then(r => r.data);

export const deletePlantilla = (id) =>
  api.delete(`${BASE_PLANTILLAS}/${id}`);

export const createSlot = (data) =>
  api.post(BASE_SLOTS, data).then(r => r.data);

export const updateSlot = (id, data) =>
  api.put(`${BASE_SLOTS}/${id}`, data).then(r => r.data);

export const deleteSlot = (id) =>
  api.delete(`${BASE_SLOTS}/${id}`);