import api from "../config/api";

/**
 * Servicio para gestión de Productos
 * Endpoints del microservicio de Catálogo
 * Ruta base: /api/productos
 */

// ===============================
// PRODUCTO - CRUD
// ===============================

/**
 * GET /api/catalogo/productos/todos - Obtener TODOS los productos (incluidos no disponibles)
 * Requiere: token JWT válido
 * Roles: Todos
 */
export const obtenerTodosProductos = async () => {
  try {
    // Este endpoint retorna TODOS los productos, disponibles y no disponibles
    const response = await api.get("/catalogo/productos/todos");
    console.log('📦 Todos los productos obtenidos del backend:', response.data);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener todos los productos:", error);
    console.error("Status:", error.response?.status);
    console.error("URL intentada:", error.config?.url);
    throw error;
  }
};

/**
 * GET /api/productos/{id} - Obtener producto por ID
 * Requiere: token JWT válido
 * Roles: Todos
 */
export const obtenerProductoPorId = async (idProducto) => {
  try {
    const response = await api.get(`/productos/${idProducto}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener producto ${idProducto}:`, error);
    throw error;
  }
};

/**
 * GET /api/productos/categoria/{idCategoria} - Obtener productos por categoría
 * Requiere: token JWT válido
 * Roles: Todos
 */
export const obtenerProductosPorCategoria = async (idCategoria) => {
  try {
    const response = await api.get(`/productos/categoria/${idCategoria}`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener productos de categoría ${idCategoria}:`, error);
    throw error;
  }
};

/**
 * GET /api/catalogo/productos - Obtener productos disponibles
 * Endpoint público según SecurityConfig (permitAll)
 * Retorna: Array de productos con campos en formato backend
 */
export const obtenerProductosDisponibles = async () => {
  try {
    const response = await api.get("/catalogo/productos");
    console.log('📦 Productos obtenidos del backend:', response.data);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener productos disponibles:", error);
    console.error("Status:", error.response?.status);
    throw error;
  }
};

/**
 * POST /api/catalogo/productos - Crear un nuevo producto
 * Requiere: token JWT válido
 * Roles: ADMIN
 */
export const crearProducto = async (productoData) => {
  try {
    const response = await api.post("/catalogo/productos", productoData);
    console.log('✅ Producto creado:', response.data);
    return response.data;
  } catch (error) {
    console.error("❌ Error al crear producto:", error);
    console.error("Status:", error.response?.status);
    console.error("Mensaje:", error.response?.data?.message || error.message);
    throw error;
  }
};

/**
 * PUT /api/catalogo/productos/{id} - Actualizar un producto
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 */
export const actualizarProducto = async (idProducto, productoData) => {
  try {
    const response = await api.put(`/catalogo/productos/${idProducto}`, productoData);
    console.log('✅ Producto actualizado:', response.data);
    return response.data;
  } catch (error) {
    console.error(`❌ Error al actualizar producto ${idProducto}:`, error);
    console.error("Status:", error.response?.status);
    console.error("Mensaje:", error.response?.data?.message || error.message);
    throw error;
  }
};

/**
 * DELETE /api/catalogo/productos/{id} - Eliminar un producto
 * Requiere: token JWT válido
 * Roles: ADMIN
 */
export const eliminarProducto = async (idProducto) => {
  try {
    const response = await api.delete(`/catalogo/productos/${idProducto}`);
    console.log('Producto eliminado:', idProducto);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar producto ${idProducto}:`, error);
    throw error;
  }
};

/**
 * POST /api/catalogo/productos/{id}/imagen - Subir imagen del producto a Firebase
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 * @param {number} idProducto - ID del producto
 * @param {File} file - Archivo de imagen (jpg, png, webp, etc.)
 * @returns {Promise<{imageUrl: string, mensaje: string}>} URL de la imagen en Firebase
 */
export const subirImagenProducto = async (idProducto, file) => {
  try {
    if (!file || !file.type.startsWith('image/')) {
      throw new Error('El archivo debe ser una imagen');
    }

    const formData = new FormData();
    formData.append('imagen', file);

    console.log('🔍 DEBUGGING DETALLADO:');
    console.log('📤 File name:', file.name);
    console.log('📤 File type:', file.type);
    console.log('📤 File size:', file.size);

    // Verificar FormData
    for (let pair of formData.entries()) {
      console.log(`📤 FormData: ${pair[0]} =`, pair[1]);
    }

    // Usar la API configurada en lugar de fetch directo
    // Esto asegura que funcione tanto en local como en producción (S3)
    const response = await api.post(`/catalogo/productos/${idProducto}/imagen`, formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });

    console.log('✅ Success:', response.data);
    return response.data;

  } catch (error) {
    console.error('❌ ERROR COMPLETO:', error);
    throw error;
  }
};

/**
 * PATCH /api/catalogo/productos/{id}/disponibilidad - Cambiar disponibilidad del producto
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 * @param {number} idProducto - ID del producto
 * @param {boolean} disponible - Nueva disponibilidad
 */
export const cambiarDisponibilidad = async (idProducto, disponible) => {
  try {
    const response = await api.patch(
      `/catalogo/productos/${idProducto}/disponibilidad`,
      null,
      {
        params: { disponible }
      }
    );
    console.log('Disponibilidad actualizada:', disponible);
    return response.data;
  } catch (error) {
    console.error(`Error al cambiar disponibilidad del producto ${idProducto}:`, error);
    throw error;
  }
};



