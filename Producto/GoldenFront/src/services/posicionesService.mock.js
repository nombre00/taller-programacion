// src/services/posicionesService.mock.js

const posiciones = [
  { idPosicion: 1, nombre: "Cocinero",  descripcion: "Preparación de alimentos",  sueldo: 620000, color: "#FF9800" },
  { idPosicion: 2, nombre: "Cajero",    descripcion: "Atención en caja",           sueldo: 520000, color: "#4CAF50" },
  { idPosicion: 3, nombre: "Limpieza",  descripcion: "Aseo y mantención del local", sueldo: 480000, color: "#9C27B0" },
];

let nextId = 4;

export const getPosiciones = () =>
  Promise.resolve(JSON.parse(JSON.stringify(posiciones)));

export const createPosicion = (data) => {
  const nueva = { idPosicion: nextId++, ...data };
  posiciones.push(nueva);
  return Promise.resolve(nueva);
};

export const updatePosicion = (id, data) => {
  const idx = posiciones.findIndex(p => p.idPosicion === id);
  if (idx === -1) return Promise.reject("No encontrado");
  posiciones[idx] = { ...posiciones[idx], ...data };
  return Promise.resolve(posiciones[idx]);
};

export const deletePosicion = (id) => {
  const idx = posiciones.findIndex(p => p.idPosicion === id);
  if (idx === -1) return Promise.reject("No encontrado");
  posiciones.splice(idx, 1);
  return Promise.resolve();
};