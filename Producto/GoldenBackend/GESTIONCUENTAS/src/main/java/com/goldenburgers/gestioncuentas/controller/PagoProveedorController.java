package com.goldenburgers.gestioncuentas.controller;

import com.goldenburgers.gestioncuentas.dto.PagoProveedorRequestDTO;
import com.goldenburgers.gestioncuentas.dto.PagoProveedorResponseDTO;
import com.goldenburgers.gestioncuentas.service.PagoProveedorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pagos-proveedor")
@RequiredArgsConstructor
public class PagoProveedorController {

    private final PagoProveedorService pagoProveedorService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagoProveedorResponseDTO> registrarPago(@Valid @RequestBody PagoProveedorRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoProveedorService.registrarPago(dto));
    }

    @GetMapping("/cuenta/{idCuenta}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PagoProveedorResponseDTO>> listarPorCuenta(@PathVariable Long idCuenta) {
        return ResponseEntity.ok(pagoProveedorService.listarPorCuenta(idCuenta));
    }
}