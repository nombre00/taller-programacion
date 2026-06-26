package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.PlantillaTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad PlantillaTurno - Acceso a datos de plantillas de turno
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla PLANTILLATURNO.
 * Las plantillas definen los tipos de turno reutilizables (ej: Turno Mañana, Turno Noche)
 * con su horario de inicio y término.
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todas las plantillas
 * - findById(Long id): Busca plantilla por ID
 * - save(PlantillaTurno plantilla): Guarda o actualiza plantilla
 * - deleteById(Long id): Elimina plantilla por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de plantillas
 *
 * Uso en el sistema:
 * - PlantillaTurnoService: Operaciones CRUD de plantillas
 * - SlotTurnoService: Consulta plantillas al crear slots
 * - AsignacionTurnoService: Consulta plantillas al crear asignaciones
 */
@Repository
public interface PlantillaTurnoRepository extends JpaRepository<PlantillaTurno, Long> {

    /**
     * Busca una plantilla por su nombre exacto
     *
     * Query generado automáticamente:
     * SELECT * FROM PLANTILLATURNO WHERE nombre = ?
     *
     * Uso típico:
     * - PlantillaTurnoService.createPlantilla(): Valida que el nombre no exista
     * - PlantillaTurnoService.updatePlantilla(): Valida que el nuevo nombre no esté en uso
     *
     * @param nombre Nombre exacto de la plantilla (ej: "Turno Mañana", "Turno Noche")
     * @return Optional<PlantillaTurno> - Contiene la plantilla si existe, Optional.empty() si no
     */
    Optional<PlantillaTurno> findByNombre(String nombre);

    /**
     * Verifica si existe una plantilla con el nombre especificado
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM PLANTILLATURNO WHERE nombre = ?
     *
     * Uso típico:
     * - Validar unicidad de nombre antes de crear o actualizar plantilla
     *
     * @param nombre Nombre de la plantilla a verificar
     * @return true si existe una plantilla con ese nombre, false si no existe
     */
    boolean existsByNombre(String nombre);
}
