package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.CalendarioSemanaRequestDTO;
import com.goldenburgers.gestionturnos.dto.CalendarioSemanaResponseDTO;
import com.goldenburgers.gestionturnos.model.CalendarioSemana;
import com.goldenburgers.gestionturnos.service.CalendarioSemanaService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/calendario-semanas")
public class CalendarioSemanaController {

    @Autowired
    private CalendarioSemanaService calendarioSemanaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CalendarioSemanaResponseDTO>> listarTodos() {
        List<CalendarioSemana> calendarios = calendarioSemanaService.listarTodos();
        List<CalendarioSemanaResponseDTO> response = calendarios.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CalendarioSemanaResponseDTO> obtenerPorId(@PathVariable Long id) {
        CalendarioSemana calendario = calendarioSemanaService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(calendario));
    }

    @GetMapping("/semana/{idSemana}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CalendarioSemanaResponseDTO>> listarPorSemana(@PathVariable Long idSemana) {
        List<CalendarioSemana> calendarios = calendarioSemanaService.listarPorSemana(idSemana);
        List<CalendarioSemanaResponseDTO> response = calendarios.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CalendarioSemanaResponseDTO> crear(@Valid @RequestBody CalendarioSemanaRequestDTO dto) {
        CalendarioSemana calendario = calendarioSemanaService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(calendario));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CalendarioSemanaResponseDTO> actualizar(@PathVariable Long id,
                                                                  @Valid @RequestBody CalendarioSemanaRequestDTO dto) {
        CalendarioSemana calendario = calendarioSemanaService.actualizar(id, dto);
        return ResponseEntity.ok(toResponseDTO(calendario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        calendarioSemanaService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── mapper ──────────────────────────────────────────────────────────────
    private CalendarioSemanaResponseDTO toResponseDTO(CalendarioSemana c) {
        CalendarioSemanaResponseDTO dto = new CalendarioSemanaResponseDTO();
        dto.setIdCalendario(c.getIdCalendario());
        dto.setIdSemana(c.getSemana().getIdSemana());
        dto.setNombreSemana(c.getSemana().getNombre());
        dto.setFechaInicio(c.getFechaInicio());
        dto.setFechaFin(c.getFechaFin());
        dto.setRepeticionAnual(c.getRepeticionAnual());
        return dto;
    }
}