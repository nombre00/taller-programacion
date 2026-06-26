// src/data/metodosProducto.js

import { productosDB as productosSeedDB, clientes as baseClientes } from "./dataBase";
// --- Gestión de Productos ---
const DB_KEY = 'productos_storage'; // Clave para productos

// Función de ayuda para OBTENER productos, inicializando si es necesario
const getDB = () => {
  let dbJson = localStorage.getItem(DB_KEY);
  let db;
  try {
    db = dbJson ? JSON.parse(dbJson) : null;
  } catch (e) {
    console.error(`Error al parsear ${DB_KEY} desde localStorage:`, e);
    db = null;
  }

  // Si no hay datos válidos, usa la semilla y guarda
  if (!db || !Array.isArray(db) || db.length === 0) {
    db = productosSeedDB; // Usa la semilla importada
    localStorage.setItem(DB_KEY, JSON.stringify(db));
  }
  return db;
};

// Función de ayuda para GUARDAR productos
const saveDB = (db) => {
  try {
    localStorage.setItem(DB_KEY, JSON.stringify(Array.isArray(db) ? db : []));
  } catch (error) {
     console.error(`Error al guardar ${DB_KEY} en localStorage:`, error);
  }
};

// Listar productos (convierte estructura)
export const listarProductos = () => {
  const productosDB = getDB();
  return productosDB.map(producto => ({
    id: producto.id,
    nombre_producto: producto.nombre,
    categoria_producto: producto.categoria,
    precio_producto: producto.precio,
    descripcion_producto: producto.descripcion,
    imagenSrc: producto.imagen,
    stock_producto: producto.stock || 0
  }));
};

// Agregar producto (convierte y guarda)
export const agregarProducto = (productoData) => {
  const productosDB = getDB();
  const nextId = productosDB.length > 0 ? Math.max(...productosDB.map(p => p.id)) + 1 : 1;
  const nuevoProducto = { // Estructura de guardado
    id: nextId,
    nombre: productoData.nombre_producto,
    categoria: productoData.categoria_producto,
    precio: parseInt(productoData.precio_producto) || 0,
    descripcion: productoData.descripcion_producto,
    stock: parseInt(productoData.stock_producto) || 0,
    imagen: productoData.imagenSrc || "/img/default.png"
  };
  productosDB.push(nuevoProducto);
  saveDB(productosDB); // Guarda usando la función auxiliar
  console.log('Producto agregado a DB:', nuevoProducto);
  // Devuelve estructura para componente
  return {
    id: nuevoProducto.id,
    nombre_producto: nuevoProducto.nombre,
    categoria_producto: nuevoProducto.categoria,
    precio_producto: nuevoProducto.precio,
    descripcion_producto: nuevoProducto.descripcion,
    stock_producto: nuevoProducto.stock,
    imagenSrc: nuevoProducto.imagen
  };
};

// Eliminar producto
export const eliminarProducto = (id) => {
  let productosDB = getDB();
  const longitudInicial = productosDB.length;
  productosDB = productosDB.filter(p => p.id !== id);
  if (productosDB.length < longitudInicial) {
    saveDB(productosDB); // Guarda usando la función auxiliar
    return true;
  }
  return false;
};

// --- Gestión de Clientes ---
const CLIENTES_KEY = 'clientes'; // Clave correcta

// Función auxiliar para OBTENER y COMBINAR clientes
const getClientesDB = () => {
  let storedClientes = [];
  const clientesJson = localStorage.getItem(CLIENTES_KEY);
  try {
    const parsed = clientesJson ? JSON.parse(clientesJson) : [];
    if (Array.isArray(parsed)) storedClientes = parsed;
  } catch (e) { console.error(`Error al parsear ${CLIENTES_KEY}:`, e); }

  let combinedClientes = [...storedClientes];
  const storedCorreos = new Set(storedClientes.map(c => c.correo));
  let needsUpdate = false; // 'needsUpdate' SÍ se usa aquí
  baseClientes.forEach(baseCliente => {
    if (!storedCorreos.has(baseCliente.correo)) {
      combinedClientes.push(baseCliente);
      needsUpdate = true;
    }
  });

  // Guardar si hubo cambios (descomentado para persistencia)
  if (storedClientes.length === 0 || needsUpdate) {
    localStorage.setItem(CLIENTES_KEY, JSON.stringify(combinedClientes));
  }
  return combinedClientes;
};

// Listar clientes
export const listarClientes = () => {
  return getClientesDB();
};

// --- Gestión de Pedidos ---
const PEDIDOS_KEY = 'pedidos_storage';

// Función auxiliar para OBTENER pedidos
const getPedidosDB = () => {
  const pedidosJson = localStorage.getItem(PEDIDOS_KEY);
  let pedidos;
  try {
    pedidos = pedidosJson ? JSON.parse(pedidosJson) : null;
  } catch (e) { console.error("Error al parsear pedidos:", e); pedidos = null; }
  if (!Array.isArray(pedidos)) {
     pedidos = [];
     localStorage.setItem(PEDIDOS_KEY, JSON.stringify(pedidos)); // Inicializa si no existe
  }
  return pedidos;
};

// Función auxiliar para GUARDAR pedidos
const savePedidosDB = (pedidos) => {
  try {
    localStorage.setItem(PEDIDOS_KEY, JSON.stringify(Array.isArray(pedidos) ? pedidos : []));
  } catch (error) {
    console.error(`Error al guardar ${PEDIDOS_KEY} en localStorage:`, error);
  }
};

// Listar pedidos
export const listarPedidos = () => {
  return getPedidosDB();
};

// Agregar pedido
export const agregarPedido = (pedido) => {
  const pedidos = getPedidosDB();
  const nextId = pedidos.length > 0 ? Math.max(...pedidos.map(p => p.id)) + 1 : 1;
  const nuevoPedido = {
    id: nextId,
    fecha: new Date().toLocaleString('es-CL'),
    nombre: pedido.nombre,
    producto: pedido.producto,
    monto: pedido.monto
  };
  pedidos.push(nuevoPedido);
  savePedidosDB(pedidos); // Guarda usando la función auxiliar
  return nuevoPedido;
};

// Eliminar pedido
export const eliminarPedido = (id) => {
  let pedidos = getPedidosDB();
  const longitudInicial = pedidos.length;
  pedidos = pedidos.filter(p => p.id !== id);
  if (pedidos.length < longitudInicial) {
    savePedidosDB(pedidos); // Guarda usando la función auxiliar
    return true;
  }
  return false;
};

// --- (Opcional: Funciones para Usuarios si las necesitas aquí) ---
// const USUARIOS_KEY = 'usuarios';
// export const listarUsuarios = () => { ... };
// etc.