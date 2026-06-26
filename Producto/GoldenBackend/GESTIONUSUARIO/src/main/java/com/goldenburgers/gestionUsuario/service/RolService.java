package com.goldenburgers.gestionUsuario.service;

import com.goldenburgers.gestionUsuario.DTOs.RolDTO;
import com.goldenburgers.gestionUsuario.model.Rol;
import com.goldenburgers.gestionUsuario.repository.RolRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Servicio de negocio para la gestión de Roles
 *
 * Servicio de SOLO LECTURA para consultar roles del sistema.
 * Los roles son datos maestros fijos en la BD (Cliente, Trabajador, Admin).
 *
 * Integraciones:
 * - RolRepository: Consultas de roles en tabla ROLES
 * - EntityMapper: Conversión entre entidades JPA y DTOs
 *
 * Transaccionalidad:
 * - @Transactional(readOnly = true): TODAS las operaciones son de solo lectura
 * - Optimización: readOnly = true mejora performance en consultas
 * - No permite operaciones de escritura (INSERT, UPDATE, DELETE)
 *
 * Datos maestros:
 * - Los roles se insertan manualmente en la BD al inicio
 * - Roles típicos: "Cliente", "Trabajador", "Admin"
 * - NO se crean, actualizan o eliminan roles desde la aplicación
 *
 * Uso en el sistema:
 * - ClienteService: Obtiene rol "Cliente" al registrar clientes
 * - TrabajadorService: Obtiene rol "Trabajador" al registrar trabajadores
 * - Spring Security: Valida roles en @PreAuthorize
 *
 * Llamado desde:
 * - RolController: para procesar las peticiones HTTP
 * - Otros servicios: para obtener roles al crear usuarios
 */

@Service
@Transactional(readOnly = true)
public class RolService {
    // Repository para consultas de roles en tabla ROLES
    @Autowired
    private RolRepository rolRepository;

    // Mapper para convertir entidades JPA a DTOs
    @Autowired
    private EntityMapper entityMapper;

    /**
     * Lista todos los roles disponibles en el sistema
     *
     * Retorna todos los roles configurados como datos maestros.
     * Los roles son fijos y se configuran al inicializar la BD.
     *
     * Integraciones:
     * - rolRepository.findAll(): SELECT * FROM ROLES
     * - EntityMapper.toDTO(): Convierte cada entidad a DTO usando streams
     *
     * Uso típico:
     * - Listar roles disponibles en panel de administración
     * - Validar roles existentes en el sistema
     *
     * Roles típicos retornados:
     * - Cliente: Usuarios que realizan pedidos
     * - Trabajador: Personal que gestiona pedidos
     * - Admin: Administradores con acceso completo
     *
     * @return List<RolDTO> con todos los roles (nunca vacía, siempre hay roles maestros)
     */
    public List<RolDTO> getAllRoles() {
        // Obtiene todos los roles y los convierte a DTOs usando streams
        return rolRepository.findAll()
                .stream()
                .map(entityMapper::toDTO) // Convierte cada Rol a RolDTO
                .collect(Collectors.toList());
    }

    /**
     * Obtiene un rol por su ID de base de datos
     *
     * Busca un rol específico por su clave primaria.
     *
     * Integraciones:
     * - rolRepository.findById(): Busca en tabla ROLES
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Uso típico:
     * - Consultar detalles de un rol específico
     * - Validar que un ID de rol es válido
     *
     * @param idRol ID del rol (clave primaria)
     * @return RolDTO con los datos del rol
     * @throws IllegalArgumentException si el rol no existe (manejado como 400 por GlobalExceptionHandler)
     */
    public RolDTO getRolById(Long idRol) {
        // Busca el rol, si no existe lanza excepción
        Rol rol = rolRepository.findById(idRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + idRol));
        return entityMapper.toDTO(rol);
    }

    /**
     * Obtiene un rol por su nombre
     *
     * Busca un rol por su nombre exacto (case-sensitive).
     * Útil para obtener roles al registrar usuarios (ej: "Cliente", "Trabajador").
     *
     * Integraciones:
     * - rolRepository.findByNombreRol(): Busca en tabla ROLES por nombre
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Uso típico:
     * - ClienteService.registerCliente(): Busca rol "Cliente"
     * - TrabajadorService.registerTrabajador(): Busca rol "Trabajador"
     * - Validar que un nombre de rol existe
     *
     * IMPORTANTE:
     * - El nombre debe coincidir exactamente (case-sensitive)
     * - Nombres válidos: "Cliente", "Trabajador", "Admin" (según datos maestros)
     *
     * @param nombreRol Nombre exacto del rol (ej: "Cliente", "Trabajador", "Admin")
     * @return RolDTO con los datos del rol
     * @throws IllegalArgumentException si el rol no existe (manejado como 400 por GlobalExceptionHandler)
     */
    public RolDTO getRolByNombre(String nombreRol) {
        // Busca el rol por nombre exacto (case-sensitive)
        Rol rol = rolRepository.findByNombreRol(nombreRol)
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado: " + nombreRol));
        return entityMapper.toDTO(rol);
    }
}
