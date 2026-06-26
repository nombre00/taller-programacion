package com.example.Microservicio_Gestion_Venta.controller;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.Microservicio_Gestion_Venta.model.Venta;
import com.example.Microservicio_Gestion_Venta.service.VentaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/ventas")
@Tag(name = "Ventas", description = "API para la gestión de ventas")
public class VentaController {

    @Autowired
    private VentaService ventaService;

    @Operation(
        summary = "Listar todas las ventas",
        description = "Obtiene un listado completo de todas las ventas registradas en el sistema"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Lista de ventas obtenida exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Venta.class)
            )
        ),
        @ApiResponse(
            responseCode = "204",
            description = "No hay ventas registradas",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping()
    public ResponseEntity<List<Venta>> listaVentas() {
        List<Venta> venta = ventaService.listarVentas();
        if (venta.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(venta);
    }

    @Operation(
        summary = "Crear venta desde pedido",
        description = "Crea una nueva venta a partir de un pedido existente. Convierte automáticamente el pedido en una venta."
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "201",
            description = "Venta creada exitosamente desde el pedido",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Venta.class)
            )
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al crear la venta - Pedido no válido o no existe",
            content = @Content(mediaType = "application/json")
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR' , 'CLIENTE')")
    @PostMapping("/desde-pedido/{id}")
    public ResponseEntity<?> crearVentaDesdePedido(
        @Parameter(description = "ID del pedido a convertir en venta", required = true)
        @PathVariable Long id,
        @RequestHeader("X-Internal-Token") String token //Para recibir el token interno desde GestionPedidos
        
    ) {
        try {
            Venta venta = ventaService.crearVentaDesdePedidoconBoleta(id, token);
            return ResponseEntity.status(HttpStatus.CREATED).body(venta);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @Operation(
        summary = "Buscar venta por ID",
        description = "Obtiene los detalles de una venta específica mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Venta encontrada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Venta.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Venta no encontrada con el ID especificado",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping("/{id}")
    public ResponseEntity<Venta> buscarPorId(
        @Parameter(description = "ID de la venta a buscar", required = true)
        @PathVariable Long id
    ) {
        try {
            Venta venta = ventaService.obtenerVentaPorId(id);
            return ResponseEntity.ok(venta);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Actualizar venta existente",
        description = "Actualiza la información de una venta existente identificada por su ID"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Venta actualizada exitosamente",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Venta.class)
            )
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Venta no encontrada con el ID especificado",
            content = @Content
        )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PutMapping("/{id}")
    public ResponseEntity<Venta> actualizarVenta(
        @Parameter(description = "ID de la venta a actualizar", required = true)
        @PathVariable Long id,
        @Parameter(description = "Datos actualizados de la venta", required = true)
        @RequestBody Venta venta
    ) {
        try {
            Venta vent = ventaService.obtenerVentaPorId(id);
            
            vent.setId_pedido(venta.getId_pedido());
            vent.setTotal_venta(venta.getTotal_venta());
            vent.setFecha_venta(venta.getFecha_venta());
            
            ventaService.guardarVenta(vent);
            return ResponseEntity.ok(vent);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(
        summary = "Eliminar venta",
        description = "Elimina una venta del sistema mediante su identificador único"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "204",
            description = "Venta eliminada exitosamente",
            content = @Content
        ),
        @ApiResponse(
            responseCode = "404",
            description = "Venta no encontrada con el ID especificado",
            content = @Content
        )
    })
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> eliminarVenta(
        @Parameter(description = "ID de la venta a eliminar", required = true)
        @PathVariable Long id
    ) {
        try {
            ventaService.eliminarVenta(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }
}