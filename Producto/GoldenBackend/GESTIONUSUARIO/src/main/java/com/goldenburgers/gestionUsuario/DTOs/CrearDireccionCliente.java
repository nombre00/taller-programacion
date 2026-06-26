package com.goldenburgers.gestionUsuario.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para crear una nueva dirección de entrega para un cliente
 *
 * DTO (Data Transfer Object) utilizado para agregar direcciones de entrega a un cliente.
 * Un cliente puede tener múltiples direcciones (casa, trabajo, etc.).
 * Contiene validaciones automáticas usando Jakarta Bean Validation.
 *
 * Flujo de uso:
 * 1. Cliente autenticado envía este DTO al endpoint POST /api/clientes/direcciones
 * 2. Spring valida automáticamente los campos con @Valid
 * 3. Si las validaciones pasan, se crea la dirección asociada al cliente
 * 4. La dirección se usa al realizar pedidos para especificar lugar de entrega
 *
 * Integración con:
 * - ClienteController.agregarDireccion(): Recibe este DTO en el request body
 * - ClienteService.agregarDireccion(): Procesa los datos del DTO
 * - DireccionCliente (entidad): Se crea un registro en tabla DIRECCION_CLIENTE
 *
 * Validaciones aplicadas:
 * - @NotNull: El campo no puede ser null (aplica a tipos numéricos como Long)
 * - @NotBlank: El campo no puede ser null, vacío o solo espacios en blanco (String)
 *
 * Restricciones de negocio (validadas en servicio):
 * - El idCliente debe existir en la tabla CLIENTES
 * - El idCiudad debe existir en la tabla CIUDADES (dato maestro)
 * - Un cliente puede tener 0 o más direcciones
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearDireccionCliente {
    
    /**
     * ID del cliente al que se le agregará la dirección
     *
     * Validaciones:
     * - @NotNull: Campo obligatorio (no puede ser null)
     *
     * Descripción:
     * - Referencia al cliente propietario de la dirección
     * - Debe corresponder a un cliente existente en tabla CLIENTES
     * - Se usa como FK (Foreign Key) en tabla DIRECCION_CLIENTE
     * - Validado en servicio: debe existir el cliente
     *
     * Ejemplo: 1L, 25L, 100L
     */
    @NotNull(message = "El ID del cliente es requerido")
    private Long idCliente;

    /**
     * ID de la ciudad de la dirección
     *
     * Validaciones:
     * - @NotNull: Campo obligatorio (no puede ser null)
     *
     * Descripción:
     * - Referencia a la ciudad donde se encuentra la dirección
     * - Debe corresponder a una ciudad existente en tabla CIUDADES (dato maestro)
     * - Se usa como FK (Foreign Key) en tabla DIRECCION_CLIENTE
     * - Determina la zona de entrega del pedido
     * - Validado en servicio: debe existir la ciudad
     *
     * Ejemplo: 1L (Santiago), 2L (Valparaíso), 3L (Concepción)
     */
    @NotNull(message = "El ID de la ciudad es requerido")
    private Long idCiudad;

    /**
     * Dirección completa de entrega
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio (no puede ser null, vacío o solo espacios)
     *
     * Descripción:
     * - Texto completo de la dirección (calle, número, depto, etc.)
     * - Usado por el repartidor para encontrar el lugar de entrega
     * - Debe ser lo más específica posible
     * - Se almacena en tabla DIRECCION_CLIENTE
     *
     * Ejemplo: "Av. Libertador Bernardo O'Higgins 1234, Depto 501"
     */
    @NotBlank(message = "La dirección es requerida")
    private String direccion;

    /**
     * Alias descriptivo de la dirección (opcional)
     *
     * Validaciones:
     * - Campo opcional (puede ser null)
     *
     * Descripción:
     * - Nombre corto para identificar fácilmente la dirección
     * - Útil cuando el cliente tiene múltiples direcciones
     * - Se muestra en la lista de direcciones al hacer un pedido
     * - Se almacena en tabla DIRECCION_CLIENTE
     *
     * Ejemplos: "Casa", "Trabajo", "Oficina", "Casa de mis padres", "Departamento"
     */
    private String alias;
}
