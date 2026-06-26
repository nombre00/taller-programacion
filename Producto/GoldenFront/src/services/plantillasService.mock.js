// src/services/plantillasService.mock.js
//
// Diseño: una PlantillaTurno por posición y franja horaria.
// El trabajador se asigna después, en la página de Planificación.
//
// Plantillas:
//   1  — Turno Cocinero Tarde   (Cocinero,  14:00–21:00)
//   2  — Turno Cajero Tarde     (Cajero,    14:00–21:00)
//   3  — Turno Cocinero Noche   (Cocinero,  21:00–02:00)
//   4  — Turno Limpieza Tarde   (Limpieza,  14:00–21:00)

const posicionesMock = [
  { idPosicion: 1, nombre: "Cocinero",  color: "#FF9800" },
  { idPosicion: 2, nombre: "Cajero",    color: "#4CAF50" },
  { idPosicion: 3, nombre: "Limpieza",  color: "#9C27B0" },
];

const plantillas = [
  {
    idPlantilla: 1,
    nombre: "Turno Cocinero Tarde",
    horaInicio: "14:00", horaTermino: "21:00",
    descripcion: "Turno tarde para posición Cocinero",
    slots: [
      { idSlot: 1, idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", idPosicion: 1, nombrePosicion: "Cocinero", colorPosicion: "#FF9800", nombre: null, cantidad: 1 },
    ],
  },
  {
    idPlantilla: 2,
    nombre: "Turno Cajero Tarde",
    horaInicio: "14:00", horaTermino: "21:00",
    descripcion: "Turno tarde para posición Cajero",
    slots: [
      { idSlot: 2, idPlantilla: 2, nombrePlantilla: "Turno Cajero Tarde", idPosicion: 2, nombrePosicion: "Cajero", colorPosicion: "#4CAF50", nombre: null, cantidad: 1 },
    ],
  },
  {
    idPlantilla: 3,
    nombre: "Turno Cocinero Noche",
    horaInicio: "21:00", horaTermino: "02:00",
    descripcion: "Turno noche para posición Cocinero",
    slots: [
      { idSlot: 3, idPlantilla: 3, nombrePlantilla: "Turno Cocinero Noche", idPosicion: 1, nombrePosicion: "Cocinero", colorPosicion: "#FF9800", nombre: null, cantidad: 1 },
    ],
  },
  {
    idPlantilla: 4,
    nombre: "Turno Limpieza Tarde",
    horaInicio: "14:00", horaTermino: "21:00",
    descripcion: "Turno tarde para posición Limpieza",
    slots: [
      { idSlot: 4, idPlantilla: 4, nombrePlantilla: "Turno Limpieza Tarde", idPosicion: 3, nombrePosicion: "Limpieza", colorPosicion: "#9C27B0", nombre: null, cantidad: 1 },
    ],
  },
];

let nextPlantillaId = 5;
let nextSlotId      = 5;

export const getPlantillas = () =>
  Promise.resolve(JSON.parse(JSON.stringify(plantillas)));

export const createPlantilla = (data) => {
  const nueva = { idPlantilla: nextPlantillaId++, ...data, slots: [] };
  plantillas.push(nueva);
  return Promise.resolve(nueva);
};

export const updatePlantilla = (id, data) => {
  const idx = plantillas.findIndex(p => p.idPlantilla === id);
  if (idx === -1) return Promise.reject("No encontrado");
  plantillas[idx] = { ...plantillas[idx], ...data };
  return Promise.resolve(plantillas[idx]);
};

export const deletePlantilla = (id) => {
  const idx = plantillas.findIndex(p => p.idPlantilla === id);
  if (idx === -1) return Promise.reject("No encontrado");
  if (plantillas[idx].slots.length > 0) return Promise.reject("Tiene slots asociados");
  plantillas.splice(idx, 1);
  return Promise.resolve();
};

export const createSlot = (data) => {
  const plantilla = plantillas.find(p => p.idPlantilla === Number(data.idPlantilla));
  if (!plantilla) return Promise.reject("Plantilla no encontrada");
  const posicion = posicionesMock.find(p => p.idPosicion === Number(data.idPosicion));
  const nuevo = {
    idSlot:          nextSlotId++,
    idPlantilla:     Number(data.idPlantilla),
    nombrePlantilla: plantilla.nombre,
    idPosicion:      Number(data.idPosicion),
    nombrePosicion:  posicion?.nombre || "Desconocida",
    colorPosicion:   posicion?.color  || "#888",
    nombre:          data.nombre || null,
    cantidad:        Number(data.cantidad),
  };
  plantilla.slots.push(nuevo);
  return Promise.resolve(nuevo);
};

export const updateSlot = (id, data) => {
  for (const p of plantillas) {
    const idx = p.slots.findIndex(s => s.idSlot === id);
    if (idx !== -1) {
      const posicion = posicionesMock.find(pos => pos.idPosicion === Number(data.idPosicion));
      p.slots[idx] = {
        ...p.slots[idx],
        idPosicion:     Number(data.idPosicion),
        nombrePosicion: posicion?.nombre || p.slots[idx].nombrePosicion,
        colorPosicion:  posicion?.color  || p.slots[idx].colorPosicion,
        nombre:         data.nombre || null,
        cantidad:       Number(data.cantidad),
      };
      return Promise.resolve(p.slots[idx]);
    }
  }
  return Promise.reject("Slot no encontrado");
};

export const deleteSlot = (id) => {
  for (const p of plantillas) {
    const idx = p.slots.findIndex(s => s.idSlot === id);
    if (idx !== -1) {
      p.slots.splice(idx, 1);
      return Promise.resolve();
    }
  }
  return Promise.reject("Slot no encontrado");
};