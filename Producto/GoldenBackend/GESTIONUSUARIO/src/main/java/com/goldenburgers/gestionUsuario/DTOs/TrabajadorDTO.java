package com.goldenburgers.gestionUsuario.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Trabajador
 *
 * DTO (Data Transfer Object) utilizado para retornar información completa de un trabajador.
 * Combina datos del usuario base con información específica del trabajador.
 *
 * Uso en el sistema:
 * - Retornado por TrabajadorController en todas las operaciones CRUD
 * - Retornado al registrar nuevo trabajador (POST /api/trabajadores) - Solo ADMIN
 * - Retornado al consultar trabajadores (GET /api/trabajadores) - ADMIN y TRABAJADOR
 * - Usado en asignación de pedidos y reportes
 *
 * Estructura de datos:
 * - Incluye información base del usuario (UsuarioDTO): Firebase UID, email, rol, fecha creación
 * - Incluye información específica del trabajador: nombre, RUT
 *
 * Conversión desde entidad:
 * - EntityMapper.toDTO(Trabajador): Convierte entidad JPA a este DTO
 * - Incluye conversión anidada: Usuario -> UsuarioDTO (con Rol -> RolDTO)
 *
 * Integración con:
 * - TrabajadorService: Retorna este DTO en todas sus operaciones
 * - TrabajadorController: Retorna este DTO en respuestas HTTP
 * - Módulo de Pedidos: Puede usar este DTO para asignación de trabajadores
 *
 * Restricciones de seguridad:
 * - CREAR: Solo ADMIN (@PreAuthorize("hasRole('ADMIN')"))
 * - LEER: ADMIN y TRABAJADOR (@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')"))
 * - ACTUALIZAR: Solo ADMIN (@PreAuthorize("hasRole('ADMIN')"))
 * - ELIMINAR: Solo ADMIN (@PreAuthorize("hasRole('ADMIN')"))
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorDTO {

    /**
     * ID único del trabajador (clave primaria)
     *
     * Descripción:
     * - Identificador único del trabajador en tabla TRABAJADORES
     * - Generado automáticamente por la BD (secuencia)
     * - Usado para operaciones CRUD sobre trabajadores
     * - Usado en asignación de pedidos
     *
     * Ejemplo: 1L, 2L, 3L
     */
    private Long idTrabajador;

    /**
     * Información base del usuario (DTO anidado)
     *
     * Descripción:
     * - Contiene datos comunes a todos los usuarios
     * - Incluye: Firebase UID, email, rol, fecha creación
     * - Convertido desde entidad Usuario mediante EntityMapper
     * - El rol siempre será "Trabajador" para este DTO
     *
     * Estructura:
     * {
     *   idUsuario: "xyz789abc123",
     *   email: "trabajador@empresa.com",
     *   rol: {idRol: 2, nombreRol: "Trabajador"},
     *   fechaCreacion: "2025-01-15"
     * }
     */
    private UsuarioDTO usuario;

    /**
     * Nombre completo del trabajador
     *
     * Descripción:
     * - Nombre del trabajador para identificación interna
     * - Se muestra en asignación de pedidos, reportes
     * - Campo obligatorio al registrar trabajador
     * - Se almacena en tabla TRABAJADORES
     *
     * Ejemplo: "Pedro López Martínez"
     */
    private String nombreTrabajador;

    /**
     * RUT (Rol Único Tributario) del trabajador - Identificador nacional chileno
     *
     * Descripción:
     * - Identificador único nacional (Chile)
     * - Usado para identificación legal del trabajador
     * - Es único en el sistema (validado en servicio)
     * - Campo obligatorio al registrar trabajador
     * - Se almacena en tabla TRABAJADORES
     *
     * Formato: rut chileno
     * - 1-2 dígitos, punto, 3 dígitos, punto, 3 dígitos, guión, dígito verificador
     * - Dígito verificador puede ser 0-9 o K/k
     *
     * Ejemplos: "12.345.678-9", "9.876.543-K", "1.234.567-0"
     */
    private String rutTrabajador;
}
