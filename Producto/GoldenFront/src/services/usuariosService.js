import api from "../config/api";

/**
 * Servicio para gestión de Clientes
 * Endpoints del microservicio de Usuarios/Clientes
 * Ruta base: /api/clientes
 */

// ===============================
// CLIENTE - CRUD
// ===============================

/**
 * POST /api/clientes - Registrar un nuevo cliente
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 */
export const registrarCliente = async (clienteData) => {
  try {
    console.log('📤 registrarCliente - Enviando petición POST /clientes');
    console.log('📦 Datos del cliente:', clienteData);

    // Verificar token antes de enviar
    const token = localStorage.getItem("authToken");
    console.log('🔐 Token en localStorage:', !!token);
    if (token) {
      console.log('🔐 Token (primeros 30 chars):', token.substring(0, 30) + '...');
    }

    const response = await api.post("/clientes", clienteData);

    console.log('✅ registrarCliente - Respuesta exitosa:', response.data);
    return response.data;
  } catch (error) {
    console.error("❌ Error al registrar cliente:", error);
    console.error("❌ Error response:", error.response);
    console.error("❌ Error data:", error.response?.data);
    throw error;
  }
};

/**
 * POST /api/clientes/admin - Registrar un nuevo cliente (Solo Admin)
 * Requiere: token JWT válido con rol ADMIN
 * Roles: ADMIN
 */
export const registrarClientePorAdmin = async (clienteData) => {
  try {
    console.log('📤 registrarClientePorAdmin - Enviando petición POST /clientes/admin');
    console.log('📦 Datos del cliente:', clienteData);

    // Verificar token antes de enviar
    const token = localStorage.getItem("authToken");
    console.log('🔐 Token en localStorage:', !!token);
    if (token) {
      console.log('🔐 Token (primeros 30 chars):', token.substring(0, 30) + '...');
    }

    const response = await api.post("/clientes/admin", clienteData);

    console.log('✅ registrarClientePorAdmin - Respuesta exitosa:', response.data);
    return response.data;
  } catch (error) {
    console.error("❌ Error al registrar cliente por admin:", error);
    console.error("❌ Error response:", error.response);
    console.error("❌ Error data:", error.response?.data);
    throw error;
  }
};

/**
 * GET /api/clientes - Listar todos los clientes
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 */
export const obtenerTodosClientes = async () => {
  try {
    const response = await api.get("/clientes");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener clientes:", error);
    throw error;
  }
};

/**
 * GET /api/clientes/{id} - Obtener cliente por ID
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 */
export const obtenerClientePorId = async (idCliente) => {
  try {
    const response = await api.get(`/clientes/${idCliente}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener cliente ${idCliente}:`, error);
    throw error;
  }
};

/**
 * GET /api/clientes/usuario/{idUsuario} - Obtener cliente por Firebase UID
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 * 
 * @param {string} idUsuario - Firebase UID del usuario
 */
export const obtenerClientePorUid = async (firebaseUid) => {
  try {
    const response = await api.get(`/clientes/usuario/${firebaseUid}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener cliente por UID ${firebaseUid}:`, error);
    throw error;
  }
};

/**
 * GET /api/clientes/email/{email} - Obtener cliente por email
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 */
export const obtenerClientePorEmail = async (email) => {
  try {
    const response = await api.get(`/clientes/email/${email}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener cliente por email ${email}:`, error);
    throw error;
  }
};

/**
 * PUT /api/clientes/{id} - Actualizar cliente (nombre y teléfono)
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 * 
 * @param {number} id - ID del cliente
 * @param {string} nombreCliente - Nuevo nombre
 * @param {string} telefonoCliente - Nuevo teléfono (opcional, 9 dígitos)
 */
export const actualizarCliente = async (id, nombreCliente, telefonoCliente = null) => {
  try {
    const params = new URLSearchParams();
    params.append("nombreCliente", nombreCliente);
    if (telefonoCliente) {
      params.append("telefonoCliente", telefonoCliente);
    }
    const response = await api.put(`/clientes/${id}`, null, { params });
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar cliente ${id}:`, error);
    throw error;
  }
};

/**
 * PUT /api/clientes/perfil - Actualizar perfil del cliente autenticado
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 * 
 * El cliente solo puede modificar sus PROPIOS datos
 * El Firebase UID se extrae automáticamente del token JWT
 * 
 * @param {object} perfilData - Datos a actualizar (nombreCliente, email, telefonoCliente)
 */
export const actualizarPerfilCliente = async (perfilData) => {
  try {
    const response = await api.put("/clientes/perfil", perfilData);
    return response.data;
  } catch (error) {
    console.error("Error al actualizar perfil:", error);
    throw error;
  }
};

/**
 * PUT /api/clientes/{id}/email - Actualizar email de un cliente
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 * 
 * IMPORTANTE: Esto solo actualiza el email en la BD local
 * El email en Firebase debe actualizarse desde el frontend usando Firebase SDK
 * 
 * @param {number} id - ID del cliente
 * @param {object} emailData - { email: "nuevo@email.com" }
 */
export const actualizarEmailCliente = async (id, emailData) => {
  try {
    const response = await api.put(`/clientes/${id}/email`, emailData);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar email del cliente ${id}:`, error);
    throw error;
  }
};

