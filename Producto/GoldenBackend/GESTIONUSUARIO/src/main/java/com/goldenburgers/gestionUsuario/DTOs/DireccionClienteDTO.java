package com.goldenburgers.gestionUsuario.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Dirección de Cliente
 *
 * DTO (Data Transfer Object) utilizado para retornar información de direcciones de entrega.
 * Cada cliente puede tener múltiples direcciones (casa, trabajo, etc.).
 *
 * Uso en el sistema:
 * - Retornado por ClienteController al crear o consultar direcciones
 * - Usado al hacer pedidos para seleccionar dirección de entrega
 * - Muestra la información completa de una dirección incluyendo ciudad
 *
 * Conversión desde entidad:
 * - EntityMapper.toDTO(DireccionCliente): Convierte entidad JPA a este DTO
 * - Incluye conversión anidada de Ciudad a CiudadDTO
 *
 * Integración con:
 * - ClienteService: Retorna este DTO en operaciones de direcciones
 * - ClienteController: Retorna este DTO en respuestas HTTP
 * - Módulo de Pedidos: Usa este DTO para direcciones de entrega
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DireccionClienteDTO {

    /**
     * ID único de la dirección (clave primaria)
     *
     * Descripción:
     * - Identificador único de la dirección en tabla DIRECCION_CLIENTE
     * - Generado automáticamente por la BD (secuencia)
     * - Usado para eliminar o actualizar direcciones específicas
     * - Usado al seleccionar dirección en pedidos
     *
     */
    private Long idDireccion;

    /**
     * ID del cliente propietario de la dirección
     *
     * Descripción:
     * - Referencia al cliente que posee esta dirección
     * - FK a tabla CLIENTES
     * - Un cliente puede tener múltiples direcciones
     * - Usado para filtrar direcciones por cliente
     *
     */
    private Long idCliente;

    /**
     * Ciudad de la dirección (DTO anidado)
     *
     * Descripción:
     * - Información completa de la ciudad (id y nombre)
     * - Convertido desde entidad Ciudad mediante EntityMapper
     * - Se muestra para identificar zona de entrega
     * - Permite mostrar nombre de ciudad sin consultas adicionales
     *
     * Ejemplo: {idCiudad: 1, nombreCiudad: "Viña del Mar"}
     */
    private CiudadDTO ciudad;

    /**
     * Dirección completa de entrega
     *
     * Descripción:
     * - Texto completo de la dirección (calle, número, depto, etc.)
     * - Usado por repartidor para encontrar lugar de entrega
     * - Campo obligatorio al crear dirección
     * - Se almacena en tabla DIRECCION_CLIENTE
     *
     * Ejemplo: "Av. Libertador Bernardo O'Higgins 1234, Depto 501"
     */
    private String direccion;

    /**
     * Alias descriptivo de la dirección (opcional)
     *
     * Descripción:
     * - Nombre corto para identificar fácilmente la dirección
     * - Útil cuando el cliente tiene múltiples direcciones
     * - Se muestra en selector de direcciones al hacer pedido
     * - Puede ser null si no se asignó alias
     *
     * Ejemplos: "Casa", "Trabajo", "Oficina", "Casa de mis padres"
     */
    private String alias;
}
