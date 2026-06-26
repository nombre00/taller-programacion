package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.PosicionRequestDTO;
import com.goldenburgers.gestionturnos.model.Posicion;
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
public class PosicionService {

    private static final Logger logger = LoggerFactory.getLogger(PosicionService.class);

    @Autowired
    private PosicionRepository posicionRepository;

    @Autowired
    private SlotTurnoRepository slotTurnoRepository;

    public List<Posicion> listarTodas() {
        return posicionRepository.findAll();
    }

    public Posicion obtenerPorId(Long idPosicion) {
        return posicionRepository.findById(idPosicion)
                .orElseThrow(() -> new IllegalArgumentException("Posición no encontrada con ID: " + idPosicion));
    }

    public Posicion crear(PosicionRequestDTO dto) {
        logger.info("Creando nueva posición: {}", dto.getNombre());

        if (posicionRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una posición con el nombre: " + dto.getNombre());
        }

        Posicion posicion = new Posicion();
        posicion.setNombre(dto.getNombre());
        posicion.setDescripcion(dto.getDescripcion());
        posicion.setSueldo(dto.getSueldo());
        posicion.setColor(dto.getColor());
        posicion = posicionRepository.save(posicion);

        logger.info("Posición creada exitosamente con ID: {}", posicion.getIdPosicion());
        return posicion;
    }

    public Posicion actualizar(Long idPosicion, PosicionRequestDTO dto) {
        logger.info("Actualizando posición ID: {}", idPosicion);

        Posicion posicion = posicionRepository.findById(idPosicion)
                .orElseThrow(() -> new IllegalArgumentException("Posición no encontrada con ID: " + idPosicion));

        if (!posicion.getNombre().equals(dto.getNombre()) && posicionRepository.existsByNombre(dto.getNombre())) {
            throw new IllegalArgumentException("Ya existe una posición con el nombre: " + dto.getNombre());
        }

        posicion.setNombre(dto.getNombre());
        posicion.setDescripcion(dto.getDescripcion());
        posicion.setSueldo(dto.getSueldo());
        posicion.setColor(dto.getColor());
        posicion = posicionRepository.save(posicion);

        logger.info("Posición actualizada exitosamente: {}", idPosicion);
        return posicion;
    }

    public void eliminar(Long idPosicion) {
        logger.info("Eliminando posición ID: {}", idPosicion);

        if (!posicionRepository.existsById(idPosicion)) {
            throw new IllegalArgumentException("Posición no encontrada con ID: " + idPosicion);
        }

        if (slotTurnoRepository.existsByPosicionIdPosicion(idPosicion)) {
            throw new IllegalArgumentException("No se puede eliminar la posición porque está asignada a uno o más slots de turno");
        }

        posicionRepository.deleteById(idPosicion);
        logger.info("Posición eliminada exitosamente: {}", idPosicion);
    }
}