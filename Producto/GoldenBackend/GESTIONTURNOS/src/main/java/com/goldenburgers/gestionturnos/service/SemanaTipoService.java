package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.SemanaTipoRequestDTO;
import com.goldenburgers.gestionturnos.model.SemanaTipo;
import com.goldenburgers.gestionturnos.repository.AsignacionTurnoRepository;
import com.goldenburgers.gestionturnos.repository.CalendarioSemanaRepository;
import com.goldenburgers.gestionturnos.repository.SemanaTipoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SemanaTipoService {

    private static final Logger logger = LoggerFactory.getLogger(SemanaTipoService.class);

    @Autowired
    private SemanaTipoRepository semanaTipoRepository;

    @Autowired
    private AsignacionTurnoRepository asignacionTurnoRepository;

    @Autowired
    private CalendarioSemanaRepository calendarioSemanaRepository;

    public List<SemanaTipo> listarTodas() {
        return semanaTipoRepository.findAll();
    }

    public List<SemanaTipo> listarActivas() {
        return semanaTipoRepository.findByActivoTrue();
    }

    public SemanaTipo obtenerPorId(Long idSemana) {
        return semanaTipoRepository.findById(idSemana)
                .orElseThrow(() -> new IllegalArgumentException("Semana tipo no encontrada con ID: " + idSemana));
    }

    public SemanaTipo crear(SemanaTipoRequestDTO dto) {
        logger.info("Creando nueva semana tipo: {}", dto.getNombre());

        if (semanaTipoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una semana tipo con el nombre: " + dto.getNombre());
        }

        SemanaTipo semana = new SemanaTipo();
        semana.setNombre(dto.getNombre());
        semana.setDescripcion(dto.getDescripcion());
        semana.setActivo(true);
        semana = semanaTipoRepository.save(semana);

        logger.info("Semana tipo creada exitosamente con ID: {}", semana.getIdSemana());
        return semana;
    }

    public SemanaTipo actualizar(Long idSemana, SemanaTipoRequestDTO dto) {
        logger.info("Actualizando semana tipo ID: {}", idSemana);

        SemanaTipo semana = semanaTipoRepository.findById(idSemana)
                .orElseThrow(() -> new IllegalArgumentException("Semana tipo no encontrada con ID: " + idSemana));

        if (!semana.getNombre().equals(dto.getNombre()) && semanaTipoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una semana tipo con el nombre: " + dto.getNombre());
        }

        semana.setNombre(dto.getNombre());
        semana.setDescripcion(dto.getDescripcion());
        semana = semanaTipoRepository.save(semana);

        logger.info("Semana tipo actualizada exitosamente: {}", idSemana);
        return semana;
    }

    // Soft delete — el controller DELETE llama a este método
    public void desactivar(Long idSemana) {
        logger.info("Desactivando semana tipo ID: {}", idSemana);

        SemanaTipo semana = semanaTipoRepository.findById(idSemana)
                .orElseThrow(() -> new IllegalArgumentException("Semana tipo no encontrada con ID: " + idSemana));

        semana.setActivo(false);
        semanaTipoRepository.save(semana);

        logger.info("Semana tipo desactivada exitosamente: {}", idSemana);
    }

    // Hard delete — solo si no tiene dependencias
    public void eliminar(Long idSemana) {
        logger.info("Eliminando semana tipo ID: {}", idSemana);

        if (!semanaTipoRepository.existsById(idSemana)) {
            throw new IllegalArgumentException("Semana tipo no encontrada con ID: " + idSemana);
        }

        if (asignacionTurnoRepository.existsBySemanaIdSemana(idSemana)) {
            throw new IllegalArgumentException("No se puede eliminar la semana tipo porque tiene asignaciones de turno asociadas");
        }

        if (calendarioSemanaRepository.existsBySemanaIdSemana(idSemana)) {
            throw new IllegalArgumentException("No se puede eliminar la semana tipo porque está aplicada en el calendario");
        }

        semanaTipoRepository.deleteById(idSemana);
        logger.info("Semana tipo eliminada exitosamente: {}", idSemana);
    }
}