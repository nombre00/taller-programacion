package com.goldenburgers.gestionturnos.controller;

import com.goldenburgers.gestionturnos.dto.HorarioTrabajadorAsignarDTO;
import com.goldenburgers.gestionturnos.dto.HorarioTrabajadorRequestDTO;
import com.goldenburgers.gestionturnos.dto.HorarioTrabajadorResponseDTO;
import com.goldenburgers.gestionturnos.model.HorarioTrabajador;
import com.goldenburgers.gestionturnos.service.HorarioTrabajadorService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/horarios")
public class HorarioTrabajadorController {

    @Autowired
    private HorarioTrabajadorService horarioTrabajadorService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HorarioTrabajadorResponseDTO>> listarTodos() {
        List<HorarioTrabajador> horarios = horarioTrabajadorService.listarTodos();
        List<HorarioTrabajadorResponseDTO> response = horarios.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<HorarioTrabajadorResponseDTO> obtenerPorId(@PathVariable Long id) {
        HorarioTrabajador horario = horarioTrabajadorService.obtenerPorId(id);
        return ResponseEntity.ok(toResponseDTO(horario));
    }

    @GetMapping("/trabajador/{idTrabajador}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<List<HorarioTrabajadorResponseDTO>> listarPorTrabajador(@PathVariable Long idTrabajador) {
        List<HorarioTrabajador> horarios = horarioTrabajadorService.listarPorTrabajador(idTrabajador);
        List<HorarioTrabajadorResponseDTO> response = horarios.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/asignacion/{idAsignacion}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HorarioTrabajadorResponseDTO>> listarPorAsignacion(@PathVariable Long idAsignacion) {
        List<HorarioTrabajador> horarios = horarioTrabajadorService.listarPorAsignacion(idAsignacion);
        List<HorarioTrabajadorResponseDTO> response = horarios.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioTrabajadorResponseDTO> crear(@Valid @RequestBody HorarioTrabajadorRequestDTO dto) {
        HorarioTrabajador horario = horarioTrabajadorService.crear(dto);
        return ResponseEntity.status(201).body(toResponseDTO(horario));
    }

    @PutMapping("/{id}/estado")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<HorarioTrabajadorResponseDTO> actualizarEstado(@PathVariable Long id,
                                                                         @RequestParam String estado) {
        HorarioTrabajador horario = horarioTrabajadorService.actualizarEstado(id, estado);
        return ResponseEntity.ok(toResponseDTO(horario));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> eliminar(@PathVariable Long id) {
        horarioTrabajadorService.eliminar(id);
        return ResponseEntity.noContent().build();
    }

    // Lista los horarios asociados a un calendario.
    @GetMapping("/calendario/{idCalendario}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<HorarioTrabajadorResponseDTO>> listarPorCalendario(@PathVariable Long idCalendario) {
        List<HorarioTrabajador> horarios = horarioTrabajadorService.listarPorCalendario(idCalendario);
        List<HorarioTrabajadorResponseDTO> response = horarios.stream()
                .map(this::toResponseDTO)
                .toList();
        return ResponseEntity.ok(response);
    }

    // Asigna un trabajador a un horario.
    @PutMapping("/{id}/asignar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<HorarioTrabajadorResponseDTO> asignarTrabajador(
            @PathVariable Long id,
            @Valid @RequestBody HorarioTrabajadorAsignarDTO dto) {
        HorarioTrabajador horario = horarioTrabajadorService.asignarTrabajador(id, dto.getIdTrabajador());
        return ResponseEntity.ok(toResponseDTO(horario));
    }

    // ── mapper actualizado ───────────────────────────────────────────────────
    private HorarioTrabajadorResponseDTO toResponseDTO(HorarioTrabajador h) {
        HorarioTrabajadorResponseDTO dto = new HorarioTrabajadorResponseDTO();
        dto.setIdHorario(h.getIdHorario());
        if (h.getTrabajador() != null) {
            dto.setIdTrabajador(h.getTrabajador().getIdTrabajador());
            dto.setNombreTrabajador(h.getTrabajador().getNombre());
        }
        dto.setIdSlot(h.getSlot().getIdSlot());
        dto.setNombreSlot(h.getSlot().getNombre());
        dto.setIdPosicion(h.getSlot().getPosicion().getIdPosicion());
        dto.setNombrePosicion(h.getSlot().getPosicion().getNombre());
        dto.setIdPlantilla(h.getSlot().getPlantilla().getIdPlantilla());
        dto.setNombrePlantilla(h.getSlot().getPlantilla().getNombre());
        dto.setHoraInicio(h.getSlot().getPlantilla().getHoraInicio());
        dto.setHoraTermino(h.getSlot().getPlantilla().getHoraTermino());
        dto.setIdAsignacion(h.getAsignacion().getIdAsignacion());
        dto.setFechaTrabajo(h.getFechaTrabajo());
        dto.setEstado(h.getEstado());
        return dto;
    }
}