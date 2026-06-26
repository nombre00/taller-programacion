package com.example.Microservicio_Gestion_Venta.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Microservicio_Gestion_Venta.model.Boleta;
import com.example.Microservicio_Gestion_Venta.service.BoletaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/boletas")
@CrossOrigin(origins = "*")
@Tag(name = "Boletas", description = "API para la gestión de boletas de venta")
public class BoletaController {

    @Autowired
    private BoletaService boletaService;

    @Operation(
        summary = "Listar todas las boletas",
        description = "Obtiene un listado completo de todas las boletas emitidas en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de boletas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boleta.class)
            )
        ),
        @ApiResponse(
            responseCode = "204",
            description = "No hay boletas registradas",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping
    public ResponseEntity<List<Boleta>> listarBoletas() {
        List<Boleta> boletas = boletaService.listarBoleta();
        if (boletas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(boletas);
    }

    @Operation(
        summary = "Obtener boleta por ID",
        description = "Obtiene los detalles de una boleta específica mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Boleta encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boleta.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Boleta no encontrada con el ID especificado",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Boleta> obtenerBoletaPorId(
        @Parameter(description = "ID de la boleta a buscar", required = true)
        @PathVariable Long id
    ) {
        try {
            Boleta boleta = boletaService.obtenerBoletaPorId(id);
            return ResponseEntity.ok(boleta);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Crear nueva boleta",
        description = "Registra una nueva boleta en el sistema con su número SII y documento asociado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Boleta creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boleta.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al crear la boleta - Datos inválidos",
            content = @Content(mediaType = "application/json")
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PostMapping
    public ResponseEntity<?> crearBoleta(
        @Parameter(description = "Datos de la nueva boleta", required = true)
        @RequestBody Boleta boleta
    ) {
        try {
            Boleta nuevaBoleta = boletaService.guardarBoleta(boleta);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaBoleta);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear boleta: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Actualizar boleta existente",
        description = "Actualiza la información de una boleta existente (número SII, URL documento, IVA)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Boleta actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Boleta.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Boleta no encontrada con el ID especificado",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarBoleta(
        @Parameter(description = "ID de la boleta a actualizar", required = true)
        @PathVariable Long id,
        @Parameter(description = "Datos actualizados de la boleta", required = true)
        @RequestBody Boleta boleta
    ) {
        try {
            Boleta boletaExistente = boletaService.obtenerBoletaPorId(id);
            
            boletaExistente.setNumero_sii(boleta.getNumero_sii());
            boletaExistente.setUrl_documento(boleta.getUrl_documento());
            boletaExistente.setIva(boleta.getIva());
            
            Boleta boletaActualizada = boletaService.guardarBoleta(boletaExistente);
            return ResponseEntity.ok(boletaActualizada);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Eliminar boleta",
        description = "Elimina una boleta del sistema mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Boleta eliminada exitosamente",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Boleta no encontrada con el ID especificado",
            content = @Content(mediaType = "application/json")
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarBoleta(
        @Parameter(description = "ID de la boleta a eliminar", required = true)
        @PathVariable Long id
    ) {
        try {
            boletaService.eliminarBoleta(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Boleta no encontrada con ID: " + id));
        }
    }
}