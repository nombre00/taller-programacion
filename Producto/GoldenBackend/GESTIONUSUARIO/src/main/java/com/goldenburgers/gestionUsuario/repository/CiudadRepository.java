package com.goldenburgers.gestionUsuario.repository;

import com.goldenburgers.gestionUsuario.model.Ciudad;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;


/**
 * Repository para la entidad Ciudad - Acceso a datos de ciudades disponibles
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla CIUDADES.
 * Las ciudades son datos maestros que definen las zonas de entrega disponibles.
 * - Las ciudades NO se crean/modifican desde la aplicación
 * - Son datos maestros insertados manualmente en la BD
 * - Solo operaciones de lectura en la aplicación
 * 
 *   Datos maestros :
 * - Viña del Mar, Valparaíso, Curauma, Quilpue y Villa Alemana.
 * - Definen zonas de cobertura para entregas
 * 
 * Características:
 * - Extiende JpaRepository<Ciudad, Long>: Hereda métodos CRUD estándar
 * - Long: Tipo de la clave primaria (idCiudad)
 * - Define queries personalizados usando Spring Data JPA method naming
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todas las ciudades
 * - findById(Long id): Busca ciudad por ID
 * - save(Ciudad ciudad): Guarda o actualiza ciudad
 * - deleteById(Long id): Elimina ciudad por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de ciudades
 *
 * Uso en el sistema:
 * - CiudadService: Usa este repository para consultar ciudades
 * - ClienteService: Valida ciudades al crear direcciones de entrega
 * - Frontend: Obtiene lista para selector de ciudades
 *
 */


@Repository

public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
    
     /**
     * Busca una ciudad por su nombre exacto
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT * FROM CIUDADES WHERE nombre_ciudad = ?
     *
     * Uso típico:
     * - Buscar ciudad por nombre en filtros
     * - Validar que una ciudad existe
     * - Autocompletado de ciudades en formularios
     *
     * IMPORTANTE:
     * - La búsqueda es case-sensitive (debe coincidir exactamente)
     * - Nombres válidos según datos maestros configurados
     * - Retorna Optional.empty() si no existe
     *
     * @param nombreCiudad Nombre exacto de la ciudad (ej: "Santiago", "Valparaíso")
     * @return Optional<Ciudad> - Contiene la ciudad si existe, Optional.empty() si no existe
     */
    Optional<Ciudad> findByNombreCiudad(String nombreCiudad);


    /**
     * Verifica si existe una ciudad con el nombre especificado
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) > 0 FROM CIUDADES WHERE nombre_ciudad = ?
     *
     * Uso típico:
     * - Validar que una ciudad existe antes de realizar operaciones
     * - Verificaciones de integridad de datos
     *
     * Ventaja sobre findByNombreCiudad():
     * - Más eficiente cuando solo necesitas verificar existencia
     * - No carga la entidad completa, solo cuenta
     *
     * @param nombreCiudad Nombre de la ciudad a verificar
     * @return true si existe una ciudad con ese nombre, false si no existe
     */
    boolean existsByNombreCiudad(String nombreCiudad);
    
}
