// src/services/recetasService.mock.js

let recetas = [
  {
    idReceta: 1,
    idProducto: 1,
    nombreProducto: "Combo Clásica",
    descripcion: "Hamburguesa clásica con doble cheddar y vegetales frescos",
    costoTotal: 3200,
    activo: true,
    detalles: [
      { idMateriaPrima: 1, nombreMateriaPrima: "Carne de vacuno", cantidad: 0.120, unidadMedida: "kg" },
      { idMateriaPrima: 3, nombreMateriaPrima: "Queso cheddar", cantidad: 0.040, unidadMedida: "kg" },
      { idMateriaPrima: 2, nombreMateriaPrima: "Pan de hamburguesa", cantidad: 1, unidadMedida: "unidad" },
      { idMateriaPrima: 5, nombreMateriaPrima: "Tomate", cantidad: 0.050, unidadMedida: "kg" },
      { idMateriaPrima: 4, nombreMateriaPrima: "Lechuga", cantidad: 0.030, unidadMedida: "kg" },
      { idMateriaPrima: 11, nombreMateriaPrima: "Cebolla morada", cantidad: 0.020, unidadMedida: "kg" },
      { idMateriaPrima: 12, nombreMateriaPrima: "Pepinillos", cantidad: 0.020, unidadMedida: "kg" },
      { idMateriaPrima: 13, nombreMateriaPrima: "Salsa Golden", cantidad: 30, unidadMedida: "ml" },
    ],
  },
  {
    idReceta: 2,
    idProducto: 3,
    nombreProducto: "Champiñon",
    descripcion: "Hamburguesa con champiñones salteados y cebolla caramelizada",
    costoTotal: 3800,
    activo: true,
    detalles: [
      { idMateriaPrima: 1, nombreMateriaPrima: "Carne de vacuno", cantidad: 0.120, unidadMedida: "kg" },
      { idMateriaPrima: 2, nombreMateriaPrima: "Pan de hamburguesa", cantidad: 1, unidadMedida: "unidad" },
      { idMateriaPrima: 14, nombreMateriaPrima: "Queso mantecoso", cantidad: 0.040, unidadMedida: "kg" },
      { idMateriaPrima: 15, nombreMateriaPrima: "Champiñones", cantidad: 0.080, unidadMedida: "kg" },
      { idMateriaPrima: 16, nombreMateriaPrima: "Cebolla caramelizada", cantidad: 0.040, unidadMedida: "kg" },
      { idMateriaPrima: 9, nombreMateriaPrima: "Mayonesa", cantidad: 25, unidadMedida: "ml" },
    ],
  },
  {
    idReceta: 3,
    idProducto: 5,
    nombreProducto: "Golden",
    descripcion: "Hamburguesa premium con tocino y doble cheddar",
    costoTotal: 4100,
    activo: true,
    detalles: [
      { idMateriaPrima: 1, nombreMateriaPrima: "Carne de vacuno", cantidad: 0.120, unidadMedida: "kg" },
      { idMateriaPrima: 2, nombreMateriaPrima: "Pan de hamburguesa", cantidad: 1, unidadMedida: "unidad" },
      { idMateriaPrima: 3, nombreMateriaPrima: "Queso cheddar", cantidad: 0.040, unidadMedida: "kg" },
      { idMateriaPrima: 17, nombreMateriaPrima: "Tocino", cantidad: 0.040, unidadMedida: "kg" },
      { idMateriaPrima: 12, nombreMateriaPrima: "Pepinillos", cantidad: 0.020, unidadMedida: "kg" },
      { idMateriaPrima: 13, nombreMateriaPrima: "Salsa Golden", cantidad: 30, unidadMedida: "ml" },
    ],
  },
];

let nextId = 4;

const delay = (ms = 300) => new Promise((res) => setTimeout(res, ms));

export const obtenerTodasRecetas = async () => {
  await delay();
  return [...recetas];
};

export const obtenerRecetaPorId = async (id) => {
  await delay();
  const receta = recetas.find((r) => r.idReceta === id);
  if (!receta) throw new Error(`Receta ${id} no encontrada`);
  return { ...receta };
};

export const obtenerRecetaPorProducto = async (idProducto) => {
  await delay();
  return recetas.find((r) => r.idProducto === idProducto) || null;
};

export const crearReceta = async (data) => {
  await delay();
  const nueva = {
    idReceta: nextId++,
    idProducto: data.idProducto,
    nombreProducto: `Producto ${data.idProducto}`,
    descripcion: data.descripcion || "",
    costoTotal: 0,
    activo: true,
    detalles: data.detalles.map((d) => ({
      idMateriaPrima: d.idMateriaPrima,
      nombreMateriaPrima: `Materia Prima ${d.idMateriaPrima}`,
      cantidad: d.cantidad,
      unidadMedida: d.unidadMedida,
    })),
  };
  recetas.push(nueva);
  return { ...nueva };
};

export const actualizarReceta = async (id, data) => {
  await delay();
  const idx = recetas.findIndex((r) => r.idReceta === id);
  if (idx === -1) throw new Error(`Receta ${id} no encontrada`);
  recetas[idx] = {
    ...recetas[idx],
    descripcion: data.descripcion,
    detalles: data.detalles.map((d) => ({
      idMateriaPrima: d.idMateriaPrima,
      nombreMateriaPrima: `Materia Prima ${d.idMateriaPrima}`,
      cantidad: d.cantidad,
      unidadMedida: d.unidadMedida,
    })),
  };
  return { ...recetas[idx] };
};

export const desactivarReceta = async (id) => {
  await delay();
  const idx = recetas.findIndex((r) => r.idReceta === id);
  if (idx === -1) throw new Error(`Receta ${id} no encontrada`);
  recetas[idx].activo = false;
  return { ...recetas[idx] };
};