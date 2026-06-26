package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.Posicion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad Posicion - Acceso a datos de posiciones/cargos
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla POSICION.
 * Las posiciones definen los cargos disponibles en el negocio (cajero, cocinero, etc.)
 * con su sueldo base y color para visualización en el calendario.
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todas las posiciones
 * - findById(Long id): Busca posición por ID
 * - save(Posicion posicion): Guarda o actualiza posición
 * - deleteById(Long id): Elimina posición por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de posiciones
 *
 * Uso en el sistema:
 * - PosicionService: Operaciones CRUD de posiciones
 * - TrabajadorLocalService: Verifica existencia de posición al asignar trabajador
 * - SlotTurnoService: Consulta posiciones al crear slots
 */
@Repository
public interface PosicionRepository extends JpaRepository<Posicion, Long> {

    /**
     * Busca una posición por su nombre exacto
     *
     * Query generado automáticamente:
     * SELECT * FROM POSICION WHERE nombre = ?
     *
     * Uso típico:
     * - PosicionService.createPosicion(): Valida que el nombre no exista
     * - PosicionService.updatePosicion(): Valida que el nuevo nombre no esté en uso
     *
     * @param nombre Nombre exacto de la posición (ej: "Cajero", "Cocinero")
     * @return Optional<Posicion> - Contiene la posición si existe, Optional.empty() si no
     */
    Optional<Posicion> findByNombre(String nombre);

    /**
     * Verifica si existe una posición con el nombre especificado
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM POSICION WHERE nombre = ?
     *
     * Uso típico:
     * - Validar unicidad de nombre antes de crear o actualizar posición
     *
     * Ventaja sobre findByNombre():
     * - Más eficiente cuando solo necesitas verificar existencia
     *
     * @param nombre Nombre de la posición a verificar
     * @return true si existe una posición con ese nombre, false si no existe
     */
    boolean existsByNombre(String nombre);
}