/**
 * DELETE /api/clientes/{id} - Eliminar cliente
 * Requiere: token JWT válido
 * Roles: ADMIN (solo)
 * 
 * ADVERTENCIA: Operación irreversible
 * Elimina el cliente y todas sus direcciones asociadas
 * 
 * @param {number} id - ID del cliente a eliminar
 */
export const eliminarCliente = async (id) => {
  try {
    const response = await api.delete(`/clientes/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar cliente ${id}:`, error);
    throw error;
  }
};

// ===============================
// DIRECCIONES DEL CLIENTE
// ===============================

/**
 * POST /api/clientes/direcciones - Crear nueva dirección
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 * 
 * @param {object} direccionData - { idCliente, idCiudad, direccion, alias? }
 */
export const crearDireccion = async (direccionData) => {
  try {
    const response = await api.post("/clientes/direcciones", direccionData);
    return response.data;
  } catch (error) {
    console.error("Error al crear dirección:", error);
    throw error;
  }
};

/**
 * GET /api/clientes/{idCliente}/direcciones - Obtener direcciones de un cliente
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 * 
 * @param {number} idCliente - ID del cliente
 */
export const obtenerDireccionesPorCliente = async (idCliente) => {
  try {
    const response = await api.get(`/clientes/${idCliente}/direcciones`);
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error(`Error al obtener direcciones del cliente ${idCliente}:`, error);
    throw error;
  }
};

/**
 * PUT /api/clientes/direcciones/{idDireccion} - Actualizar dirección
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 * 
 * @param {number} idDireccion - ID de la dirección a actualizar
 * @param {object} direccionData - { idCiudad, direccion, alias? }
 */
export const actualizarDireccion = async (idDireccion, direccionData) => {
  try {
    const response = await api.put(`/clientes/direcciones/${idDireccion}`, direccionData);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar dirección ${idDireccion}:`, error);
    throw error;
  }
};

/**
 * DELETE /api/clientes/direcciones/{idDireccion} - Eliminar dirección
 * Requiere: token JWT válido
 * Roles: Cualquier usuario autenticado
 *
 * @param {number} idDireccion - ID de la dirección a eliminar
 */
export const eliminarDireccion = async (idDireccion) => {
  try {
    const response = await api.delete(`/clientes/direcciones/${idDireccion}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar dirección ${idDireccion}:`, error);
    throw error;
  }
};

// ===============================
// TRABAJADORES - CRUD
// ===============================

/**
 * POST /api/trabajadores - Registrar un nuevo trabajador
 * Requiere: token JWT válido
 * Roles: ADMIN (solo)
 *
 * @param {object} trabajadorData - { idUsuario, email, nombreTrabajador, rutTrabajador }
 */
export const registrarTrabajador = async (trabajadorData) => {
  try {
    const response = await api.post("/trabajadores", trabajadorData);
    return response.data;
  } catch (error) {
    console.error("Error al registrar trabajador:", error);
    throw error;
  }
};

/**
 * GET /api/trabajadores - Listar todos los trabajadores
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 */
export const obtenerTodosTrabajadores = async () => {
  try {
    const response = await api.get("/trabajadores");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener trabajadores:", error);
    throw error;
  }
};

/**
 * GET /api/trabajadores/{id} - Obtener trabajador por ID
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 *
 * @param {number} idTrabajador - ID del trabajador
 */
export const obtenerTrabajadorPorId = async (idTrabajador) => {
  try {
    const response = await api.get(`/trabajadores/${idTrabajador}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener trabajador ${idTrabajador}:`, error);
    throw error;
  }
};

/**
 * GET /api/trabajadores/usuario/{idUsuario} - Obtener trabajador por Firebase UID
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 *
 * @param {string} firebaseUid - Firebase UID del usuario
 */
export const obtenerTrabajadorPorUid = async (firebaseUid) => {
  try {
    const response = await api.get(`/trabajadores/usuario/${firebaseUid}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener trabajador por UID ${firebaseUid}:`, error);
    throw error;
  }
};

/**
 * GET /api/trabajadores/rut/{rut} - Obtener trabajador por RUT
 * Requiere: token JWT válido
 * Roles: ADMIN, TRABAJADOR
 *
 * @param {string} rut - RUT del trabajador (formato: 12.345.678-9)
 */
export const obtenerTrabajadorPorRut = async (rut) => {
  try {
    const response = await api.get(`/trabajadores/rut/${rut}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener trabajador por RUT ${rut}:`, error);
    throw error;
  }
};

/**
 * PUT /api/trabajadores/{id} - Actualizar trabajador (nombre y RUT)
 * Requiere: token JWT válido
 * Roles: ADMIN (solo)
 *
 * @param {number} id - ID del trabajador
 * @param {string} nombreTrabajador - Nuevo nombre
 * @param {string} rutTrabajador - Nuevo RUT
 */
export const actualizarTrabajador = async (id, nombreTrabajador, rutTrabajador) => {
  try {
    const params = new URLSearchParams();
    params.append("nombreTrabajador", nombreTrabajador);
    params.append("rutTrabajador", rutTrabajador);
    const response = await api.put(`/trabajadores/${id}`, null, { params });
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar trabajador ${id}:`, error);
    throw error;
  }
};

