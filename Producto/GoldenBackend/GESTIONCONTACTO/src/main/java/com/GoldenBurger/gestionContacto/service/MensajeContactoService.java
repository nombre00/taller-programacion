package com.GoldenBurger.gestionContacto.service;

import com.GoldenBurger.gestionContacto.model.MensajeContacto;
import com.GoldenBurger.gestionContacto.repository.MensajeContactoRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
@Service
public class MensajeContactoService {

    private final MensajeContactoRepository repository;

    public MensajeContactoService(MensajeContactoRepository repository) {
        this.repository = repository;
    }

    public List<MensajeContacto> listarMensajes() {
        return repository.findAll();
    }

    public Optional<MensajeContacto> obtenerPorId(Long id) {
        return repository.findById(id);
    }

    // ======================================================
    // GUARDAR MENSAJE (siempre inicia como NO LEÍDO = 0)
    // ======================================================
    public MensajeContacto guardar(MensajeContacto mensaje) {

        // Forzar estado inicial
        mensaje.setLeido(0);

        // Asegurar que Oracle genere el ID
        mensaje.setIdMensaje(null);

        return repository.save(mensaje);
    }

    // ======================================================
    // ELIMINAR
    // ======================================================
    public void eliminar(Long id) {
        repository.deleteById(id);
    }

    // ======================================================
    // ACTUALIZAR SOLO EL ESTADO (0=No leído, 1=Leído, 2=Respondido)
    // ======================================================
    public MensajeContacto actualizarEstado(Long id, Integer estado) {

        if (estado != 0 && estado != 1 && estado != 2) {
            throw new IllegalArgumentException("Estado inválido. Solo se permite 0, 1 o 2.");
        }

        MensajeContacto msg = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Mensaje no encontrado"));

        msg.setLeido(estado);

        return repository.save(msg);
    }

    // ======================================================
    // LISTAR POR ESTADO
    // ======================================================
    public List<MensajeContacto> listarPorEstado(Integer estado) {
        return repository.findByLeido(estado);
    }
}
