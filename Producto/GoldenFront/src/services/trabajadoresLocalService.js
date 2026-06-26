// src/services/trabajadoresLocalService.js
import api from "../config/api";

const BASE_TURNOS   = "/turnos/trabajadores-local";
const BASE_USUARIOS = "/usuarios/trabajadores";

export const getTrabajadoresLocal = () =>
  api.get(BASE_TURNOS).then(r => r.data);

export const getTrabajadoresUsuario = () =>
  api.get(BASE_USUARIOS).then(r => r.data);

export const createTrabajadorLocal = (data) =>
  api.post(BASE_TURNOS, data).then(r => r.data);

export const updateTrabajadorLocal = (id, data) =>
  api.put(`${BASE_TURNOS}/${id}`, data).then(r => r.data);

export const desactivarTrabajador = (id) =>
  api.delete(`${BASE_TURNOS}/${id}`);