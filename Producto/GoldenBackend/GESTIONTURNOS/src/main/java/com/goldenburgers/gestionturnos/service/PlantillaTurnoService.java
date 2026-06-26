package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.PlantillaTurnoRequestDTO;
import com.goldenburgers.gestionturnos.model.PlantillaTurno;
import com.goldenburgers.gestionturnos.repository.AsignacionTurnoRepository;
import com.goldenburgers.gestionturnos.repository.PlantillaTurnoRepository;
import com.goldenburgers.gestionturnos.repository.SlotTurnoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class PlantillaTurnoService {

    private static final Logger logger = LoggerFactory.getLogger(PlantillaTurnoService.class);

    @Autowired
    private PlantillaTurnoRepository plantillaTurnoRepository;

    @Autowired
    private SlotTurnoRepository slotTurnoRepository;

    @Autowired
    private AsignacionTurnoRepository asignacionTurnoRepository;

    public List<PlantillaTurno> listarTodas() {
        return plantillaTurnoRepository.findAll();
    }

    public PlantillaTurno obtenerPorId(Long idPlantilla) {
        return plantillaTurnoRepository.findById(idPlantilla)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada con ID: " + idPlantilla));
    }

    public PlantillaTurno crear(PlantillaTurnoRequestDTO dto) {
        logger.info("Creando nueva plantilla de turno: {}", dto.getNombre());

        if (plantillaTurnoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una plantilla con el nombre: " + dto.getNombre());
        }

        PlantillaTurno plantilla = new PlantillaTurno();
        plantilla.setNombre(dto.getNombre());
        plantilla.setHoraInicio(dto.getHoraInicio());
        plantilla.setHoraTermino(dto.getHoraTermino());
        plantilla.setDescripcion(dto.getDescripcion());
        plantilla = plantillaTurnoRepository.save(plantilla);

        logger.info("Plantilla creada exitosamente con ID: {}", plantilla.getIdPlantilla());
        return plantilla;
    }

    public PlantillaTurno actualizar(Long idPlantilla, PlantillaTurnoRequestDTO dto) {
        logger.info("Actualizando plantilla ID: {}", idPlantilla);

        PlantillaTurno plantilla = plantillaTurnoRepository.findById(idPlantilla)
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada con ID: " + idPlantilla));

        if (!plantilla.getNombre().equals(dto.getNombre()) && plantillaTurnoRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una plantilla con el nombre: " + dto.getNombre());
        }

        plantilla.setNombre(dto.getNombre());
        plantilla.setHoraInicio(dto.getHoraInicio());
        plantilla.setHoraTermino(dto.getHoraTermino());
        plantilla.setDescripcion(dto.getDescripcion());
        plantilla = plantillaTurnoRepository.save(plantilla);

        logger.info("Plantilla actualizada exitosamente: {}", idPlantilla);
        return plantilla;
    }

    public void eliminar(Long idPlantilla) {
        logger.info("Eliminando plantilla ID: {}", idPlantilla);

        if (!plantillaTurnoRepository.existsById(idPlantilla)) {
            throw new IllegalArgumentException("Plantilla no encontrada con ID: " + idPlantilla);
        }

        if (slotTurnoRepository.existsByPlantillaIdPlantilla(idPlantilla)) {
            throw new IllegalArgumentException("No se puede eliminar la plantilla porque tiene slots de turno asociados");
        }

        if (asignacionTurnoRepository.existsByPlantillaIdPlantilla(idPlantilla)) {
            throw new IllegalArgumentException("No se puede eliminar la plantilla porque está asignada en una o más semanas tipo");
        }

        plantillaTurnoRepository.deleteById(idPlantilla);
        logger.info("Plantilla eliminada exitosamente: {}", idPlantilla);
    }
}