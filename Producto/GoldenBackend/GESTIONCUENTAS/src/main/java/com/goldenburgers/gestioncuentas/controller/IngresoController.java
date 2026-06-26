package com.goldenburgers.gestioncuentas.controller;

import com.goldenburgers.gestioncuentas.dto.RegistrarIngresoRequestDTO;
import com.goldenburgers.gestioncuentas.dto.RegistrarIngresoResponseDTO;
import com.goldenburgers.gestioncuentas.service.IngresoService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/ingresos")
@RequiredArgsConstructor
public class IngresoController {

    private final IngresoService ingresoService;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<?> registrarIngreso(
            @Valid @RequestBody RegistrarIngresoRequestDTO request,
            @RequestHeader("X-Internal-Token") String token
    ) {
        try {
            RegistrarIngresoResponseDTO response = ingresoService.registrarIngreso(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error al registrar ingreso: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}