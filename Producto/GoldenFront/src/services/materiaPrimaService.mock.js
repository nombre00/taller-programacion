// src/services/materiaPrimaService.mock.js
let materiasPrimas = [
  {
    idMateriaPrima: 1,
    nombre: "Carne de vacuno",
    unidadMedida: "kg",
    stockActual: 15.500,
    stockMinimo: 5.000,
    costoUnitarioPromedio: 8500,
    activo: true,
  },
  {
    idMateriaPrima: 2,
    nombre: "Pan de hamburguesa",
    unidadMedida: "unidad",
    stockActual: 80,
    stockMinimo: 20,
    costoUnitarioPromedio: 180,
    activo: true,
  },
  {
    idMateriaPrima: 3,
    nombre: "Queso cheddar",
    unidadMedida: "kg",
    stockActual: 4.200,
    stockMinimo: 5.000,
    costoUnitarioPromedio: 12000,
    activo: true,
  },
  {
    idMateriaPrima: 4,
    nombre: "Lechuga",
    unidadMedida: "kg",
    stockActual: 3.000,
    stockMinimo: 1.000,
    costoUnitarioPromedio: 950,
    activo: true,
  },
  {
    idMateriaPrima: 5,
    nombre: "Tomate",
    unidadMedida: "kg",
    stockActual: 2.500,
    stockMinimo: 1.000,
    costoUnitarioPromedio: 800,
    activo: true,
  },
  {
    idMateriaPrima: 6,
    nombre: "Papa",
    unidadMedida: "kg",
    stockActual: 25.000,
    stockMinimo: 10.000,
    costoUnitarioPromedio: 600,
    activo: true,
  },
  {
    idMateriaPrima: 7,
    nombre: "Aceite vegetal",
    unidadMedida: "l",
    stockActual: 8.000,
    stockMinimo: 10.000,
    costoUnitarioPromedio: 1800,
    activo: true,
  },
  {
    idMateriaPrima: 8,
    nombre: "Salsa de tomate",
    unidadMedida: "ml",
    stockActual: 4500,
    stockMinimo: 1000,
    costoUnitarioPromedio: 3,
    activo: true,
  },
  {
    idMateriaPrima: 9,
    nombre: "Mayonesa",
    unidadMedida: "ml",
    stockActual: 3200,
    stockMinimo: 1000,
    costoUnitarioPromedio: 4,
    activo: true,
  },
  {
    idMateriaPrima: 10,
    nombre: "Cebolla",
    unidadMedida: "kg",
    stockActual: 0.800,
    stockMinimo: 2.000,
    costoUnitarioPromedio: 500,
    activo: false,
  },
  // ── Nuevas — requeridas por las recetas ──────────────────────────────────
  {
    idMateriaPrima: 11,
    nombre: "Cebolla morada",
    unidadMedida: "kg",
    stockActual: 2.000,
    stockMinimo: 0.500,
    costoUnitarioPromedio: 600,
    activo: true,
  },
  {
    idMateriaPrima: 12,
    nombre: "Pepinillos",
    unidadMedida: "kg",
    stockActual: 1.500,
    stockMinimo: 0.500,
    costoUnitarioPromedio: 1200,
    activo: true,
  },
  {
    idMateriaPrima: 13,
    nombre: "Salsa Golden",
    unidadMedida: "ml",
    stockActual: 2000,
    stockMinimo: 500,
    costoUnitarioPromedio: 5,
    activo: true,
  },
  {
    idMateriaPrima: 14,
    nombre: "Queso mantecoso",
    unidadMedida: "kg",
    stockActual: 3.000,
    stockMinimo: 1.000,
    costoUnitarioPromedio: 9500,
    activo: true,
  },
  {
    idMateriaPrima: 15,
    nombre: "Champiñones",
    unidadMedida: "kg",
    stockActual: 2.500,
    stockMinimo: 1.000,
    costoUnitarioPromedio: 4500,
    activo: true,
  },
  {
    idMateriaPrima: 16,
    nombre: "Cebolla caramelizada",
    unidadMedida: "kg",
    stockActual: 1.000,
    stockMinimo: 0.500,
    costoUnitarioPromedio: 1800,
    activo: true,
  },
  {
    idMateriaPrima: 17,
    nombre: "Tocino",
    unidadMedida: "kg",
    stockActual: 0.400,
    stockMinimo: 1.000, // stock bajo → ⚠️
    costoUnitarioPromedio: 11000,
    activo: true,
  },
];

let nextId = 18;

const delay = (ms = 300) => new Promise((res) => setTimeout(res, ms));

export const obtenerTodasMateriasPrimas = async () => {
  await delay();
  return [...materiasPrimas];
};

export const obtenerMateriaPrimaPorId = async (id) => {
  await delay();
  const mp = materiasPrimas.find((m) => m.idMateriaPrima === id);
  if (!mp) throw new Error(`Materia prima ${id} no encontrada`);
  return { ...mp };
};

export const crearMateriaPrima = async (data) => {
  await delay();
  const nueva = {
    idMateriaPrima: nextId++,
    nombre: data.nombre,
    unidadMedida: data.unidadMedida,
    stockActual: data.stockActual,
    stockMinimo: data.stockMinimo,
    costoUnitarioPromedio: 0,
    activo: true,
  };
  materiasPrimas.push(nueva);
  return { ...nueva };
};

export const actualizarMateriaPrima = async (id, data) => {
  await delay();
  const idx = materiasPrimas.findIndex((m) => m.idMateriaPrima === id);
  if (idx === -1) throw new Error(`Materia prima ${id} no encontrada`);
  materiasPrimas[idx] = { ...materiasPrimas[idx], ...data };
  return { ...materiasPrimas[idx] };
};

export const desactivarMateriaPrima = async (id) => {
  await delay();
  const idx = materiasPrimas.findIndex((m) => m.idMateriaPrima === id);
  if (idx === -1) throw new Error(`Materia prima ${id} no encontrada`);
  materiasPrimas[idx].activo = false;
  return { ...materiasPrimas[idx] };
};