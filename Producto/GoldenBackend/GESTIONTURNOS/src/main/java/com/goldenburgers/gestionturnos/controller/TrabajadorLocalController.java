package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.TrabajadorLocalRequestDTO;
import com.goldenburgers.gestionturnos.dto.TrabajadorLocalResponseDTO;
import com.goldenburgers.gestionturnos.model.TrabajadorLocal;
import com.goldenburgers.gestionturnos.service.TrabajadorLocalService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trabajadores-local")
public class TrabajadorLocalController {

    @Autowired
    private TrabajadorLocalService trabajadorLocalService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<TrabajadorLocalResponseDTO>> listarTodos() {
        List<TrabajadorLocal> trabajadores = trabajadorLocalService.listarTodos();
        List<TrabajadorLocalResponseDTO> response = trabajadores.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrabajadorLocalResponseDTO> obtenerPorId(@PathVariable Long id) {
        TrabajadorLocal trabajador = trabajadorLocalService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(trabajador));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrabajadorLocalResponseDTO> crear(@Valid @RequestBody TrabajadorLocalRequestDTO dto) {
        TrabajadorLocal trabajador = trabajadorLocalService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(trabajador));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<TrabajadorLocalResponseDTO> actualizar(@PathVariable Long id,
                                                                 @Valid @RequestBody TrabajadorLocalRequestDTO dto) {
        TrabajadorLocal trabajador = trabajadorLocalService.actualizar(id, dto);
        return ResponseEntity.ok(toResponseDTO(trabajador));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        trabajadorLocalService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // ── mapper ──────────────────────────────────────────────────────────────
    private TrabajadorLocalResponseDTO toResponseDTO(TrabajadorLocal t) {
        TrabajadorLocalResponseDTO dto = new TrabajadorLocalResponseDTO(); 
        dto.setIdTrabajador(t.getIdTrabajador());
        dto.setNombre(t.getNombre());
        dto.setActivo(t.getActivo());
        dto.setIdPosicion(t.getPosicion().getIdPosicion());
        dto.setNombrePosicion(t.getPosicion().getNombre());
        return dto;
    }
}