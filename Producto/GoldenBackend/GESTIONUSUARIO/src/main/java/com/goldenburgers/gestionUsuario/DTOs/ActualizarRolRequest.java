package com.goldenburgers.gestionUsuario.DTOs;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para cambiar el rol de un trabajador
 *
 * DTO utilizado por ADMIN para cambiar el rol de un trabajador.
 * Permite promover un trabajador a ADMIN o degradar un ADMIN a trabajador.
 *
 * Casos de uso:
 * - Promover trabajador experimentado a administrador
 * - Degradar administrador que ya no requiere permisos elevados
 * - Ajustes organizacionales en permisos
 *
 * Roles disponibles (según tabla ROL):
 * - 1: Cliente (NO aplicable para trabajadores)
 * - 2: Trabajador
 * - 3: Admin
 *
 * IMPORTANTE:
 * - Solo se puede cambiar entre roles Trabajador (2) y Admin (3)
 * - NO se puede cambiar a rol Cliente (1) - requiere crear cliente por separado
 * - Solo ADMIN puede ejecutar esta operación
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarRolRequest {

    /**
     * ID del nuevo rol a asignar
     *
     * Validaciones:
     * - @NotNull: Campo obligatorio
     *
     * Descripción:
     * - ID del rol en la tabla ROLES
     * - Valores válidos: 2 (Trabajador) o 3 (Admin)
     * - NO se permite: 1 (Cliente)
     *
     * Ejemplo: 3 (para promover a Admin)
     */
    @NotNull(message = "El ID del rol es requerido")
    private Long idRol;
}
