package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.PosicionRequestDTO;
import com.goldenburgers.gestionturnos.dto.PosicionResponseDTO;
import com.goldenburgers.gestionturnos.model.Posicion;
import com.goldenburgers.gestionturnos.service.PosicionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/posiciones")
public class PosicionController {

    @Autowired
    private PosicionService posicionService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PosicionResponseDTO>> listarTodas() {
        List<Posicion> posiciones = posicionService.listarTodas();
        List<PosicionResponseDTO> response = posiciones.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PosicionResponseDTO> obtenerPorId(@PathVariable Long id) {
        Posicion posicion = posicionService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(posicion));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PosicionResponseDTO> crear(@Valid @RequestBody PosicionRequestDTO dto) {
        Posicion posicion = posicionService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(posicion));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PosicionResponseDTO> actualizar(@PathVariable Long id,
                                                          @Valid @RequestBody PosicionRequestDTO dto) {
        Posicion posicion = posicionService.actualizar(id, dto);
        return ResponseEntity.ok(toResponseDTO(posicion));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        posicionService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── mapper ──────────────────────────────────────────────────────────────
    private PosicionResponseDTO toResponseDTO(Posicion p) {
        PosicionResponseDTO dto = new PosicionResponseDTO();
        dto.setIdPosicion(p.getIdPosicion());
        dto.setNombre(p.getNombre());
        dto.setDescripcion(p.getDescripcion());
        dto.setSueldo(p.getSueldo());
        dto.setColor(p.getColor());
        return dto;
    }
}