/**
 * PUT /api/trabajadores/{id}/email - Actualizar email de un trabajador
 * Requiere: token JWT válido
 * Roles: ADMIN (solo)
 *
 * IMPORTANTE: Esto solo actualiza el email en la BD local
 * El email en Firebase debe actualizarse desde el frontend usando Firebase SDK
 *
 * @param {number} id - ID del trabajador
 * @param {object} emailData - { nuevoEmail: "nuevo@email.com" }
 */
export const actualizarEmailTrabajador = async (id, emailData) => {
  try {
    const response = await api.put(`/trabajadores/${id}/email`, emailData);
    return response.data;
  } catch (error) {
    console.error(`Error al actualizar email del trabajador ${id}:`, error);
    throw error;
  }
};

/**
 * PUT /api/trabajadores/{id}/rol - Cambiar rol de trabajador
 * Requiere: token JWT válido
 * Roles: ADMIN (solo)
 *
 * Permite cambiar entre:
 * - idRol: 2 (Trabajador)
 * - idRol: 3 (Admin)
 *
 * @param {number} id - ID del trabajador
 * @param {object} rolData - { idRol: 2 o 3 }
 */
export const cambiarRolTrabajador = async (id, rolData) => {
  try {
    const response = await api.put(`/trabajadores/${id}/rol`, rolData);
    return response.data;
  } catch (error) {
    console.error(`Error al cambiar rol del trabajador ${id}:`, error);
    throw error;
  }
};

/**
 * DELETE /api/trabajadores/{id} - Eliminar trabajador
 * Requiere: token JWT válido
 * Roles: ADMIN (solo)
 *
 * ADVERTENCIA: Operación irreversible
 * Elimina el trabajador del sistema
 *
 * @param {number} id - ID del trabajador a eliminar
 */
export const eliminarTrabajador = async (id) => {
  try {
    const response = await api.delete(`/trabajadores/${id}`);
    return response.data;
  } catch (error) {
    console.error(`Error al eliminar trabajador ${id}:`, error);
    throw error;
  }
};

// ===============================
// CIUDADES
// ===============================

/**
 * GET /api/ciudades - Listar todas las ciudades
 * Acceso: Público
 *
 * Obtiene lista de todas las ciudades disponibles para direcciones de entrega
 */
export const obtenerTodasCiudades = async () => {
  try {
    const response = await api.get("/ciudades");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener ciudades:", error);
    throw error;
  }
};

/**
 * GET /api/ciudades/{id} - Obtener ciudad por ID
 * Acceso: Público
 *
 * @param {number} idCiudad - ID de la ciudad
 */
export const obtenerCiudadPorId = async (idCiudad) => {
  try {
    const response = await api.get(`/ciudades/${idCiudad}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener ciudad ${idCiudad}:`, error);
    throw error;
  }
};

/**
 * GET /api/ciudades/nombre/{nombre} - Obtener ciudad por nombre
 * Acceso: Público
 *
 * @param {string} nombreCiudad - Nombre de la ciudad
 */
export const obtenerCiudadPorNombre = async (nombreCiudad) => {
  try {
    const response = await api.get(`/ciudades/nombre/${nombreCiudad}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener ciudad por nombre ${nombreCiudad}:`, error);
    throw error;
  }
};

// ===============================
// ROLES
// ===============================

/**
 * GET /api/roles - Listar todos los roles
 * Acceso: Público
 *
 * Obtiene lista de todos los roles del sistema:
 * - idRol: 1 (Cliente)
 * - idRol: 2 (Trabajador)
 * - idRol: 3 (Admin)
 */
export const obtenerTodosRoles = async () => {
  try {
    const response = await api.get("/roles");
    return Array.isArray(response.data) ? response.data : [];
  } catch (error) {
    console.error("Error al obtener roles:", error);
    throw error;
  }
};

/**
 * GET /api/roles/{id} - Obtener rol por ID
 * Acceso: Público
 *
 * @param {number} idRol - ID del rol (1: Cliente, 2: Trabajador, 3: Admin)
 */
export const obtenerRolPorId = async (idRol) => {
  try {
    const response = await api.get(`/roles/${idRol}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener rol ${idRol}:`, error);
    throw error;
  }
};

/**
 * GET /api/roles/nombre/{nombre} - Obtener rol por nombre
 * Acceso: Público
 *
 * @param {string} nombreRol - Nombre del rol (Cliente, Trabajador, Admin)
 */
export const obtenerRolPorNombre = async (nombreRol) => {
  try {
    const response = await api.get(`/roles/nombre/${nombreRol}`);
    return response.data;
  } catch (error) {
    console.error(`Error al obtener rol por nombre ${nombreRol}:`, error);
    throw error;
  }
};
