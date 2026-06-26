package com.goldenburgers.gestionUsuario.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para actualizar el email de un usuario (Cliente o Trabajador)
 *
 * DTO utilizado por ADMIN o TRABAJADOR para modificar el email de un cliente,
 * o por ADMIN para modificar el email de un trabajador.
 *
 * Casos de uso:
 * - Cliente solicita cambio de email pero olvidó contraseña del email anterior
 * - Corrección administrativa de emails incorrectos
 * - Actualización masiva de emails corporativos
 *
 * IMPORTANTE:
 * - Este endpoint actualiza el email en la base de datos local
 * - NO actualiza el email en Firebase Authentication
 * - El email en Firebase debe actualizarse desde el frontend usando Firebase SDK
 *
 * Restricciones de seguridad:
 * - Para clientes: ADMIN o TRABAJADOR pueden modificar
 * - Para trabajadores: SOLO ADMIN puede modificar
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarEmailRequest {

    /**
     * Nuevo email del usuario
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     * - @Email: Debe tener formato válido (usuario@dominio.com)
     *
     * Descripción:
     * - Email actualizado del usuario
     * - Debe ser único en el sistema (validado en servicio)
     * - Reemplaza el email anterior en tabla USUARIOS
     *
     * Ejemplo: "nuevo.email@ejemplo.com"
     */
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;
}
