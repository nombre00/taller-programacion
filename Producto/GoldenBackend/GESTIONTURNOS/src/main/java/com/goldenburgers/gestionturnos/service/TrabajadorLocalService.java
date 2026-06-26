package com.goldenburgers.gestionturnos.service;

import com.goldenburgers.gestionturnos.dto.TrabajadorLocalRequestDTO;
import com.goldenburgers.gestionturnos.model.Posicion;
import com.goldenburgers.gestionturnos.model.TrabajadorLocal;
import com.goldenburgers.gestionturnos.repository.HorarioTrabajadorRepository;
import com.goldenburgers.gestionturnos.repository.PosicionRepository;
import com.goldenburgers.gestionturnos.repository.TrabajadorLocalRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class TrabajadorLocalService {

    private static final Logger logger = LoggerFactory.getLogger(TrabajadorLocalService.class);

    @Autowired
    private TrabajadorLocalRepository trabajadorLocalRepository;

    @Autowired
    private PosicionRepository posicionRepository;

    @Autowired
    private HorarioTrabajadorRepository horarioTrabajadorRepository;

    public List<TrabajadorLocal> listarTodos() {
        return trabajadorLocalRepository.findAll();
    }

    public TrabajadorLocal obtenerPorId(Long idTrabajador) {
        return trabajadorLocalRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));
    }

    /**
     * Crea un trabajador local nuevo. El ID debe coincidir con el de GESTIONUSUARIO.
     * Si ya existe un trabajador con ese ID, lanza excepción (usar actualizar en su lugar).
     */
    public TrabajadorLocal crear(TrabajadorLocalRequestDTO dto) {
        logger.info("Creando trabajador local ID: {}, nombre: {}", dto.getIdTrabajador(), dto.getNombre());

        if (trabajadorLocalRepository.existsById(dto.getIdTrabajador())) {
            throw new IllegalArgumentException("Ya existe un trabajador local con ID: " + dto.getIdTrabajador() +
                    ". Use el endpoint PUT para actualizar.");
        }

        Posicion posicion = posicionRepository.findById(dto.getIdPosicion())
                .orElseThrow(() -> new IllegalArgumentException("Posición no encontrada con ID: " + dto.getIdPosicion()));

        TrabajadorLocal trabajador = new TrabajadorLocal();
        trabajador.setIdTrabajador(dto.getIdTrabajador());
        trabajador.setNombre(dto.getNombre());
        trabajador.setActivo(true);
        trabajador.setPosicion(posicion);
        trabajador = trabajadorLocalRepository.save(trabajador);

        logger.info("Trabajador local creado exitosamente con ID: {}", trabajador.getIdTrabajador());
        return trabajador;
    }

    public TrabajadorLocal actualizar(Long idTrabajador, TrabajadorLocalRequestDTO dto) {
        logger.info("Actualizando trabajador local ID: {}", idTrabajador);

        TrabajadorLocal trabajador = trabajadorLocalRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));

        Posicion posicion = posicionRepository.findById(dto.getIdPosicion())
                .orElseThrow(() -> new IllegalArgumentException("Posición no encontrada con ID: " + dto.getIdPosicion()));

        trabajador.setNombre(dto.getNombre());
        trabajador.setPosicion(posicion);
        trabajador = trabajadorLocalRepository.save(trabajador);

        logger.info("Trabajador local actualizado exitosamente: {}", idTrabajador);
        return trabajador;
    }

    public void desactivar(Long idTrabajador) {
        logger.info("Desactivando trabajador local ID: {}", idTrabajador);

        TrabajadorLocal trabajador = trabajadorLocalRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));

        boolean tieneHorariosActivos = horarioTrabajadorRepository
                .existsByTrabajadorIdTrabajadorAndEstadoIn(idTrabajador, List.of("pendiente", "confirmado"));

        if (tieneHorariosActivos) {
            throw new IllegalArgumentException(
                    "No se puede desactivar el trabajador ID: " + idTrabajador +
                    " porque tiene horarios en estado 'pendiente' o 'confirmado' asociados");
        }

        trabajador.setActivo(false);
        trabajadorLocalRepository.save(trabajador);

        logger.info("Trabajador local desactivado exitosamente: {}", idTrabajador);
    }
}