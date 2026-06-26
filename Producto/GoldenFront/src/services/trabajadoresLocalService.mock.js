// src/services/trabajadoresLocalService.mock.js 

const trabajadores = [
  { idTrabajador: 1, nombre: "Carlos Rojas",    activo: true, idPosicion: 1, nombrePosicion: "Cocinero" },
  { idTrabajador: 2, nombre: "Diego Fuentes",   activo: true, idPosicion: 1, nombrePosicion: "Cocinero" },
  { idTrabajador: 3, nombre: "Sofía Morales",   activo: true, idPosicion: 1, nombrePosicion: "Cocinero" },
  { idTrabajador: 4, nombre: "Valentina Cruz",  activo: true, idPosicion: 2, nombrePosicion: "Cajero"   },
  { idTrabajador: 5, nombre: "Isabel Pinto",    activo: true, idPosicion: 3, nombrePosicion: "Limpieza" },
];

// Espejo de GESTIONUSUARIO — lo que devuelve GET /api/trabajadores
const trabajadoresUsuario = [
  { idTrabajador: 1, nombreTrabajador: "Carlos Rojas",   rutTrabajador: "12.345.678-9" },
  { idTrabajador: 2, nombreTrabajador: "Diego Fuentes",  rutTrabajador: "13.456.789-0" },
  { idTrabajador: 3, nombreTrabajador: "Sofía Morales",  rutTrabajador: "14.567.890-1" },
  { idTrabajador: 4, nombreTrabajador: "Valentina Cruz", rutTrabajador: "15.678.901-2" },
  { idTrabajador: 5, nombreTrabajador: "Isabel Pinto",   rutTrabajador: "16.789.012-3" },
];

export const getTrabajadoresLocal = () =>
  Promise.resolve(JSON.parse(JSON.stringify(trabajadores)));

export const getTrabajadoresUsuario = () =>
  Promise.resolve(JSON.parse(JSON.stringify(trabajadoresUsuario)));

export const createTrabajadorLocal = (data) => {
  const existe = trabajadores.find(t => t.idTrabajador === data.idTrabajador);
  if (existe) return Promise.reject({ response: { status: 409 } });
  const nuevo = { ...data, activo: true, idPosicion: null, nombrePosicion: null };
  trabajadores.push(nuevo);
  return Promise.resolve(nuevo);
};

export const updateTrabajadorLocal = (id, data) => {
  const idx = trabajadores.findIndex(t => t.idTrabajador === id);
  if (idx === -1) return Promise.reject("No encontrado");
  trabajadores[idx] = { ...trabajadores[idx], ...data };
  return Promise.resolve(trabajadores[idx]);
};

export const desactivarTrabajador = (id) => {
  const idx = trabajadores.findIndex(t => t.idTrabajador === id);
  if (idx === -1) return Promise.reject("No encontrado");
  trabajadores[idx].activo = false;
  return Promise.resolve();
};