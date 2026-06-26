package com.example.Microservicio_Gestion_Venta.controller;

import com.example.Microservicio_Gestion_Venta.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
//import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@CrossOrigin(origins = "*")
@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
@Tag(name = "Dashboard", description = "API para métricas y estadísticas de ventas del dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    // --- NUEVOS ENDPOINTS ---
    
    @Operation(
        summary = "Obtener resumen completo de ventas",
        description = "Retorna un resumen con las ventas de hoy, mes actual y año actual"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Resumen obtenido exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/resumen-ventas")
    public ResponseEntity<?> getResumenVentas() {
        try {
            Map<String, Object> resumen = dashboardService.getResumenVentasCompleto();
            return ResponseEntity.ok(resumen);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener resumen de ventas",
                "detalle", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Obtener ventas del día actual",
        description = "Retorna las ventas realizadas en el día de hoy"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ventas del día obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/ventas-hoy")
    public ResponseEntity<?> getVentasHoy() {
        try {
            Map<String, Object> ventas = dashboardService.getVentasHoy();
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener ventas de hoy",
                "detalle", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Obtener ventas del mes actual",
        description = "Retorna las ventas realizadas en el mes en curso"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ventas del mes obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/ventas-mes-actual")
    public ResponseEntity<?> getVentasMesActual() {
        try {
            Map<String, Object> ventas = dashboardService.getVentasMesActual();
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener ventas del mes",
                "detalle", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Obtener ventas del año actual",
        description = "Retorna las ventas realizadas en el año en curso"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ventas del año obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/ventas-anio-actual")
    public ResponseEntity<?> getVentasAnioActual() {
        try {
            Map<String, Object> ventas = dashboardService.getVentasAnioActual();
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener ventas del año",
                "detalle", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Obtener ventas por mes del año actual",
        description = "Retorna las ventas agrupadas por mes para gráficos estadísticos"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ventas por mes obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/ventas-por-mes")
    public ResponseEntity<?> getVentasPorMes() {
        try {
            List<Map<String, Object>> ventas = dashboardService.getVentasPorMesAnioActual();
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener ventas por mes",
                "detalle", e.getMessage()
            ));
        }
    }

    // --- ENDPOINTS ORIGINALES ---
    
    @Operation(
        summary = "Obtener KPIs del dashboard",
        description = "Retorna los indicadores clave de rendimiento según el periodo especificado"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "KPIs obtenidos exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/kpis")
    public ResponseEntity<?> obtenerKPIs(
        @Parameter(description = "Periodo de análisis (ej: 'hoy', 'mes', 'anio')", required = true)
        @RequestParam String periodo
    ) {
        try {
            Map<String, Object> kpis = dashboardService.obtenerKPIs(periodo);
            return ResponseEntity.ok(kpis);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener KPIs",
                "detalle", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Obtener ventas por categoría",
        description = "Retorna las ventas agrupadas por categoría de producto según el periodo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ventas por categoría obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/ventas-categoria")
    public ResponseEntity<?> obtenerVentasPorCategoria(
        @Parameter(description = "Periodo de análisis", required = true)
        @RequestParam String periodo
    ) {
        try {
            List<Map<String, Object>> ventas = dashboardService.getVentasPorCategoria(periodo);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener ventas por categoría",
                "detalle", e.getMessage()
            ));
        }
    }

    @Operation(
        summary = "Obtener ventas por ciudad",
        description = "Retorna las ventas agrupadas por ciudad según el periodo"
    )
    @ApiResponses(value = {
        @ApiResponse(
            responseCode = "200",
            description = "Ventas por ciudad obtenidas exitosamente",
            content = @Content(mediaType = "application/json")
        ),
        @ApiResponse(
            responseCode = "400",
            description = "Error al procesar la solicitud",
            content = @Content(mediaType = "application/json")
        )
    })
    @GetMapping("/ventas-ciudad")
    public ResponseEntity<?> obtenerVentasPorCiudad(
        @Parameter(description = "Periodo de análisis", required = true)
        @RequestParam String periodo
    ) {
        try {
            List<Map<String, Object>> ventas = dashboardService.getVentasPorCiudad(periodo);
            return ResponseEntity.ok(ventas);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of(
                "error", "Error al obtener ventas por ciudad",
                "detalle", e.getMessage()
            ));
        }
    }
}