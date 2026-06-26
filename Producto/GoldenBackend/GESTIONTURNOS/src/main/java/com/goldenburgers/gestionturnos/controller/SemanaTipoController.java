package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.SemanaTipoRequestDTO;
import com.goldenburgers.gestionturnos.dto.SemanaTipoResponseDTO;
import com.goldenburgers.gestionturnos.model.SemanaTipo;
import com.goldenburgers.gestionturnos.service.SemanaTipoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/semanas-tipo")
public class SemanaTipoController {

    @Autowired
    private SemanaTipoService semanaTipoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SemanaTipoResponseDTO>> listarTodas() {
        List<SemanaTipo> semanas = semanaTipoService.listarTodas();
        List<SemanaTipoResponseDTO> response = semanas.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemanaTipoResponseDTO> obtenerPorId(@PathVariable Long id) {
        SemanaTipo semana = semanaTipoService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(semana));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemanaTipoResponseDTO> crear(@Valid @RequestBody SemanaTipoRequestDTO dto) {
        SemanaTipo semana = semanaTipoService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(semana));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SemanaTipoResponseDTO> actualizar(@PathVariable Long id,
                                                            @Valid @RequestBody SemanaTipoRequestDTO dto) {
        SemanaTipo semana = semanaTipoService.actualizar(id, dto);
        return ResponseEntity.ok(toResponseDTO(semana));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        semanaTipoService.desactivar(id);
        return ResponseEntity.noContent().build();
    }

    // ── mapper ──────────────────────────────────────────────────────────────
    private SemanaTipoResponseDTO toResponseDTO(SemanaTipo s) {
        SemanaTipoResponseDTO dto = new SemanaTipoResponseDTO();
        dto.setIdSemana(s.getIdSemana());
        dto.setNombre(s.getNombre());
        dto.setDescripcion(s.getDescripcion());
        dto.setActivo(s.getActivo());
        return dto;
    }
}