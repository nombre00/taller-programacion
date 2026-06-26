package com.goldenburgers.catalogo.controller;

import com.goldenburgers.catalogo.dto.MovimientoStockRequestDTO;
import com.goldenburgers.catalogo.dto.MovimientoStockResponseDTO;
import com.goldenburgers.catalogo.service.MovimientoStockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/movimientos-stock")
@RequiredArgsConstructor
@Tag(name = "Movimientos de Stock", description = "Gestión de entradas, salidas y ajustes de stock")
public class MovimientoStockController {

    private final MovimientoStockService movimientoStockService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todos los movimientos")
    public ResponseEntity<List<MovimientoStockResponseDTO>> listarTodos() {
        return ResponseEntity.ok(movimientoStockService.listarTodos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Obtener movimiento por ID")
    public ResponseEntity<MovimientoStockResponseDTO> obtenerPorId(@PathVariable Long id) {
        return ResponseEntity.ok(movimientoStockService.obtenerPorId(id));
    }

    @GetMapping("/materia-prima/{idMateriaPrima}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar movimientos de una materia prima")
    public ResponseEntity<List<MovimientoStockResponseDTO>> listarPorMateriaPrima(
            @PathVariable Long idMateriaPrima) {
        return ResponseEntity.ok(movimientoStockService.listarPorMateriaPrima(idMateriaPrima));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar movimiento manual de stock")
    public ResponseEntity<MovimientoStockResponseDTO> registrar(
            @Valid @RequestBody MovimientoStockRequestDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(movimientoStockService.registrar(request));
    }

    @PostMapping("/descontar-por-venta")
    @PreAuthorize("hasRole('ADMIN') or hasRole('TRABAJADOR')")
    @Operation(summary = "Descuenta stock según la receta del producto vendido (llamado por GESTIONVENTA)")
    public ResponseEntity<Void> descontarPorVenta(
            @RequestParam Long idProducto,
            @RequestParam BigDecimal cantidad,
            @RequestParam(required = false) Long idVenta) {
        movimientoStockService.descontarStockPorVenta(idProducto, cantidad, idVenta);
        return ResponseEntity.noContent().build();
    }
}