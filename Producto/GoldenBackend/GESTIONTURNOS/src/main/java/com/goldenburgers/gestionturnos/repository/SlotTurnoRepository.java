package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.SlotTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad SlotTurno - Acceso a datos de slots de turno
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla SLOTTURNO.
 * Los slots definen cuántos trabajadores de cada posición se necesitan dentro
 * de una plantilla de turno.
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todos los slots
 * - findById(Long id): Busca slot por ID
 * - save(SlotTurno slot): Guarda o actualiza slot
 * - deleteById(Long id): Elimina slot por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de slots
 *
 * Uso en el sistema:
 * - SlotTurnoService: Operaciones CRUD de slots
 * - HorarioTrabajadorService: Consulta slots al asignar trabajadores
 * - CalendarioSemanaService: Consulta slots al generar horarios concretos
 */
@Repository
public interface SlotTurnoRepository extends JpaRepository<SlotTurno, Long> {

    /**
     * Busca todos los slots de una plantilla específica
     *
     * Query generado automáticamente:
     * SELECT * FROM SLOTTURNO WHERE id_plantilla = ?
     *
     * Uso típico:
     * - SlotTurnoService.getSlotsByPlantilla(): Obtener slots al ver detalle de plantilla
     * - Mostrar desglose de personal requerido por turno
     *
     * @param idPlantilla ID de la plantilla de la cual obtener los slots
     * @return List<SlotTurno> - Lista de slots (puede ser vacía)
     */
    List<SlotTurno> findByPlantillaIdPlantilla(Long idPlantilla);

    /**
     * Busca todos los slots de una posición específica
     *
     * Query generado automáticamente:
     * SELECT * FROM SLOTTURNO WHERE id_posicion = ?
     *
     * Uso típico:
     * - SlotTurnoService.getSlotsByPosicion(): Consultar en qué turnos aparece una posición
     * - Validar si una posición está en uso antes de eliminarla
     *
     * @param idPosicion ID de la posición a consultar
     * @return List<SlotTurno> - Lista de slots (puede ser vacía)
     */
    List<SlotTurno> findByPosicionIdPosicion(Long idPosicion);

    /**
     * Verifica si existe algún slot que use la posición especificada
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM SLOTTURNO WHERE id_posicion = ?
     *
     * Uso típico:
     * - PosicionService.deletePosicion(): Valida que la posición no esté en uso
     *   antes de permitir su eliminación
     *
     * @param idPosicion ID de la posición a verificar
     * @return true si existe algún slot con esa posición, false si no existe
     */
    boolean existsByPosicionIdPosicion(Long idPosicion);

    /**
     * Verifica si existe algún slot que use la plantilla especificada
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM SLOTTURNO WHERE id_plantilla = ?
     *
     * Uso típico:
     * - PlantillaTurnoService.deletePlantilla(): Valida que la plantilla no esté en uso
     *   antes de permitir su eliminación
     *
     * @param idPlantilla ID de la plantilla a verificar
     * @return true si existe algún slot con esa plantilla, false si no existe
     */
    boolean existsByPlantillaIdPlantilla(Long idPlantilla);
}