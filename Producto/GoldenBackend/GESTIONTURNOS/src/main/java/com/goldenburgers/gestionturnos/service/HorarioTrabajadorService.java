package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.HorarioTrabajadorRequestDTO;
import com.goldenburgers.gestionturnos.model.AsignacionTurno;
import com.goldenburgers.gestionturnos.model.CalendarioSemana;
import com.goldenburgers.gestionturnos.model.HorarioTrabajador;
import com.goldenburgers.gestionturnos.model.SlotTurno;
import com.goldenburgers.gestionturnos.model.TrabajadorLocal;
import com.goldenburgers.gestionturnos.repository.AsignacionTurnoRepository;
import com.goldenburgers.gestionturnos.repository.CalendarioSemanaRepository;
import com.goldenburgers.gestionturnos.repository.HorarioTrabajadorRepository;
import com.goldenburgers.gestionturnos.repository.SlotTurnoRepository;
import com.goldenburgers.gestionturnos.repository.TrabajadorLocalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HorarioTrabajadorService {

    private static final Logger logger = LoggerFactory.getLogger(HorarioTrabajadorService.class);

    @Autowired
    private HorarioTrabajadorRepository horarioTrabajadorRepository;

    @Autowired
    private TrabajadorLocalRepository trabajadorLocalRepository;

    @Autowired
    private SlotTurnoRepository slotTurnoRepository;

    @Autowired
    private AsignacionTurnoRepository asignacionTurnoRepository;

    @Autowired
    private CalendarioSemanaRepository calendarioSemanaRepository;



    public List<HorarioTrabajador> listarTodos() {
        return horarioTrabajadorRepository.findAll();
    }

    public HorarioTrabajador obtenerPorId(Long idHorario) {
        return horarioTrabajadorRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + idHorario));
    }

    public List<HorarioTrabajador> listarPorTrabajador(Long idTrabajador) {
        return horarioTrabajadorRepository.findByTrabajadorIdTrabajador(idTrabajador);
    }

    public List<HorarioTrabajador> listarPorAsignacion(Long idAsignacion) {
        return horarioTrabajadorRepository.findByAsignacionIdAsignacion(idAsignacion);
    }

    

    /**
     * Crea un horario manualmente. Útil para asignaciones puntuales fuera del
     * flujo normal de CalendarioSemanaService.
     * El trabajador debe existir y estar activo.
     */
    public HorarioTrabajador crear(HorarioTrabajadorRequestDTO dto) {
        logger.info("Creando horario para trabajador ID: {}, slot ID: {}, fecha: {}",
                dto.getIdTrabajador(), dto.getIdSlot(), dto.getFechaTrabajo());

        TrabajadorLocal trabajador = trabajadorLocalRepository.findById(dto.getIdTrabajador())
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + dto.getIdTrabajador()));

        if (!trabajador.getActivo()) {
            throw new IllegalArgumentException("El trabajador con ID " + dto.getIdTrabajador() + " está inactivo");
        }

        SlotTurno slot = slotTurnoRepository.findById(dto.getIdSlot())
                .orElseThrow(() -> new IllegalArgumentException("Slot no encontrado con ID: " + dto.getIdSlot()));

        AsignacionTurno asignacion = asignacionTurnoRepository.findById(dto.getIdAsignacion())
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada con ID: " + dto.getIdAsignacion()));

        validarEstado(dto.getEstado());

        HorarioTrabajador horario = new HorarioTrabajador();
        horario.setTrabajador(trabajador);
        horario.setSlot(slot);
        horario.setAsignacion(asignacion);
        horario.setFechaTrabajo(dto.getFechaTrabajo());
        horario.setEstado(dto.getEstado());
        horario = horarioTrabajadorRepository.save(horario);

        logger.info("Horario creado exitosamente con ID: {}", horario.getIdHorario());
        return horario;
    }

    /**
     * Actualiza solo el estado de un horario existente.
     * Transiciones válidas: pendiente → confirmado, confirmado → ausente, confirmado → pendiente.
     */
    public HorarioTrabajador actualizarEstado(Long idHorario, String estado) {
        logger.info("Actualizando estado del horario ID: {} a '{}'", idHorario, estado);

        HorarioTrabajador horario = horarioTrabajadorRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + idHorario));

        validarEstado(estado);

        horario.setEstado(estado);
        horario = horarioTrabajadorRepository.save(horario);

        logger.info("Estado del horario ID: {} actualizado exitosamente a '{}'", idHorario, estado);
        return horario;
    }

    /**
     * Solo se pueden eliminar horarios en estado "pendiente".
     * Los confirmados y ausentes se conservan por historial.
     */
    public void eliminar(Long idHorario) {
        logger.info("Eliminando horario ID: {}", idHorario);

        HorarioTrabajador horario = horarioTrabajadorRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + idHorario));

        if (!"pendiente".equals(horario.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se pueden eliminar horarios en estado 'pendiente'. Estado actual: " + horario.getEstado());
        }

        horarioTrabajadorRepository.deleteById(idHorario);
        logger.info("Horario eliminado exitosamente: {}", idHorario);
    }

    private void validarEstado(String estado) {
        if (!"pendiente".equals(estado) && !"confirmado".equals(estado) && !"ausente".equals(estado)) {
            throw new IllegalArgumentException(
                    "Estado inválido: '" + estado + "'. Los valores permitidos son: pendiente, confirmado, ausente");
        }
    }


    /**
     * Asigna un trabajador a un horario existente y lo pasa a estado "confirmado".
     * El horario debe estar en estado "pendiente".
     * El trabajador debe existir y estar activo.
     */
    public HorarioTrabajador asignarTrabajador(Long idHorario, Long idTrabajador) {
        logger.info("Asignando trabajador ID: {} al horario ID: {}", idTrabajador, idHorario);

        HorarioTrabajador horario = horarioTrabajadorRepository.findById(idHorario)
                .orElseThrow(() -> new IllegalArgumentException("Horario no encontrado con ID: " + idHorario));

        if (!"pendiente".equals(horario.getEstado())) {
            throw new IllegalArgumentException(
                    "Solo se puede asignar trabajador a horarios en estado 'pendiente'. Estado actual: " + horario.getEstado());
        }

        TrabajadorLocal trabajador = trabajadorLocalRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));

        if (!trabajador.getActivo()) {
            throw new IllegalArgumentException("El trabajador con ID " + idTrabajador + " está inactivo");
        }

        horario.setTrabajador(trabajador);
        horario.setEstado("confirmado");
        horario = horarioTrabajadorRepository.save(horario);

        logger.info("Trabajador ID: {} asignado exitosamente al horario ID: {}", idTrabajador, idHorario);
        return horario;
    }

    // Para que la interfaz gráfica muestre los horarios de un calendario.public List<HorarioTrabajador> listarPorCalendario(Long idCalendario) {
    public List<HorarioTrabajador> listarPorCalendario(Long idCalendario) {
        CalendarioSemana calendario = calendarioSemanaRepository.findById(idCalendario)
                .orElseThrow(() -> new IllegalArgumentException("Calendario no encontrado con ID: " + idCalendario));
        return horarioTrabajadorRepository.findByCalendario(
                calendario.getSemana().getIdSemana(),
                calendario.getFechaInicio(),
                calendario.getFechaFin()
        );
    }

}