package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.SlotTurnoRequestDTO;
import com.goldenburgers.gestionturnos.dto.SlotTurnoResponseDTO;
import com.goldenburgers.gestionturnos.model.SlotTurno;
import com.goldenburgers.gestionturnos.service.SlotTurnoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/slots-turno")
public class SlotTurnoController {

    @Autowired
    private SlotTurnoService slotTurnoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SlotTurnoResponseDTO>> listarTodos() {
        List<SlotTurno> slots = slotTurnoService.listarTodos();
        List<SlotTurnoResponseDTO> response = slots.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SlotTurnoResponseDTO> obtenerPorId(@PathVariable Long id) {
        SlotTurno slot = slotTurnoService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(slot));
    }

    @GetMapping("/plantilla/{idPlantilla}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SlotTurnoResponseDTO>> listarPorPlantilla(@PathVariable Long idPlantilla) {
        List<SlotTurno> slots = slotTurnoService.listarPorPlantilla(idPlantilla);
        List<SlotTurnoResponseDTO> response = slots.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SlotTurnoResponseDTO> crear(@Valid @RequestBody SlotTurnoRequestDTO dto) {
        SlotTurno slot = slotTurnoService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(slot));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SlotTurnoResponseDTO> actualizar(@PathVariable Long id,
                                                           @Valid @RequestBody SlotTurnoRequestDTO dto) {
        SlotTurno slot = slotTurnoService.actualizar(id, dto);
        return ResponseEntity.ok(toResponseDTO(slot));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        slotTurnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── mapper ──────────────────────────────────────────────────────────────
    private SlotTurnoResponseDTO toResponseDTO(SlotTurno s) {
        SlotTurnoResponseDTO dto = new SlotTurnoResponseDTO();
        dto.setIdSlot(s.getIdSlot());
        dto.setIdPlantilla(s.getPlantilla().getIdPlantilla());
        dto.setNombrePlantilla(s.getPlantilla().getNombre());
        dto.setIdPosicion(s.getPosicion().getIdPosicion());
        dto.setNombrePosicion(s.getPosicion().getNombre());
        dto.setColorPosicion(s.getPosicion().getColor());
        dto.setNombre(s.getNombre());
        dto.setCantidad(s.getCantidad());
        return dto;
    }
}