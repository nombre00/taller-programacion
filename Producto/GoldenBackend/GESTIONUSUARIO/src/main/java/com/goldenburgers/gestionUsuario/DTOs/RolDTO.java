package com.goldenburgers.gestionUsuario.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Rol
 *
 * DTO (Data Transfer Object) utilizado para retornar información de roles del sistema.
 * Los roles son datos maestros que definen permisos y accesos.
 *
 * Uso en el sistema:
 * - Retornado por RolController al consultar roles
 * - Usado en UsuarioDTO para mostrar el rol de un usuario
 * - Los roles determinan las restricciones de seguridad (@PreAuthorize)
 *
 * Roles típicos del sistema:
 * - Cliente: Usuario final que realiza pedidos
 * - Trabajador: Personal que gestiona pedidos y clientes
 * - Admin: Administrador con acceso completo al sistema
 *
 * Conversión desde entidad:
 * - EntityMapper.toDTO(Rol): Convierte entidad JPA Rol a este DTO
 *
 * Integración con:
 * - RolService: Retorna este DTO en todas sus operaciones
 * - RolController: Retorna este DTO en respuestas HTTP
 * - UsuarioDTO: Incluye este DTO como campo anidado
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RolDTO {

    /**
     * ID único del rol (clave primaria)
     *
     * Descripción:
     * - Identificador único del rol en tabla ROLES
     * - Generado automáticamente por la BD (secuencia)
     * - Usado como FK en tabla USUARIOS
     *
     */
    private Long idRol;

    /**
     * Nombre del rol
     *
     * Descripción:
     * - Nombre descriptivo del rol
     * - Usado en anotaciones @PreAuthorize para control de acceso
     * - Es único en el sistema
     * - Case-sensitive al buscar por nombre
     *
     * Valores típicos: "Cliente", "Trabajador", "Admin"
     */
    private String nombreRol;
}
