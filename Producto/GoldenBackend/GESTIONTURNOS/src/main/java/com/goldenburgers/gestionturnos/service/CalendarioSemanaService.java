package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.CalendarioSemanaRequestDTO;
import com.goldenburgers.gestionturnos.model.AsignacionTurno;
import com.goldenburgers.gestionturnos.model.CalendarioSemana;
import com.goldenburgers.gestionturnos.model.HorarioTrabajador;
import com.goldenburgers.gestionturnos.model.SemanaTipo;
import com.goldenburgers.gestionturnos.repository.AsignacionTurnoRepository;
import com.goldenburgers.gestionturnos.repository.CalendarioSemanaRepository;
import com.goldenburgers.gestionturnos.repository.HorarioTrabajadorRepository;
import com.goldenburgers.gestionturnos.repository.SemanaTipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class CalendarioSemanaService {

    private static final Logger logger = LoggerFactory.getLogger(CalendarioSemanaService.class);

    @Autowired
    private CalendarioSemanaRepository calendarioSemanaRepository;

    @Autowired
    private SemanaTipoRepository semanaTipoRepository;

    @Autowired
    private AsignacionTurnoRepository asignacionTurnoRepository;

    @Autowired
    private HorarioTrabajadorRepository horarioTrabajadorRepository;

    public List<CalendarioSemana> listarTodos() {
        return calendarioSemanaRepository.findAll();
    }

    public CalendarioSemana obtenerPorId(Long idCalendario) {
        return calendarioSemanaRepository.findById(idCalendario)
                .orElseThrow(() -> new IllegalArgumentException("Registro de calendario no encontrado con ID: " + idCalendario));
    }

    public List<CalendarioSemana> listarPorSemana(Long idSemana) {
        return calendarioSemanaRepository.findBySemanaIdSemana(idSemana);
    }

    /**
     * Aplica una semana tipo al calendario en un rango de fechas y genera
     * automáticamente los HorarioTrabajador en estado "pendiente" para cada
     * día del rango según las asignaciones de la semana tipo.
     *
     * Validaciones:
     * - La semana tipo debe existir
     * - fechaInicio debe ser anterior o igual a fechaFin
     * - fechaInicio debe ser lunes
     * - La semana tipo debe tener al menos una asignación
     */
    public CalendarioSemana crear(CalendarioSemanaRequestDTO dto) {
        logger.info("Aplicando semana tipo ID: {} al calendario del {} al {}",
                dto.getIdSemana(), dto.getFechaInicio(), dto.getFechaFin());

        SemanaTipo semana = semanaTipoRepository.findById(dto.getIdSemana())
                .orElseThrow(() -> new IllegalArgumentException("Semana tipo no encontrada con ID: " + dto.getIdSemana()));

        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior o igual a la fecha de fin");
        }

        if (dto.getFechaInicio().getDayOfWeek() != DayOfWeek.MONDAY) {
            throw new IllegalArgumentException("La fecha de inicio debe ser un lunes");
        }

        List<AsignacionTurno> asignaciones = asignacionTurnoRepository.findBySemanaIdSemana(dto.getIdSemana());
        if (asignaciones.isEmpty()) {
            throw new IllegalArgumentException("La semana tipo no tiene asignaciones de turno configuradas");
        }

        // Generar HorarioTrabajador en estado "pendiente" por cada día del rango
        LocalDate inicioSemanaActual = dto.getFechaInicio();
        while (!inicioSemanaActual.isAfter(dto.getFechaFin())) {
            for (AsignacionTurno asignacion : asignaciones) {
                LocalDate fechaConcreta = inicioSemanaActual.plusDays(asignacion.getDiaSemana() - 1);

                if (!fechaConcreta.isAfter(dto.getFechaFin())) {
                    HorarioTrabajador horario = new HorarioTrabajador();
                    horario.setAsignacion(asignacion);
                    // Slot y trabajador quedan en null — se asignan luego desde HorarioTrabajadorService
                    horario.setFechaTrabajo(fechaConcreta);
                    horario.setEstado("pendiente");
                    horarioTrabajadorRepository.save(horario);
                }
            }
            inicioSemanaActual = inicioSemanaActual.plusWeeks(1);
        }

        CalendarioSemana calendario = new CalendarioSemana();
        calendario.setSemana(semana);
        calendario.setFechaInicio(dto.getFechaInicio());
        calendario.setFechaFin(dto.getFechaFin());
        calendario.setRepeticionAnual(dto.getRepeticionAnual() != null ? dto.getRepeticionAnual() : false);
        calendario = calendarioSemanaRepository.save(calendario);

        logger.info("Semana tipo aplicada exitosamente al calendario con ID: {}", calendario.getIdCalendario());
        return calendario;
    }

    public CalendarioSemana actualizar(Long idCalendario, CalendarioSemanaRequestDTO dto) {
        logger.info("Actualizando registro de calendario ID: {}", idCalendario);

        CalendarioSemana calendario = calendarioSemanaRepository.findById(idCalendario)
                .orElseThrow(() -> new IllegalArgumentException("Registro de calendario no encontrado con ID: " + idCalendario));

        SemanaTipo semana = semanaTipoRepository.findById(dto.getIdSemana())
                .orElseThrow(() -> new IllegalArgumentException("Semana tipo no encontrada con ID: " + dto.getIdSemana()));

        if (dto.getFechaInicio().isAfter(dto.getFechaFin())) {
            throw new IllegalArgumentException("La fecha de inicio debe ser anterior o igual a la fecha de fin");
        }

        calendario.setSemana(semana);
        calendario.setFechaInicio(dto.getFechaInicio());
        calendario.setFechaFin(dto.getFechaFin());
        calendario.setRepeticionAnual(dto.getRepeticionAnual() != null ? dto.getRepeticionAnual() : false);
        calendario = calendarioSemanaRepository.save(calendario);

        logger.info("Registro de calendario actualizado exitosamente: {}", idCalendario);
        return calendario;
    }

    public void eliminar(Long idCalendario) {
        logger.info("Eliminando registro de calendario ID: {}", idCalendario);

        if (!calendarioSemanaRepository.existsById(idCalendario)) {
            throw new IllegalArgumentException("Registro de calendario no encontrado con ID: " + idCalendario);
        }

        calendarioSemanaRepository.deleteById(idCalendario);
        logger.info("Registro de calendario eliminado exitosamente: {}", idCalendario);
    }
}