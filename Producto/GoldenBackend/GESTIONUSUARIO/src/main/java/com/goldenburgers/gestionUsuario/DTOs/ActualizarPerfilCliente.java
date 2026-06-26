package com.goldenburgers.gestionUsuario.DTOs;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para que el cliente actualice su propio perfil
 *
 * DTO (Data Transfer Object) utilizado para que el cliente autenticado
 * pueda modificar sus datos personales: nombre, email y teléfono.
 *
 * Casos de uso:
 * - Cliente olvida la contraseña de su email original y no puede recuperarlo
 * - Cliente cambia de número de teléfono
 * - Cliente se cambia de nombre legalmente
 * - Cliente desea corregir datos ingresados incorrectamente
 *
 * Flujo de uso:
 * 1. Cliente inicia sesión con Firebase Authentication
 * 2. Frontend obtiene el token JWT con el Firebase UID
 * 3. Frontend envía este DTO al endpoint PUT /api/clientes/perfil
 * 4. Backend extrae el Firebase UID del token JWT
 * 5. Backend actualiza los datos del cliente correspondiente
 *
 * Seguridad:
 * - El cliente solo puede modificar SUS PROPIOS datos
 * - El Firebase UID se obtiene del token JWT (no del body)
 * - No se requiere enviar contraseña (autenticación por token)
 *
 * Validaciones aplicadas:
 * - @NotBlank: El campo no puede ser null, vacío o solo espacios
 * - @Email: Valida formato de email correcto
 * - @Pattern: Valida que el teléfono tenga exactamente 9 dígitos
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarPerfilCliente {

    /**
     * Nuevo nombre del cliente
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     *
     * Descripción:
     * - Nombre completo actualizado del cliente
     * - Reemplaza el nombre anterior en tabla CLIENTES
     * - Se mostrará en pedidos futuros
     *
     * Ejemplo: "María González López"
     */
    @NotBlank(message = "El nombre del cliente es requerido")
    private String nombreCliente;

    /**
     * Nuevo email del cliente
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     * - @Email: Debe tener formato válido (usuario@dominio.com)
     *
     * Descripción:
     * - Email actualizado del cliente
     * - Debe ser único en el sistema (validado en servicio)
     * - Reemplaza el email anterior en tabla USUARIOS
     * - Importante: NO actualiza el email en Firebase Authentication
     *   (eso debe hacerse desde el frontend con Firebase SDK)
     *
     * Ejemplo: "nuevo.email@ejemplo.com"
     */
    @NotBlank(message = "El email es requerido")
    @Email(message = "El email debe ser válido")
    private String email;

    /**
     * Nuevo número de teléfono del cliente
     *
     * Validaciones:
     * - @Pattern: Debe tener exactamente 9 dígitos numéricos
     * - Campo opcional (puede ser null para eliminar teléfono)
     *
     * Descripción:
     * - Teléfono actualizado del cliente
     * - Formato chileno sin código de país (+56)
     * - Reemplaza el teléfono anterior en tabla CLIENTES
     * - Si se envía null, se elimina el teléfono
     *
     * Formato válido: "987654321" (9 dígitos sin espacios)
     * Formato inválido: "+56987654321", "9 8765 4321", "12345678"
     */
    @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos")
    private String telefonoCliente;
}
