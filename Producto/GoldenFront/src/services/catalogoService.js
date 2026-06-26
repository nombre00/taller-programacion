// ====================================================================
// SERVICIO DE CATÁLOGO (CLIENTE)
// ====================================================================
//
// Este archivo permite al frontend obtener productos desde el backend
// (Oracle Database) en vez de usar datos hardcodeados.
//
// ====================================================================

import api from "../config/api";// Configuración de Axios para llamadas HTTP
//axios sirve para hacer peticiones HTTP al backend
/**
 * Obtener productos disponibles del catálogo
 * 
 * antes :import { productosDB } from '../../data/dataBase.js'
 */
// GET /api/catalogo/productos - Obtener todos los productos disponibles
export const obtenerProductosDisponibles = async () => {
  try {
    // Hacer petición GET al microservicio de catálogo
    const response = await api.get("/catalogo/productos");
    
    console.log('Productos obtenidos desde Oracle Database:', response.data);
    
    // Verificar que sea un array válido
    return Array.isArray(response.data) ? response.data : [];
    
  } catch (error) {
    console.error(" Error al obtener productos:", error);
    console.error("Status HTTP:", error.response?.status);
    console.error("Mensaje:", error.message);
    
    // Si hay error, retornar array vacío para no romper la página
    return [];
  }
};

/**
 * Obtener un producto específico por ID
 */
export const obtenerProductoPorId = async (idProducto) => {
  try {
    const response = await api.get(`/catalogo/productos/${idProducto}`);
    console.log(`Producto ${idProducto} obtenido:`, response.data);
    return response.data;
  } catch (error) {
    console.error(` Error al obtener producto ${idProducto}:`, error);
    return null;
  }
};


//Obtener productos por categoría
 //esta función hace una petición GET al backend y
 // devuelve los productos que pertenecen a una categoría específica
export const obtenerProductosPorCategoria = async (idCategoria) => {// recibe el id de la categoría
  try {
    const response = await api.get(`/catalogo/productos/categoria/${idCategoria}`);// hace la petición GET al endpoint correspondiente
    console.log(`Productos de categoría ${idCategoria}:`, response.data);// muestra en consola los productos obtenidos
    return Array.isArray(response.data) ? response.data : [];// devuelve un array de productos o un array vacío si no hay productos
  } catch (error) {// si hay un error en la petición
    console.error(` Error al obtener productos de categoría:`, error);// muestra el error en consola
    return [];// devuelve un array vacío
  }
};

/**
 * Buscar productos por nombre
 */
export const buscarProductosPorNombre = async (nombre) => {
  try {
    const response = await api.get(`/catalogo/productos/buscar`, {
      params: { nombre }
    });
    console.log(`Búsqueda "${nombre}":`, response.data);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(` Error al buscar productos:`, error);
    return [];
  }
};

/**
 * Obtener todas las categorías
 */
//el flujo es similar al de obtener productos
//porque ambas funciones hacen peticiones GET al backend
export const obtenerCategorias = async () => {
  try {
    const response = await api.get("/catalogo/categorias");
    console.log('Categorías obtenidas:', response.data);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(" Error al obtener categorías:", error);
    return [];
  }
};