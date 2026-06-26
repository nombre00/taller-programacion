package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.AsignacionTurno;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad AsignacionTurno - Acceso a datos de asignaciones de turno
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla ASIGNACIONTURNO.
 * Las asignaciones vinculan una plantilla de turno a un día de la semana dentro
 * de una semana tipo. Permiten definir qué turno se trabaja cada día.
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todas las asignaciones
 * - findById(Long id): Busca asignación por ID
 * - save(AsignacionTurno asignacion): Guarda o actualiza asignación
 * - deleteById(Long id): Elimina asignación por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de asignaciones
 *
 * Uso en el sistema:
 * - AsignacionTurnoService: Operaciones CRUD de asignaciones
 * - CalendarioSemanaService: Consulta asignaciones al generar horarios concretos
 * - HorarioTrabajadorService: Consulta asignaciones al asignar trabajadores
 */
@Repository
public interface AsignacionTurnoRepository extends JpaRepository<AsignacionTurno, Long> {

    /**
     * Busca todas las asignaciones de una semana tipo específica
     *
     * Query generado automáticamente:
     * SELECT * FROM ASIGNACIONTURNO WHERE id_semana = ?
     *
     * Uso típico:
     * - AsignacionTurnoService.getAsignacionesBySemana(): Ver detalle completo de una semana tipo
     * - CalendarioSemanaService: Obtener todas las asignaciones al aplicar una semana al calendario
     *
     * @param idSemana ID de la semana tipo
     * @return List<AsignacionTurno> - Lista de asignaciones (puede ser vacía)
     */
    List<AsignacionTurno> findBySemanaIdSemana(Long idSemana);

    /**
     * Busca todas las asignaciones de un día de semana específico dentro de una semana tipo
     *
     * Query generado automáticamente:
     * SELECT * FROM ASIGNACIONTURNO WHERE id_semana = ? AND dia_semana = ?
     *
     * Uso típico:
     * - AsignacionTurnoService.getAsignacionesBySemanayDia(): Ver turnos de un día específico
     * - CalendarioSemanaService: Obtener asignaciones del día al generar horarios
     *
     * IMPORTANTE:
     * - dia_semana: 1=lunes, 2=martes, 3=miércoles, 4=jueves, 5=viernes, 6=sábado, 7=domingo
     *
     * @param idSemana ID de la semana tipo
     * @param diaSemana Número del día (1-7)
     * @return List<AsignacionTurno> - Lista de asignaciones para ese día (puede ser vacía)
     */
    List<AsignacionTurno> findBySemanaIdSemanaAndDiaSemana(Long idSemana, Integer diaSemana);

    /**
     * Verifica si existe alguna asignación que use la plantilla especificada
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM ASIGNACIONTURNO WHERE id_plantilla = ?
     *
     * Uso típico:
     * - PlantillaTurnoService.deletePlantilla(): Valida que la plantilla no esté
     *   en uso en alguna asignación antes de permitir su eliminación
     *
     * @param idPlantilla ID de la plantilla a verificar
     * @return true si existe alguna asignación con esa plantilla, false si no existe
     */
    boolean existsByPlantillaIdPlantilla(Long idPlantilla);

    /**
     * Verifica si existe alguna asignación que use la semana tipo especificada
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM ASIGNACIONTURNO WHERE id_semana = ?
     *
     * Uso típico:
     * - SemanaTipoService.deleteSemanaTipo(): Valida que la semana no esté
     *   en uso antes de permitir su eliminación
     *
     * @param idSemana ID de la semana tipo a verificar
     * @return true si existe alguna asignación con esa semana, false si no existe
     */
    boolean existsBySemanaIdSemana(Long idSemana);
}