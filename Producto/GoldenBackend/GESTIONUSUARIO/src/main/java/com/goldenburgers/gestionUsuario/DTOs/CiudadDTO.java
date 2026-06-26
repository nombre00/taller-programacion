package com.goldenburgers.gestionUsuario.DTOs;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO de respuesta para Ciudad
 *
 * DTO (Data Transfer Object) utilizado para retornar información de ciudades disponibles.
 * Las ciudades son datos maestros que definen las zonas de entrega.
 *
 * Uso en el sistema:
 * - Retornado por CiudadController al consultar ciudades
 * - Usado en DireccionClienteDTO para mostrar la ciudad de una dirección
 * - Usado en formularios frontend para seleccionar ciudad al crear dirección
 *
 * Conversión desde entidad:
 * - EntityMapper.toDTO(Ciudad): Convierte entidad JPA Ciudad a este DTO
 *
 * Integración con:
 * - CiudadService: Retorna este DTO en todas sus operaciones
 * - CiudadController: Retorna este DTO en respuestas HTTP
 * - DireccionClienteDTO: Incluye este DTO como campo anidado
 * - CreateDireccionRequest: Recibe idCiudad para crear direcciones
 *
 * Anotaciones Lombok:
 * - @Data: Genera getters, setters, toString, equals, hashCode
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CiudadDTO {

    /**
     * ID único de la ciudad (clave primaria)
     *
     * Descripción:
     * - Identificador único de la ciudad en tabla CIUDADES
     * - Generado automáticamente por la BD (secuencia)
     * - Usado como FK en tabla DIRECCION_CLIENTE
     * - Usado en CreateDireccionRequest para seleccionar ciudad
     *
     */
    private Long idCiudad;

    /**
     * Nombre de la ciudad
     *
     * Descripción:
     * - Nombre de la ciudad disponible para entrega
     * - Se muestra en lista de selección de ciudades
     * - Es único en el sistema
     * - Case-sensitive al buscar por nombre
     *
     */
    private String nombreCiudad;
}
