package com.goldenburgers.gestioncuentas.controller;

import com.goldenburgers.gestioncuentas.dto.CuentaPorPagarResponseDTO;
import com.goldenburgers.gestioncuentas.service.CuentaPorPagarService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/cuentas-por-pagar")
@RequiredArgsConstructor
public class CuentaPorPagarController {

    private final CuentaPorPagarService cuentaPorPagarService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CuentaPorPagarResponseDTO>> listarTodas(
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(cuentaPorPagarService.listarTodas(estado));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CuentaPorPagarResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(cuentaPorPagarService.obtenerPorId(id));
    }

    @GetMapping("/proveedor/{idProveedor}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CuentaPorPagarResponseDTO>> listarPorProveedor(@PathVariable Long idProveedor) {
        return ResponseEntity.ok(cuentaPorPagarService.listarPorProveedor(idProveedor));
    }

    @GetMapping("/buscar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CuentaPorPagarResponseDTO>> buscar(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmisionDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaEmisionHasta,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimientoDesde,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaVencimientoHasta) {
        return ResponseEntity.ok(cuentaPorPagarService.buscarConFiltros(
                estado,
                fechaEmisionDesde,
                fechaEmisionHasta,
                fechaVencimientoDesde,
                fechaVencimientoHasta));
    }
}