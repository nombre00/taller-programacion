package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.AsignacionTurnoRequestDTO;
import com.goldenburgers.gestionturnos.dto.AsignacionTurnoResponseDTO;
import com.goldenburgers.gestionturnos.model.AsignacionTurno;
import com.goldenburgers.gestionturnos.service.AsignacionTurnoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/asignaciones-turno")
public class AsignacionTurnoController {

    @Autowired
    private AsignacionTurnoService asignacionTurnoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AsignacionTurnoResponseDTO>> listarTodas() {
        List<AsignacionTurno> asignaciones = asignacionTurnoService.listarTodas();
        List<AsignacionTurnoResponseDTO> response = asignaciones.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AsignacionTurnoResponseDTO> obtenerPorId(@PathVariable Long id) {
        AsignacionTurno asignacion = asignacionTurnoService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(asignacion));
    }

    @GetMapping("/semana/{idSemana}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AsignacionTurnoResponseDTO>> listarPorSemana(@PathVariable Long idSemana) {
        List<AsignacionTurno> asignaciones = asignacionTurnoService.listarPorSemana(idSemana);
        List<AsignacionTurnoResponseDTO> response = asignaciones.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AsignacionTurnoResponseDTO> crear(@Valid @RequestBody AsignacionTurnoRequestDTO dto) {
        AsignacionTurno asignacion = asignacionTurnoService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(asignacion));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AsignacionTurnoResponseDTO> actualizar(@PathVariable Long id,
                                                                 @Valid @RequestBody AsignacionTurnoRequestDTO dto) {
        AsignacionTurno asignacion = asignacionTurnoService.actualizar(id, dto);
        return ResponseEntity.ok(toResponseDTO(asignacion));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        asignacionTurnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── mapper ──────────────────────────────────────────────────────────────
    private AsignacionTurnoResponseDTO toResponseDTO(AsignacionTurno a) {
        AsignacionTurnoResponseDTO dto = new AsignacionTurnoResponseDTO();
        dto.setIdAsignacion(a.getIdAsignacion());
        dto.setIdSemana(a.getSemana().getIdSemana());
        dto.setNombreSemana(a.getSemana().getNombre());
        dto.setIdPlantilla(a.getPlantilla().getIdPlantilla());
        dto.setNombrePlantilla(a.getPlantilla().getNombre());
        dto.setDiaSemana(a.getDiaSemana());
        return dto;
    }
}