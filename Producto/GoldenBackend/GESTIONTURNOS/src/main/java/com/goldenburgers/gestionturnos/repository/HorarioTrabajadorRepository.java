package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.HorarioTrabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface HorarioTrabajadorRepository extends JpaRepository<HorarioTrabajador, Long> {

    // ── Búsquedas por trabajador ─────────────────────────────────────────────

    List<HorarioTrabajador> findByTrabajadorIdTrabajador(Long idTrabajador);

    List<HorarioTrabajador> findByTrabajadorIdTrabajadorAndFechaTrabajoGreaterThanEqualAndFechaTrabajoLessThanEqual(
            Long idTrabajador, LocalDate fechaInicio, LocalDate fechaFin);

    List<HorarioTrabajador> findByTrabajadorIdTrabajadorAndEstado(Long idTrabajador, String estado);

    long countByTrabajadorIdTrabajadorAndFechaTrabajo(Long idTrabajador, LocalDate fechaTrabajo);

    boolean existsByTrabajadorIdTrabajadorAndEstadoIn(Long idTrabajador, List<String> estados);

    // ── Búsquedas por fecha ──────────────────────────────────────────────────

    List<HorarioTrabajador> findByFechaTrabajo(LocalDate fechaTrabajo);

    List<HorarioTrabajador> findByFechaTrabajoAndEstado(LocalDate fechaTrabajo, String estado);

    // List<HorarioTrabajador> findByAsignacionCalendarioIdCalendario(Long idCalendario);  deprecado.

    // ── Búsquedas por slot ───────────────────────────────────────────────────

    List<HorarioTrabajador> findBySlotIdSlotAndFechaTrabajo(Long idSlot, LocalDate fechaTrabajo);

    /**
     * Verifica si existe algún horario asociado a un slot específico.
     * Usado en SlotTurnoService.eliminar() para impedir borrar un slot en uso.
     */
    boolean existsBySlotIdSlot(Long idSlot);

    // ── Búsquedas por asignación ─────────────────────────────────────────────

    /**
     * Lista todos los horarios generados a partir de una asignación concreta.
     * Usado en HorarioTrabajadorService.listarPorAsignacion().
     */
    List<HorarioTrabajador> findByAsignacionIdAsignacion(Long idAsignacion);

    /**
     * Verifica si existe algún horario asociado a una asignación específica.
     * Usado en AsignacionTurnoService.eliminar() para impedir borrar una asignación en uso.
     */
    boolean existsByAsignacionIdAsignacion(Long idAsignacion);


    // Busca todos los horarios dentro de una fecha.
    @Query("SELECT h FROM HorarioTrabajador h " +
        "WHERE h.asignacion.semana.idSemana = :idSemana " +
        "AND h.fechaTrabajo BETWEEN :fechaInicio AND :fechaFin")
    List<HorarioTrabajador> findByCalendario(
        @Param("idSemana") Long idSemana,
        @Param("fechaInicio") LocalDate fechaInicio,
        @Param("fechaFin") LocalDate fechaFin
    );
}