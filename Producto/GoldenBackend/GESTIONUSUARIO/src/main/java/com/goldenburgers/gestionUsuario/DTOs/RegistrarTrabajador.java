package com.goldenburgers.gestionUsuario.DTOs;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para registrar un nuevo Trabajador en el sistema
 *
 * DTO (Data Transfer Object) utilizado para recibir los datos de registro de un nuevo trabajador.
 * Contiene validaciones automáticas usando Jakarta Bean Validation.
 *
 * IMPORTANTE - Restricción de seguridad:
 * - SOLO los ADMINISTRADORES pueden crear trabajadores
 * - Validado en TrabajadorController con @PreAuthorize("hasRole('ADMIN')")
 * - Los trabajadores NO pueden crear otros trabajadores
 *
 * Flujo de uso:
 * 1. ADMIN crea cuenta en Firebase Authentication para el trabajador (obtiene Firebase UID)
 * 2. ADMIN envía este DTO al endpoint POST /api/trabajadores
 * 3. Spring valida automáticamente los campos con @Valid
 * 4. Si las validaciones pasan, se crea el trabajador en la BD local
 *
 * Integración con:
 * - TrabajadorController.registerTrabajador(): Recibe este DTO en el request body
 * - TrabajadorService.registerTrabajador(): Procesa los datos del DTO
 * - Firebase Authentication: El idUsuario debe corresponder a un usuario Firebase
 *
 * Validaciones aplicadas:
 * - @NotBlank: El campo no puede ser null, vacío o solo espacios en blanco
 * - @Email: Valida formato de email (ej: usuario@dominio.com)
 * - @Pattern: Valida expresión regular personalizada
 *
 * Restricciones de negocio (validadas en servicio):
 * - El idUsuario (Firebase UID) debe ser único (no puede estar registrado)
 * - El email debe ser único (no puede estar en uso)
 * - El RUT debe ser único (no pueden existir dos trabajadores con el mismo RUT)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor

public class RegistrarTrabajador {
    
    /**
     * ID único del usuario en Firebase Authentication (Firebase UID)
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     *
     * Descripción:
     * - Este ID se obtiene al registrar al usuario en Firebase Authentication
     * - Es único por usuario en Firebase
     * - Se usa como clave primaria en la tabla USUARIOS
     * - Permite vincular el usuario local con Firebase Auth
     *
     * Ejemplo: "xyz789abc123ghi456"
     */
    @NotBlank(message = "El ID de usuario (Firebase UID) es requerido")
    private String idUsuario;

    /**
     * Email del trabajador
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     * - @Email: Debe tener formato válido (usuario@dominio.com)
     *
     * Descripción:
     * - Debe coincidir con el email registrado en Firebase Authentication
     * - Se usa para identificación y comunicación con el trabajador
     * - Es único en el sistema (validado en servicio)
     * - Se almacena en la tabla USUARIOS
     *
     * Ejemplo: "trabajador@empresa.com"
     */
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    /**
     * Nombre completo del trabajador
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     *
     * Descripción:
     * - Nombre del trabajador para identificación interna
     * - Se muestra en asignación de pedidos, reportes, etc.
     * - Se almacena en la tabla TRABAJADORES
     *
     * Ejemplo: "Pedro López Martínez"
     */
    @NotBlank(message = "El nombre del trabajador es requerido")
    private String nombreTrabajador;

    /**
     * RUT (Rol Único Tributario) del trabajador - Identificador nacional chileno
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     * - @Pattern: Debe cumplir formato chileno 
     *
     * Descripción:
     * - Identificador único nacional (Chile)
     * - Usado para identificación legal del trabajador
     * - Es único en el sistema (validado en servicio)
     * - Se almacena en la tabla TRABAJADORES
     *
     * Formato válido:
     * - Patrón: "^[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3}-[0-9Kk]$"
     * - 1-2 dígitos, punto, 3 dígitos, punto, 3 dígitos, guión, dígito verificador (0-9 o K/k)
     * - Ejemplos válidos: "12.345.678-9", "9.876.543-K", "1.234.567-0"
     * - Ejemplos inválidos: "12345678-9", "12.345.678", "123456789"
     *
     * IMPORTANTE:
     * - El dígito verificador puede ser número (0-9) o letra K (mayúscula o minúscula)
     * - Los puntos y guión son obligatorios
     */
    @NotBlank(message = "El RUT es requerido")
    @Pattern(regexp = "^[0-9]{1,2}\\.[0-9]{3}\\.[0-9]{3}-[0-9Kk]$",
             message = "El RUT debe tener formato XX.XXX.XXX-X")
    private String rutTrabajador;
}
