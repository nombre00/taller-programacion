package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.PlantillaTurnoRequestDTO;
import com.goldenburgers.gestionturnos.dto.PlantillaTurnoResponseDTO;
import com.goldenburgers.gestionturnos.dto.SlotTurnoResponseDTO;
import com.goldenburgers.gestionturnos.model.PlantillaTurno;
import com.goldenburgers.gestionturnos.model.SlotTurno;
import com.goldenburgers.gestionturnos.service.PlantillaTurnoService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/plantillas-turno")
public class PlantillaTurnoController {

    @Autowired
    private PlantillaTurnoService plantillaTurnoService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PlantillaTurnoResponseDTO>> listarTodas() {
        List<PlantillaTurno> plantillas = plantillaTurnoService.listarTodas();
        List<PlantillaTurnoResponseDTO> response = plantillas.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlantillaTurnoResponseDTO> obtenerPorId(@PathVariable Long id) {
        PlantillaTurno plantilla = plantillaTurnoService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(plantilla));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlantillaTurnoResponseDTO> crear(@Valid @RequestBody PlantillaTurnoRequestDTO dto) {
        PlantillaTurno plantilla = plantillaTurnoService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(plantilla));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PlantillaTurnoResponseDTO> actualizar(@PathVariable Long id,
                                                                @Valid @RequestBody PlantillaTurnoRequestDTO dto) {
        PlantillaTurno plantilla = plantillaTurnoService.actualizar(id, dto);
        return ResponseEntity.ok(toResponseDTO(plantilla));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        plantillaTurnoService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // ── mappers ─────────────────────────────────────────────────────────────
    private PlantillaTurnoResponseDTO toResponseDTO(PlantillaTurno p) {
        PlantillaTurnoResponseDTO dto = new PlantillaTurnoResponseDTO();
        dto.setIdPlantilla(p.getIdPlantilla());
        dto.setNombre(p.getNombre());
        dto.setHoraInicio(p.getHoraInicio());
        dto.setHoraTermino(p.getHoraTermino());
        dto.setDescripcion(p.getDescripcion());
        dto.setSlots(p.getSlots().stream().map(this::toSlotDTO).toList());
        return dto;
    }

    private SlotTurnoResponseDTO toSlotDTO(SlotTurno s) {
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