package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.CalendarioSemana;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

/**
 * Repository para la entidad CalendarioSemana - Acceso a datos del calendario
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla CALENDARIOSEMANA.
 * El calendario registra en qué rango de fechas concretas se aplica cada semana tipo.
 * Es el puente entre las plantillas atemporales y los horarios reales.
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todos los registros del calendario
 * - findById(Long id): Busca registro por ID
 * - save(CalendarioSemana calendario): Guarda o actualiza registro
 * - deleteById(Long id): Elimina registro por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de registros
 *
 * Uso en el sistema:
 * - CalendarioSemanaService: Operaciones CRUD del calendario
 * - HorarioTrabajadorService: Consulta el calendario para generar horarios concretos
 */
@Repository
public interface CalendarioSemanaRepository extends JpaRepository<CalendarioSemana, Long> {

    /**
     * Busca todos los registros del calendario para una semana tipo específica
     *
     * Query generado automáticamente:
     * SELECT * FROM CALENDARIOSEMANA WHERE id_semana = ?
     *
     * Uso típico:
     * - CalendarioSemanaService.getCalendarioBySemana(): Ver en qué fechas se aplica una semana tipo
     * - Mostrar historial de aplicaciones de una semana tipo
     *
     * @param idSemana ID de la semana tipo
     * @return List<CalendarioSemana> - Lista de aplicaciones (puede ser vacía)
     */
    List<CalendarioSemana> findBySemanaIdSemana(Long idSemana);

    /**
     * Busca todos los registros del calendario dentro de un rango de fechas
     *
     * Query generado automáticamente:
     * SELECT * FROM CALENDARIOSEMANA WHERE fecha_inicio >= ? AND fecha_fin <= ?
     *
     * Uso típico:
     * - CalendarioSemanaService.getCalendarioByRango(): Obtener semanas aplicadas en un mes
     * - Mostrar vista mensual del calendario al usuario
     *
     * @param fechaInicio Fecha de inicio del rango a consultar
     * @param fechaFin Fecha de fin del rango a consultar
     * @return List<CalendarioSemana> - Lista de registros dentro del rango (puede ser vacía)
     */
    List<CalendarioSemana> findByFechaInicioGreaterThanEqualAndFechaFinLessThanEqual(
            LocalDate fechaInicio, LocalDate fechaFin);

    /**
     * Verifica si existe algún registro del calendario que use la semana tipo especificada
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM CALENDARIOSEMANA WHERE id_semana = ?
     *
     * Uso típico:
     * - SemanaTipoService.deleteSemanaTipo(): Valida que la semana tipo no esté
     *   aplicada en el calendario antes de permitir su eliminación
     *
     * @param idSemana ID de la semana tipo a verificar
     * @return true si existe algún registro con esa semana tipo, false si no existe
     */
    boolean existsBySemanaIdSemana(Long idSemana);

    /**
     * Busca todos los registros del calendario con repetición anual activa
     *
     * Query generado automáticamente:
     * SELECT * FROM CALENDARIOSEMANA WHERE repeticion_anual = true
     *
     * Uso típico:
     * - CalendarioSemanaService.getCalendariosRepeticionAnual(): Obtener semanas
     *   que se repiten automáticamente cada año para procesarlas al inicio del año
     *
     * @return List<CalendarioSemana> - Lista de registros con repetición anual (puede ser vacía)
     */
    List<CalendarioSemana> findByRepeticionAnualTrue();
}
