package com.goldenburgers.gestionUsuario.DTOs;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para registrar un nuevo Cliente en el sistema
 *
 * DTO (Data Transfer Object) utilizado para recibir los datos de registro de un nuevo cliente.
 * Contiene validaciones automáticas usando Jakarta Bean Validation.
 *
 * Flujo de uso:
 * 1. Cliente se registra en Firebase Authentication (obtiene Firebase UID)
 * 2. Frontend envía este DTO al endpoint POST /api/clientes
 * 3. Spring valida automáticamente los campos con @Valid
 * 4. Si las validaciones pasan, se crea el cliente en la BD local
 *
 * Integración con:
 * - ClienteController.registerCliente(): Recibe este DTO en el request body
 * - ClienteService.registerCliente(): Procesa los datos del DTO
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
 *
 * Anotaciones Lombok:
 * - @Data: Genera getters, setters, toString, equals, hashCode
 * - @NoArgsConstructor: Genera constructor vacío (requerido por Jackson para deserialización)
 * - @AllArgsConstructor: Genera constructor con todos los parámetros
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegistrarCliente {
    
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
     * Ejemplo: "abc123xyz456def789"
     */
    @NotBlank(message = "El ID de usuario (Firebase UID) es requerido")
    private String idUsuario;

    /**
     * Email del cliente
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     * - @Email: Debe tener formato válido (usuario@dominio.com)
     *
     * Descripción:
     * - Debe coincidir con el email registrado en Firebase Authentication
     * - Se usa para identificación y comunicación con el cliente
     * - Es único en el sistema (validado en servicio)
     * - Se almacena en la tabla USUARIOS
     *
     * Ejemplo: "cliente@ejemplo.com"
     */
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    /**
     * Nombre completo del cliente
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     *
     * Descripción:
     * - Nombre del cliente para identificación
     * - Se muestra en pedidos, facturas, etc.
     * - Se almacena en la tabla CLIENTES
     *
     * Ejemplo: "Juan Pérez González"
     */
    @NotBlank(message = "El nombre del cliente es requerido")
    private String nombreCliente;

    /**
     * Número de teléfono del cliente (opcional)
     *
     * Validaciones:
     * - @Pattern: Debe tener exactamente 9 dígitos numéricos
     * - Campo opcional (puede ser null)
     *
     * Descripción:
     * - Teléfono de contacto del cliente
     * - Usado para comunicación sobre pedidos
     * - Formato chileno sin código de país (+56)
     * - Se almacena en la tabla CLIENTES
     *
     * Formato válido: "912345678" (9 dígitos sin espacios ni guiones)
     * Formato inválido: "+56912345678", "9 1234 5678", "12345678"
     */
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefonoCliente;
}
