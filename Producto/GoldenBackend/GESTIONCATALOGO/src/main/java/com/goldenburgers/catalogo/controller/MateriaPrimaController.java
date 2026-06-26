package com.goldenburgers.catalogo.controller;

import com.goldenburgers.catalogo.dto.DescontarPorVentaRequestDTO;
import com.goldenburgers.catalogo.dto.IngresarPorCompraRequestDTO;
import com.goldenburgers.catalogo.dto.StockOperacionResponseDTO;
import com.goldenburgers.catalogo.service.MateriaPrimaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.goldenburgers.catalogo.dto.MateriaPrimaRequestDTO;
import com.goldenburgers.catalogo.dto.MateriaPrimaResponseDTO;
import org.springframework.http.HttpStatus;
import java.util.List;

@RestController
@RequestMapping("/api/materias-primas")
@RequiredArgsConstructor
public class MateriaPrimaController {

    private final MateriaPrimaService materiaPrimaService;


    // ── CRUD ──────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MateriaPrimaResponseDTO>> listarTodas() {
        return ResponseEntity.ok(materiaPrimaService.listarTodas());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MateriaPrimaResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(materiaPrimaService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MateriaPrimaResponseDTO> crear(
            @Valid @RequestBody MateriaPrimaRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(materiaPrimaService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<MateriaPrimaResponseDTO> actualizar(
            @PathVariable Long id,
            @Valid @RequestBody MateriaPrimaRequestDTO request) {
        return ResponseEntity.ok(materiaPrimaService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> desactivar(@PathVariable Long id) {
        materiaPrimaService.desactivar(id);
        return ResponseEntity.noContent().build();
    }





    // Compra de materias primas.
    @PostMapping("/ingresar-por-compra")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<StockOperacionResponseDTO> ingresarPorCompra(
            @Valid @RequestBody IngresarPorCompraRequestDTO request) {

        StockOperacionResponseDTO response = materiaPrimaService.ingresarPorCompra(request);
        return ResponseEntity.ok(response);
    }


    // Descuento de materia prima por una venta de producto o productos.
    @PostMapping("/descontar-por-venta")
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    public ResponseEntity<StockOperacionResponseDTO> descontarPorVenta(
            @Valid @RequestBody DescontarPorVentaRequestDTO request) {

        StockOperacionResponseDTO response = materiaPrimaService.descontarPorVenta(request);
        return ResponseEntity.ok(response);
    }
}
