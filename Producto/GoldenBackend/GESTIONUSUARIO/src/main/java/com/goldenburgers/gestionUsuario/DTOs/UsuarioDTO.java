package com.goldenburgers.gestionUsuario.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO de respuesta para Usuario
 *
 * DTO (Data Transfer Object) utilizado para retornar información base de usuarios.
 * Un Usuario es la entidad base que se vincula con Firebase Authentication.
 *
 * Uso en el sistema:
 * - Incluido en ClienteDTO y TrabajadorDTO como información base del usuario
 * - Contiene datos comunes a todos los tipos de usuarios
 * - Vincula usuarios locales con Firebase Authentication
 *
 * Relación con otras entidades:
 * - Cliente: Extiende Usuario con información específica de clientes
 * - Trabajador: Extiende Usuario con información específica de trabajadores
 * - Rol: Define permisos y accesos del usuario
 *
 * Conversión desde entidad:
 * - EntityMapper.toDTO(Usuario): Convierte entidad JPA a este DTO
 * - Incluye conversión anidada de Rol a RolDTO
 *
 * Integración con:
 * - Firebase Authentication: idUsuario corresponde a Firebase UID
 * - Spring Security: rol determina permisos (@PreAuthorize)
 * - ClienteDTO/TrabajadorDTO: Incluyen este DTO como campo usuario
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UsuarioDTO {

    /**
     * ID único del usuario (Firebase UID)
     *
     * Descripción:
     * - ID obtenido de Firebase Authentication al registrar usuario
     * - Usado como clave primaria en tabla USUARIOS
     * - Permite vincular usuario local con Firebase Auth
     * - Es único en el sistema (validado al crear usuario)
     * - Usado para autenticación y autorización
     *
     * Ejemplo: "abc123xyz456def789"
     */
    private String idUsuario;

    /**
     * Rol del usuario (DTO anidado)
     *
     * Descripción:
     * - Define permisos y accesos del usuario
     * - Convertido desde entidad Rol mediante EntityMapper
     * - Usado por Spring Security para validar permisos
     * - Determina qué endpoints puede acceder (@PreAuthorize)
     *
     * Roles posibles:
     * - Cliente: Usuarios finales que realizan pedidos
     * - Trabajador: Personal que gestiona pedidos y clientes
     * - Admin: Administradores con acceso completo
     *
     * Ejemplo: {idRol: 1, nombreRol: "Cliente"}
     */
    private RolDTO rol;

    /**
     * Email del usuario
     *
     * Descripción:
     * - Email registrado en Firebase Authentication
     * - Es único en el sistema (validado al crear usuario)
     * - Usado para comunicación con el usuario
     * - Usado para autenticación (login)
     *
     * Ejemplo: "usuario@ejemplo.com"
     */
    private String email;

    /**
     * Fecha de creación del usuario
     *
     * Descripción:
     * - Fecha en que se registró el usuario en el sistema local
     * - Se establece automáticamente al crear usuario (LocalDate.now())
     * - Usado para auditoría y estadísticas
     * - Formato ISO-8601: YYYY-MM-DD
     *
     * Ejemplo: 2025-01-15
     */
    private LocalDate fechaCreacion;
}
