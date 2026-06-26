// src/services/planificacionService.mock.js
//
// Dos calendarios para junio 2026, ambos con "Semana Estándar".
// Semana 1: 15–21 jun (lun–dom) — mayormente confirmados
// Semana 2: 22–28 jun (lun–dom) — mix confirmados/pendientes
//
// Plantillas:
//   1 — Turno Cocinero Tarde  (Cocinero, 14:00–21:00)
//   2 — Turno Cajero Tarde    (Cajero,   14:00–21:00)
//   3 — Turno Cocinero Noche  (Cocinero, 21:00–02:00)
//   4 — Turno Limpieza Tarde  (Limpieza, 14:00–21:00)
//
// Trabajadores:
//   1 Carlos Rojas    Cocinero  #FF9800
//   2 Diego Fuentes   Cocinero  #FF9800
//   3 Sofía Morales   Cocinero  #FF9800
//   4 Valentina Cruz  Cajero    #4CAF50
//   5 Isabel Pinto    Limpieza  #9C27B0

const semanasMock = [
  { idSemana: 1, nombre: "Semana Estándar", descripcion: "Turnos de tarde lun–vie, noche jue–sáb, limpieza mar/jue/sáb", activo: true },
];

const trabajadoresMock = [
  { idTrabajador: 1, nombre: "Carlos Rojas",   activo: true, idPosicion: 1, nombrePosicion: "Cocinero" },
  { idTrabajador: 2, nombre: "Diego Fuentes",  activo: true, idPosicion: 1, nombrePosicion: "Cocinero" },
  { idTrabajador: 3, nombre: "Sofía Morales",  activo: true, idPosicion: 1, nombrePosicion: "Cocinero" },
  { idTrabajador: 4, nombre: "Valentina Cruz", activo: true, idPosicion: 2, nombrePosicion: "Cajero"   },
  { idTrabajador: 5, nombre: "Isabel Pinto",   activo: true, idPosicion: 3, nombrePosicion: "Limpieza" },
];

let calendarios = [
  { idCalendario: 1, idSemana: 1, nombreSemana: "Semana Estándar", fechaInicio: "2026-06-15", fechaFin: "2026-06-21", repeticionAnual: false },
  { idCalendario: 2, idSemana: 1, nombreSemana: "Semana Estándar", fechaInicio: "2026-06-22", fechaFin: "2026-06-28", repeticionAnual: false },
];

const CF = "#FF9800";
const CA = "#4CAF50";
const CL = "#9C27B0";

const mkH = (id, trab, slot, pos, color, plant, plantNombre, ini, ter, asig, fecha, estado) => ({
  idHorario:        id,
  idTrabajador:     trab ? trab.idTrabajador : null,
  nombreTrabajador: trab ? trab.nombre       : null,
  idSlot:           slot,
  nombreSlot:       `Slot ${plantNombre}`,
  idPosicion:       pos,
  nombrePosicion:   pos === 1 ? "Cocinero" : pos === 2 ? "Cajero" : "Limpieza",
  colorPosicion:    color,
  idPlantilla:      plant,
  nombrePlantilla:  plantNombre,
  horaInicio:       ini,
  horaTermino:      ter,
  idAsignacion:     asig,
  fechaTrabajo:     fecha,
  estado,
});

const T = (id) => trabajadoresMock.find(t => t.idTrabajador === id);

