package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.TrabajadorLocal;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad TrabajadorLocal - Acceso a datos del caché local de trabajadores
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla TRABAJADORLOCAL.
 * Almacena una copia mínima de los datos del trabajador obtenidos desde gestionUsuario.
 * El ID del trabajador es el mismo que en gestionUsuario — no se autogenera.
 *
 * IMPORTANTE:
 * - Esta tabla es un caché local, no la fuente de verdad de los trabajadores
 * - La fuente de verdad es el microservicio gestionUsuario
 * - Se sincroniza al iniciar el microservicio consumiendo la API de gestionUsuario
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todos los trabajadores locales
 * - findById(Long id): Busca trabajador por ID
 * - save(TrabajadorLocal trabajador): Guarda o actualiza trabajador
 * - deleteById(Long id): Elimina trabajador por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de trabajadores locales
 *
 * Uso en el sistema:
 * - TrabajadorLocalService: Sincronización y consulta de trabajadores
 * - HorarioTrabajadorService: Consulta trabajadores al crear horarios
 */
@Repository
public interface TrabajadorLocalRepository extends JpaRepository<TrabajadorLocal, Long> {

    /**
     * Busca todos los trabajadores locales activos
     *
     * Query generado automáticamente:
     * SELECT * FROM TRABAJADORLOCAL WHERE activo = true
     *
     * Uso típico:
     * - TrabajadorLocalService.getTrabajadoresActivos(): Obtener trabajadores disponibles
     *   para asignar en el calendario
     * - Mostrar lista de trabajadores en el selector de horarios
     *
     * @return List<TrabajadorLocal> - Lista de trabajadores activos (puede ser vacía)
     */
    List<TrabajadorLocal> findByActivoTrue();

    /**
     * Busca todos los trabajadores locales de una posición específica
     *
     * Query generado automáticamente:
     * SELECT * FROM TRABAJADORLOCAL WHERE id_posicion = ?
     *
     * Uso típico:
     * - TrabajadorLocalService.getTrabajadoresByPosicion(): Obtener trabajadores
     *   disponibles para cubrir un slot de una posición específica
     * - Filtrar trabajadores aptos para un slot al asignar horarios
     *
     * @param idPosicion ID de la posición a filtrar
     * @return List<TrabajadorLocal> - Lista de trabajadores con esa posición (puede ser vacía)
     */
    List<TrabajadorLocal> findByPosicionIdPosicion(Long idPosicion);

    /**
     * Busca todos los trabajadores locales activos de una posición específica
     *
     * Query generado automáticamente:
     * SELECT * FROM TRABAJADORLOCAL WHERE id_posicion = ? AND activo = true
     *
     * Uso típico:
     * - TrabajadorLocalService.getTrabajadoresActivosByPosicion(): Obtener solo trabajadores
     *   activos y aptos para cubrir un slot — el filtro más útil al asignar horarios
     *
     * @param idPosicion ID de la posición a filtrar
     * @return List<TrabajadorLocal> - Lista de trabajadores activos con esa posición (puede ser vacía)
     */
    List<TrabajadorLocal> findByPosicionIdPosicionAndActivoTrue(Long idPosicion);

    /**
     * Busca todos los trabajadores activos de una posición específica
     *
     * Query generado automáticamente:
     * SELECT * FROM TRABAJADORLOCAL WHERE activo = true AND id_posicion = ?
     *
     * Uso típico:
     * - TrabajadorLocalService.getTrabajadoresActivosByPosicion(): Filtrar trabajadores
     *   disponibles según la posición requerida por un slot al momento de asignar turnos
     *
     * @param idPosicion ID de la posición a filtrar
     * @return List<TrabajadorLocal> con los trabajadores activos en esa posición (puede ser lista vacía)
     */
    List<TrabajadorLocal> findByActivoTrueAndPosicionIdPosicion(Long idPosicion);
}
