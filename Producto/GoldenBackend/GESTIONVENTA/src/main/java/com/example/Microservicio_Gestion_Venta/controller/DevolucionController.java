package com.example.Microservicio_Gestion_Venta.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.Microservicio_Gestion_Venta.model.Devolucion;
import com.example.Microservicio_Gestion_Venta.service.DevolucionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/devoluciones")
@CrossOrigin(origins = "*")
@Tag(name = "Devoluciones", description = "API para la gestión de devoluciones de productos")
public class DevolucionController {

    @Autowired
    private DevolucionService devolucionService;

    @Operation(
        summary = "Listar todas las devoluciones",
        description = "Obtiene un listado completo de todas las devoluciones registradas en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de devoluciones obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Devolucion.class)
            )
        ),
        @ApiResponse(
            responseCode = "204",
            description = "No hay devoluciones registradas",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping
    public ResponseEntity<List<Devolucion>> listarDevoluciones() {
        List<Devolucion> devoluciones = devolucionService.listarDev();
        if (devoluciones.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(devoluciones);
    }

    @Operation(
        summary = "Obtener devolución por ID",
        description = "Obtiene los detalles de una devolución específica mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Devolución encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Devolucion.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Devolución no encontrada con el ID especificado",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Devolucion> obtenerDevolucionPorId(
        @Parameter(description = "ID de la devolución a buscar", required = true)
        @PathVariable Long id
    ) {
        try {
            Devolucion devolucion = devolucionService.obtenerDevolucionPorId(id);
            return ResponseEntity.ok(devolucion);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Crear nueva devolución",
        description = "Registra una nueva devolución en el sistema con motivo, monto devuelto y fecha"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Devolución creada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Devolucion.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al crear la devolución - Datos inválidos",
            content = @Content(mediaType = "application/json")
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PostMapping
    public ResponseEntity<?> crearDevolucion(
        @Parameter(description = "Datos de la nueva devolución", required = true)
        @RequestBody Devolucion devolucion
    ) {
        try {
            Devolucion nuevaDevolucion = devolucionService.guardarDev(devolucion);
            return ResponseEntity.status(HttpStatus.CREATED).body(nuevaDevolucion);
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Error al crear devolución: " + e.getMessage()));
        }
    }

    @Operation(
        summary = "Actualizar devolución existente",
        description = "Actualiza la información de una devolución existente (monto, motivo, fecha)"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Devolución actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Devolucion.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Devolución no encontrada con el ID especificado",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<?> actualizarDevolucion(
        @Parameter(description = "ID de la devolución a actualizar", required = true)
        @PathVariable Long id,
        @Parameter(description = "Datos actualizados de la devolución", required = true)
        @RequestBody Devolucion devolucion
    ) {
        try {
            Devolucion devolucionExistente = devolucionService.obtenerDevolucionPorId(id);
            
            devolucionExistente.setMontoDevuelto(devolucion.getMontoDevuelto());
            devolucionExistente.setMotivo(devolucion.getMotivo());
            devolucionExistente.setFechaDevolucion(devolucion.getFechaDevolucion());
            
            Devolucion devolucionActualizada = devolucionService.guardarDev(devolucionExistente);
            return ResponseEntity.ok(devolucionActualizada);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Eliminar devolución",
        description = "Elimina una devolución del sistema mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Devolución eliminada exitosamente",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Devolución no encontrada con el ID especificado",
            content = @Content(mediaType = "application/json")
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> eliminarDevolucion(
        @Parameter(description = "ID de la devolución a eliminar", required = true)
        @PathVariable Long id
    ) {
        try {
            devolucionService.eliminarDev(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", "Devolución no encontrada con ID: " + id));
        }
    }
}