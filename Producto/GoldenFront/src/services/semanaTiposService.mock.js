// src/services/semanaTiposService.mock.js
//
// SemanaTipo única: "Semana Estándar"
// Lun–Mié: 2x Cocinero Tarde + 1x Cajero Tarde
// Mar:     además 1x Limpieza Tarde
// Jue:     2x Cocinero Tarde + 1x Cajero Tarde + 1x Limpieza Tarde + 2x Cocinero Noche
// Vie:     2x Cocinero Tarde + 1x Cajero Tarde + 2x Cocinero Noche
// Sáb:     2x Cocinero Noche + 1x Limpieza Tarde
//
// diaSemana: 1=lun, 2=mar, 3=mié, 4=jue, 5=vie, 6=sáb, 7=dom
//
// Plantillas:
//   1 — Turno Cocinero Tarde  (Cocinero, 14:00–21:00)
//   2 — Turno Cajero Tarde    (Cajero,   14:00–21:00)
//   3 — Turno Cocinero Noche  (Cocinero, 21:00–02:00)
//   4 — Turno Limpieza Tarde  (Limpieza, 14:00–21:00)

const plantillasMock = [
  { idPlantilla: 1, nombre: "Turno Cocinero Tarde",  horaInicio: "14:00", horaTermino: "21:00" },
  { idPlantilla: 2, nombre: "Turno Cajero Tarde",    horaInicio: "14:00", horaTermino: "21:00" },
  { idPlantilla: 3, nombre: "Turno Cocinero Noche",  horaInicio: "21:00", horaTermino: "02:00" },
  { idPlantilla: 4, nombre: "Turno Limpieza Tarde",  horaInicio: "14:00", horaTermino: "21:00" },
];

const semanas = [
  {
    idSemana: 1,
    nombre: "Semana Estándar",
    descripcion: "Turnos de tarde lun–vie, turno de noche jue–sáb, limpieza mar/jue/sáb",
    activo: true,
  },
];

const asignaciones = [
  // Lunes — 2x Cocinero Tarde + 1x Cajero Tarde
  { idAsignacion:  1, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 1 },
  { idAsignacion:  2, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 1 },
  { idAsignacion:  3, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 2, nombrePlantilla: "Turno Cajero Tarde",   diaSemana: 1 },

  // Martes — 2x Cocinero Tarde + 1x Cajero Tarde + 1x Limpieza Tarde
  { idAsignacion:  4, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 2 },
  { idAsignacion:  5, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 2 },
  { idAsignacion:  6, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 2, nombrePlantilla: "Turno Cajero Tarde",   diaSemana: 2 },
  { idAsignacion:  7, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 4, nombrePlantilla: "Turno Limpieza Tarde", diaSemana: 2 },

  // Miércoles — 2x Cocinero Tarde + 1x Cajero Tarde
  { idAsignacion:  8, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 3 },
  { idAsignacion:  9, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 3 },
  { idAsignacion: 10, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 2, nombrePlantilla: "Turno Cajero Tarde",   diaSemana: 3 },

  // Jueves — 2x Cocinero Tarde + 1x Cajero Tarde + 1x Limpieza Tarde + 2x Cocinero Noche
  { idAsignacion: 11, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 4 },
  { idAsignacion: 12, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 4 },
  { idAsignacion: 13, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 2, nombrePlantilla: "Turno Cajero Tarde",   diaSemana: 4 },
  { idAsignacion: 14, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 4, nombrePlantilla: "Turno Limpieza Tarde", diaSemana: 4 },
  { idAsignacion: 15, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 3, nombrePlantilla: "Turno Cocinero Noche", diaSemana: 4 },
  { idAsignacion: 16, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 3, nombrePlantilla: "Turno Cocinero Noche", diaSemana: 4 },

  // Viernes — 2x Cocinero Tarde + 1x Cajero Tarde + 2x Cocinero Noche
  { idAsignacion: 17, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 5 },
  { idAsignacion: 18, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 1, nombrePlantilla: "Turno Cocinero Tarde", diaSemana: 5 },
  { idAsignacion: 19, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 2, nombrePlantilla: "Turno Cajero Tarde",   diaSemana: 5 },
  { idAsignacion: 20, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 3, nombrePlantilla: "Turno Cocinero Noche", diaSemana: 5 },
  { idAsignacion: 21, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 3, nombrePlantilla: "Turno Cocinero Noche", diaSemana: 5 },

  // Sábado — 2x Cocinero Noche + 1x Limpieza Tarde
  { idAsignacion: 22, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 3, nombrePlantilla: "Turno Cocinero Noche", diaSemana: 6 },
  { idAsignacion: 23, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 3, nombrePlantilla: "Turno Cocinero Noche", diaSemana: 6 },
  { idAsignacion: 24, idSemana: 1, nombreSemana: "Semana Estándar", idPlantilla: 4, nombrePlantilla: "Turno Limpieza Tarde", diaSemana: 6 },
];

let nextSemanaId     = 2;
let nextAsignacionId = 25;

// — SemanaTipo —

export const getSemanaTipos = () =>
  Promise.resolve(JSON.parse(JSON.stringify(semanas)));

export const createSemanaTipo = (data) => {
  const nueva = { idSemana: nextSemanaId++, ...data, activo: true };
  semanas.push(nueva);
  return Promise.resolve(nueva);
};

export const updateSemanaTipo = (id, data) => {
  const idx = semanas.findIndex(s => s.idSemana === id);
  if (idx === -1) return Promise.reject("No encontrado");
  semanas[idx] = { ...semanas[idx], ...data };
  return Promise.resolve(semanas[idx]);
};

export const deleteSemanaTipo = (id) => {
  const idx = semanas.findIndex(s => s.idSemana === id);
  if (idx === -1) return Promise.reject("No encontrado");
  semanas[idx].activo = false;
  return Promise.resolve();
};

// — AsignacionTurno —

export const getAsignacionesPorSemana = (idSemana) =>
  Promise.resolve(
    JSON.parse(JSON.stringify(asignaciones.filter(a => a.idSemana === idSemana)))
  );

export const createAsignacion = (data) => {
  const semana    = semanas.find(s => s.idSemana === Number(data.idSemana));
  const plantilla = plantillasMock.find(p => p.idPlantilla === Number(data.idPlantilla));
  if (!semana || !plantilla) return Promise.reject("Referencia no encontrada");
  const nueva = {
    idAsignacion:    nextAsignacionId++,
    idSemana:        Number(data.idSemana),
    nombreSemana:    semana.nombre,
    idPlantilla:     Number(data.idPlantilla),
    nombrePlantilla: plantilla.nombre,
    diaSemana:       Number(data.diaSemana),
  };
  asignaciones.push(nueva);
  return Promise.resolve(nueva);
};

export const deleteAsignacion = (id) => {
  const idx = asignaciones.findIndex(a => a.idAsignacion === id);
  if (idx === -1) return Promise.reject("No encontrado");
  asignaciones.splice(idx, 1);
  return Promise.resolve();
};

// — Plantillas para select —

export const getPlantillasParaSelect = () =>
  Promise.resolve(JSON.parse(JSON.stringify(plantillasMock)));