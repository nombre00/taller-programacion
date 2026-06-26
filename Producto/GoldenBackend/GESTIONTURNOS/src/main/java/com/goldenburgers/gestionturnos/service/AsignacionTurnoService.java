package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.AsignacionTurnoRequestDTO;
import com.goldenburgers.gestionturnos.model.AsignacionTurno;
import com.goldenburgers.gestionturnos.model.PlantillaTurno;
import com.goldenburgers.gestionturnos.model.SemanaTipo;
import com.goldenburgers.gestionturnos.repository.AsignacionTurnoRepository;
import com.goldenburgers.gestionturnos.repository.HorarioTrabajadorRepository;
import com.goldenburgers.gestionturnos.repository.PlantillaTurnoRepository;
import com.goldenburgers.gestionturnos.repository.SemanaTipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class AsignacionTurnoService {

    private static final Logger logger = LoggerFactory.getLogger(AsignacionTurnoService.class);

    @Autowired
    private AsignacionTurnoRepository asignacionTurnoRepository;

    @Autowired
    private SemanaTipoRepository semanaTipoRepository;

    @Autowired
    private PlantillaTurnoRepository plantillaTurnoRepository;

    @Autowired
    private HorarioTrabajadorRepository horarioTrabajadorRepository;

    public List<AsignacionTurno> listarTodas() {
        return asignacionTurnoRepository.findAll();
    }

    public AsignacionTurno obtenerPorId(Long idAsignacion) {
        return asignacionTurnoRepository.findById(idAsignacion)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada con ID: " + idAsignacion));
    }

    public List<AsignacionTurno> listarPorSemana(Long idSemana) {
        return asignacionTurnoRepository.findBySemanaIdSemana(idSemana);
    }

    public AsignacionTurno crear(AsignacionTurnoRequestDTO dto) {
        logger.info("Creando asignación para semana ID: {}, plantilla ID: {}, día: {}",
                dto.getIdSemana(), dto.getIdPlantilla(), dto.getDiaSemana());

        SemanaTipo semana = semanaTipoRepository.findById(dto.getIdSemana())
                .orElseThrow(() -> new IllegalArgumentException("Semana tipo no encontrada con ID: " + dto.getIdSemana()));

        PlantillaTurno plantilla = plantillaTurnoRepository.findById(dto.getIdPlantilla())
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada con ID: " + dto.getIdPlantilla()));

        // Validación de rango ya cubierta por @Min/@Max en el DTO, pero la dejamos como seguridad
        if (dto.getDiaSemana() < 1 || dto.getDiaSemana() > 7) {
            throw new IllegalArgumentException("El día de semana debe ser un valor entre 1 (lunes) y 7 (domingo)");
        }

        AsignacionTurno asignacion = new AsignacionTurno();
        asignacion.setSemana(semana);
        asignacion.setPlantilla(plantilla);
        asignacion.setDiaSemana(dto.getDiaSemana());
        asignacion = asignacionTurnoRepository.save(asignacion);

        logger.info("Asignación creada exitosamente con ID: {}", asignacion.getIdAsignacion());
        return asignacion;
    }

    public AsignacionTurno actualizar(Long idAsignacion, AsignacionTurnoRequestDTO dto) {
        logger.info("Actualizando asignación ID: {}", idAsignacion);

        AsignacionTurno asignacion = asignacionTurnoRepository.findById(idAsignacion)
                .orElseThrow(() -> new IllegalArgumentException("Asignación no encontrada con ID: " + idAsignacion));

        PlantillaTurno plantilla = plantillaTurnoRepository.findById(dto.getIdPlantilla())
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada con ID: " + dto.getIdPlantilla()));

        if (dto.getDiaSemana() < 1 || dto.getDiaSemana() > 7) {
            throw new IllegalArgumentException("El día de semana debe ser un valor entre 1 (lunes) y 7 (domingo)");
        }

        // La semana tipo no se puede cambiar — queda igual
        asignacion.setPlantilla(plantilla);
        asignacion.setDiaSemana(dto.getDiaSemana());
        asignacion = asignacionTurnoRepository.save(asignacion);

        logger.info("Asignación actualizada exitosamente: {}", idAsignacion);
        return asignacion;
    }

    public void eliminar(Long idAsignacion) {
        logger.info("Eliminando asignación ID: {}", idAsignacion);

        if (!asignacionTurnoRepository.existsById(idAsignacion)) {
            throw new IllegalArgumentException("Asignación no encontrada con ID: " + idAsignacion);
        }

        // BUG CORREGIDO: el original pasaba idAsignacion como idTrabajador y null como fecha
        // Lo correcto es verificar si hay horarios asociados a esta asignación
        if (horarioTrabajadorRepository.existsByAsignacionIdAsignacion(idAsignacion)) {
            throw new IllegalArgumentException("No se puede eliminar la asignación porque tiene horarios de trabajador asociados");
        }

        asignacionTurnoRepository.deleteById(idAsignacion);
        logger.info("Asignación eliminada exitosamente: {}", idAsignacion);
    }
}