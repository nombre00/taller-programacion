package com.goldenburgers.gestioncuentas.controller;

import com.goldenburgers.gestioncuentas.dto.RegistrarCompraRequestDTO;
import com.goldenburgers.gestioncuentas.dto.RegistrarCompraResponseDTO;
import com.goldenburgers.gestioncuentas.service.CompraService;
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
@RequestMapping("/api/compras")
@RequiredArgsConstructor
public class CompraController {

    private final CompraService compraService;

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping
    public ResponseEntity<?> registrarCompra(
            @Valid @RequestBody RegistrarCompraRequestDTO request,
            @RequestHeader("X-Internal-Token") String token
    ) {
        try {
            RegistrarCompraResponseDTO response = compraService.registrarCompra(request, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            log.error("Error al registrar compra: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