let horarios = [
  // ─── SEMANA 1 (15–21 jun) ────────────────────────────────────────────────

  // Lunes 15 — 2x Cocinero Tarde + 1x Cajero Tarde
  mkH( 1, T(1), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  1, "2026-06-15", "confirmado"),
  mkH( 2, T(2), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  2, "2026-06-15", "confirmado"),
  mkH( 3, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00",  3, "2026-06-15", "confirmado"),

  // Martes 16 — 2x Cocinero Tarde + 1x Cajero Tarde + 1x Limpieza Tarde
  mkH( 4, T(1), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  4, "2026-06-16", "confirmado"),
  mkH( 5, T(3), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  5, "2026-06-16", "confirmado"),
  mkH( 6, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00",  6, "2026-06-16", "confirmado"),
  mkH( 7, T(5), 4, 3, CL, 4, "Turno Limpieza Tarde", "14:00","21:00",  7, "2026-06-16", "confirmado"),

  // Miércoles 17 — 2x Cocinero Tarde + 1x Cajero Tarde
  mkH( 8, T(2), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  8, "2026-06-17", "confirmado"),
  mkH( 9, T(3), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  9, "2026-06-17", "confirmado"),
  mkH(10, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00", 10, "2026-06-17", "confirmado"),

  // Jueves 18 — 2x Cocinero Tarde + 1x Cajero Tarde + 1x Limpieza Tarde + 2x Cocinero Noche
  mkH(11, T(2), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 11, "2026-06-18", "confirmado"),
  mkH(12, T(3), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 12, "2026-06-18", "confirmado"),
  mkH(13, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00", 13, "2026-06-18", "confirmado"),
  mkH(14, T(5), 4, 3, CL, 4, "Turno Limpieza Tarde", "14:00","21:00", 14, "2026-06-18", "confirmado"),
  mkH(15, T(1), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 15, "2026-06-18", "confirmado"),
  mkH(16, T(3), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 16, "2026-06-18", "confirmado"),

  // Viernes 19 — 2x Cocinero Tarde + 1x Cajero Tarde + 2x Cocinero Noche
  mkH(17, T(2), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 17, "2026-06-19", "confirmado"),
  mkH(18, T(3), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 18, "2026-06-19", "confirmado"),
  mkH(19, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00", 19, "2026-06-19", "confirmado"),
  mkH(20, T(1), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 20, "2026-06-19", "confirmado"),
  mkH(21, T(3), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 21, "2026-06-19", "confirmado"),

  // Sábado 20 — 2x Cocinero Noche + 1x Limpieza Tarde
  mkH(22, T(1), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 22, "2026-06-20", "confirmado"),
  mkH(23, T(3), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 23, "2026-06-20", "confirmado"),
  mkH(24, T(5), 4, 3, CL, 4, "Turno Limpieza Tarde", "14:00","21:00", 24, "2026-06-20", "confirmado"),

  // ─── SEMANA 2 (22–28 jun) — mix confirmados/pendientes ──────────────────

  // Lunes 22 — Diego pendiente
  mkH(25, T(1), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  1, "2026-06-22", "confirmado"),
  mkH(26, null, 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  2, "2026-06-22", "pendiente"),
  mkH(27, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00",  3, "2026-06-22", "confirmado"),

  // Martes 23 — Valentina pendiente
  mkH(28, T(1), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  4, "2026-06-23", "confirmado"),
  mkH(29, T(3), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  5, "2026-06-23", "confirmado"),
  mkH(30, null, 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00",  6, "2026-06-23", "pendiente"),
  mkH(31, T(5), 4, 3, CL, 4, "Turno Limpieza Tarde", "14:00","21:00",  7, "2026-06-23", "confirmado"),

  // Miércoles 24 — Sofía pendiente
  mkH(32, T(2), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  8, "2026-06-24", "confirmado"),
  mkH(33, null, 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00",  9, "2026-06-24", "pendiente"),
  mkH(34, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00", 10, "2026-06-24", "confirmado"),

  // Jueves 25 — Cocinero Noche pendiente
  mkH(35, T(2), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 11, "2026-06-25", "confirmado"),
  mkH(36, T(3), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 12, "2026-06-25", "confirmado"),
  mkH(37, T(4), 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00", 13, "2026-06-25", "confirmado"),
  mkH(38, T(5), 4, 3, CL, 4, "Turno Limpieza Tarde", "14:00","21:00", 14, "2026-06-25", "confirmado"),
  mkH(39, null, 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 15, "2026-06-25", "pendiente"),
  mkH(40, T(3), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 16, "2026-06-25", "confirmado"),

  // Viernes 26 — Cajero Tarde y Cocinero Noche pendientes
  mkH(41, T(2), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 17, "2026-06-26", "confirmado"),
  mkH(42, T(3), 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 18, "2026-06-26", "confirmado"),
  mkH(43, null, 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00", 19, "2026-06-26", "pendiente"),
  mkH(44, T(1), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 20, "2026-06-26", "confirmado"),
  mkH(45, null, 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 21, "2026-06-26", "pendiente"),

  // Sábado 27 — Limpieza pendiente
  mkH(46, T(1), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 22, "2026-06-27", "confirmado"),
  mkH(47, T(3), 3, 1, CF, 3, "Turno Cocinero Noche", "21:00","02:00", 23, "2026-06-27", "confirmado"),
  mkH(48, null, 4, 3, CL, 4, "Turno Limpieza Tarde", "14:00","21:00", 24, "2026-06-27", "pendiente"),
];

let nextCalendarioId = 3;
let nextHorarioId    = 49;

// — CalendarioSemana —

export const getCalendarios = () =>
  Promise.resolve(JSON.parse(JSON.stringify(calendarios)));

export const createCalendario = (data) => {
  const semana = semanasMock.find(s => s.idSemana === Number(data.idSemana));
  if (!semana) return Promise.reject("Semana no encontrada");
  const nuevo = {
    idCalendario: nextCalendarioId++,
    idSemana:     Number(data.idSemana),
    nombreSemana: semana.nombre,
    fechaInicio:  data.fechaInicio,
    fechaFin:     data.fechaFin,
    repeticionAnual: false,
  };
  const fecha1 = data.fechaInicio;
  const fecha2 = (() => {
    const d = new Date(data.fechaInicio + "T00:00:00");
    d.setDate(d.getDate() + 1);
    return d.toISOString().split("T")[0];
  })();
  horarios.push(
    mkH(nextHorarioId++, null, 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 1, fecha1, "pendiente"),
    mkH(nextHorarioId++, null, 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 2, fecha1, "pendiente"),
    mkH(nextHorarioId++, null, 2, 2, CA, 2, "Turno Cajero Tarde",   "14:00","21:00", 3, fecha1, "pendiente"),
    mkH(nextHorarioId++, null, 1, 1, CF, 1, "Turno Cocinero Tarde", "14:00","21:00", 4, fecha2, "pendiente"),
    mkH(nextHorarioId++, null, 4, 3, CL, 4, "Turno Limpieza Tarde", "14:00","21:00", 7, fecha2, "pendiente"),
  );
  calendarios.push(nuevo);
  return Promise.resolve(nuevo);
};

export const updateCalendario = (id, data) => {
  const idx = calendarios.findIndex(c => c.idCalendario === id);
  if (idx === -1) return Promise.reject("Calendario no encontrado");
  const semana = semanasMock.find(s => s.idSemana === Number(data.idSemana));
  calendarios[idx] = {
    ...calendarios[idx],
    idSemana:     Number(data.idSemana),
    nombreSemana: semana ? semana.nombre : calendarios[idx].nombreSemana,
    fechaInicio:  data.fechaInicio,
    fechaFin:     data.fechaFin,
  };
  return Promise.resolve(calendarios[idx]);
};

export const deleteCalendario = (id) => {
  const idx = calendarios.findIndex(c => c.idCalendario === id);
  if (idx === -1) return Promise.reject("Calendario no encontrado");
  calendarios.splice(idx, 1);
  return Promise.resolve();
};

// — HorarioTrabajador —

export const getHorariosPorCalendario = (idCalendario) => {
  const calendario = calendarios.find(c => c.idCalendario === idCalendario);
  if (!calendario) return Promise.resolve([]);
  const inicio = new Date(calendario.fechaInicio + "T00:00:00");
  const fin    = new Date(calendario.fechaFin    + "T00:00:00");
  const resultado = horarios.filter(hr => {
    const fecha = new Date(hr.fechaTrabajo + "T00:00:00");
    return fecha >= inicio && fecha <= fin;
  });
  return Promise.resolve(JSON.parse(JSON.stringify(resultado)));
};

export const asignarTrabajador = (idHorario, idTrabajador) => {
  const idx = horarios.findIndex(hr => hr.idHorario === idHorario);
  if (idx === -1) return Promise.reject("Horario no encontrado");
  const trabajador = trabajadoresMock.find(t => t.idTrabajador === idTrabajador);
  if (!trabajador) return Promise.reject("Trabajador no encontrado");
  horarios[idx].idTrabajador     = trabajador.idTrabajador;
  horarios[idx].nombreTrabajador = trabajador.nombre;
  horarios[idx].estado           = "confirmado";
  return Promise.resolve(JSON.parse(JSON.stringify(horarios[idx])));
};

export const deleteHorario = (idHorario) => {
  const idx = horarios.findIndex(hr => hr.idHorario === idHorario);
  if (idx === -1) return Promise.reject("Horario no encontrado");
  if (horarios[idx].estado !== "pendiente") return Promise.reject("Solo se pueden eliminar horarios pendientes");
  horarios.splice(idx, 1);
  return Promise.resolve();
};

// — SemanaTipos —

export const getSemanasParaSelect = () =>
  Promise.resolve(JSON.parse(JSON.stringify(semanasMock)));

// — TrabajadoresLocal —

export const getTrabajadoresActivos = () =>
  Promise.resolve(JSON.parse(JSON.stringify(trabajadoresMock.filter(t => t.activo))));

// — Todos los horarios (para CalendarioTurnos) —

export const getTodosLosHorarios = () =>
  Promise.resolve(JSON.parse(JSON.stringify(horarios)));