package com.goldenburgers.gestionUsuario.DTOs;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request DTO para actualizar una dirección existente de un cliente
 *
 * DTO utilizado para modificar una dirección de entrega ya registrada.
 * Permite cambiar la ciudad, el texto de la dirección y el alias.
 *
 * Casos de uso:
 * - Cliente se muda de dirección
 * - Corrección de errores en la dirección ingresada
 * - Cambio de ciudad
 * - Actualización del alias descriptivo
 *
 * Diferencia con CrearDireccionCliente:
 * - NO requiere idCliente (se obtiene de la dirección existente)
 * - Actualiza registro existente en lugar de crear nuevo
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ActualizarDireccionRequest {

    /**
     * ID de la ciudad actualizada
     *
     * Validaciones:
     * - @NotNull: Campo obligatorio
     *
     * Descripción:
     * - ID de la ciudad en la tabla CIUDAD
     * - Debe existir en la base de datos (validado en servicio)
     * - Permite cambiar la ciudad de la dirección
     *
     * Ejemplo: 5 (para Santiago)
     */
    @NotNull(message = "El ID de la ciudad es requerido")
    private Long idCiudad;

    /**
     * Texto actualizado de la dirección
     *
     * Validaciones:
     * - @NotBlank: Campo obligatorio
     *
     * Descripción:
     * - Dirección completa (calle, número, depto, etc.)
     * - Máximo 500 caracteres
     * - Reemplaza el texto anterior
     *
     * Ejemplo: "Av. Libertador 123, Depto 45, Torre B"
     */
    @NotBlank(message = "La dirección es requerida")
    private String direccion;

    /**
     * Alias actualizado de la dirección (opcional)
     *
     * Descripción:
     * - Nombre descriptivo para identificar la dirección
     * - Máximo 100 caracteres
     * - Puede ser null
     *
     * Ejemplos: "Casa Nueva", "Oficina Central", "Casa de mis padres"
     */
    private String alias;
}
