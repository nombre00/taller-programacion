package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.SlotTurnoRequestDTO;
import com.goldenburgers.gestionturnos.model.PlantillaTurno;
import com.goldenburgers.gestionturnos.model.Posicion;
import com.goldenburgers.gestionturnos.model.SlotTurno;
import com.goldenburgers.gestionturnos.repository.HorarioTrabajadorRepository;
import com.goldenburgers.gestionturnos.repository.PlantillaTurnoRepository;
import com.goldenburgers.gestionturnos.repository.PosicionRepository;
import com.goldenburgers.gestionturnos.repository.SlotTurnoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class SlotTurnoService {

    private static final Logger logger = LoggerFactory.getLogger(SlotTurnoService.class);

    @Autowired
    private SlotTurnoRepository slotTurnoRepository;

    @Autowired
    private PlantillaTurnoRepository plantillaTurnoRepository;

    @Autowired
    private PosicionRepository posicionRepository;

    @Autowired
    private HorarioTrabajadorRepository horarioTrabajadorRepository;

    public List<SlotTurno> listarTodos() {
        return slotTurnoRepository.findAll();
    }

    public SlotTurno obtenerPorId(Long idSlot) {
        return slotTurnoRepository.findById(idSlot)
                .orElseThrow(() -> new IllegalArgumentException("Slot no encontrado con ID: " + idSlot));
    }

    public List<SlotTurno> listarPorPlantilla(Long idPlantilla) {
        return slotTurnoRepository.findByPlantillaIdPlantilla(idPlantilla);
    }

    public SlotTurno crear(SlotTurnoRequestDTO dto) {
        logger.info("Creando nuevo slot para plantilla ID: {} y posición ID: {}", dto.getIdPlantilla(), dto.getIdPosicion());

        PlantillaTurno plantilla = plantillaTurnoRepository.findById(dto.getIdPlantilla())
                .orElseThrow(() -> new IllegalArgumentException("Plantilla no encontrada con ID: " + dto.getIdPlantilla()));

        Posicion posicion = posicionRepository.findById(dto.getIdPosicion())
                .orElseThrow(() -> new IllegalArgumentException("Posición no encontrada con ID: " + dto.getIdPosicion()));

        SlotTurno slot = new SlotTurno();
        slot.setPlantilla(plantilla);
        slot.setPosicion(posicion);
        slot.setNombre(dto.getNombre());
        slot.setCantidad(dto.getCantidad());
        slot = slotTurnoRepository.save(slot);

        logger.info("Slot creado exitosamente con ID: {}", slot.getIdSlot());
        return slot;
    }

    public SlotTurno actualizar(Long idSlot, SlotTurnoRequestDTO dto) {
        logger.info("Actualizando slot ID: {}", idSlot);

        SlotTurno slot = slotTurnoRepository.findById(idSlot)
                .orElseThrow(() -> new IllegalArgumentException("Slot no encontrado con ID: " + idSlot));

        Posicion posicion = posicionRepository.findById(dto.getIdPosicion())
                .orElseThrow(() -> new IllegalArgumentException("Posición no encontrada con ID: " + dto.getIdPosicion()));

        slot.setPosicion(posicion);
        slot.setNombre(dto.getNombre());
        slot.setCantidad(dto.getCantidad());
        slot = slotTurnoRepository.save(slot);

        logger.info("Slot actualizado exitosamente: {}", idSlot);
        return slot;
    }

    public void eliminar(Long idSlot) {
        logger.info("Eliminando slot ID: {}", idSlot);

        if (!slotTurnoRepository.existsById(idSlot)) {
            throw new IllegalArgumentException("Slot no encontrado con ID: " + idSlot);
        }

        if (horarioTrabajadorRepository.existsBySlotIdSlot(idSlot)) {
            throw new IllegalArgumentException("No se puede eliminar el slot porque tiene horarios de trabajador asociados");
        }

        slotTurnoRepository.deleteById(idSlot);
        logger.info("Slot eliminado exitosamente: {}", idSlot);
    }
}