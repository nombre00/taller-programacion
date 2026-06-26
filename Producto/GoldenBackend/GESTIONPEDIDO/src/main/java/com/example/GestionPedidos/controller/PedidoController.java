package com.example.GestionPedidos.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import com.example.GestionPedidos.model.DetallePedido;
import com.example.GestionPedidos.model.Pedido;
import com.example.GestionPedidos.service.ClienteClienteService;
import com.example.GestionPedidos.service.PedidoService;
import com.example.GestionPedidos.service.VentaClienteService;
import com.example.GestionPedidos.service.WhatsAppService;
import com.example.GestionPedidos.dto.PedidoDTO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

@RestController
@RequestMapping("/api/pedidos")
@CrossOrigin(origins = "*")
@Tag(name = "Pedidos", description = "API para la gestión de pedidos de clientes")
public class PedidoController {

    @Autowired
    private PedidoService pedidoService;

    @Autowired
    private VentaClienteService ventaClienteService;

    @Autowired
    private WhatsAppService whatsAppService;

    @Autowired
    private ClienteClienteService clienteClienteService;

    private static final Logger logger = LoggerFactory.getLogger(PedidoController.class);

    @Operation(summary = "Listar todos los pedidos", description = "Obtiene un listado completo de todos los pedidos registrados en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista de pedidos obtenida exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "204", description = "No hay pedidos registrados", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping
    public ResponseEntity<List<Pedido>> listarPedidos() {
        List<Pedido> pedidos = pedidoService.listarPedidos();
        if (pedidos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Obtener pedido por ID", description = "Obtiene los detalles de un pedido específico mediante su identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido encontrado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID especificado", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @GetMapping("/{id}")
    public ResponseEntity<Pedido> obtenerPedido(
            @Parameter(description = "ID del pedido a buscar", required = true) @PathVariable Long id) {
        try {
            Pedido pedido = pedidoService.obtenerPedidoPorId(id);
            return ResponseEntity.ok(pedido);
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Listar pedidos por cliente", description = "Obtiene todos los pedidos asociados a un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos del cliente obtenidos exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "204", description = "El cliente no tiene pedidos registrados", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @GetMapping("/cliente/{idCliente}")
    public ResponseEntity<List<Pedido>> listarPedidosPorCliente(
            @Parameter(description = "ID del cliente", required = true) @PathVariable Long idCliente) {
        List<Pedido> pedidos = pedidoService.listarPedidosPorCliente(idCliente);
        if (pedidos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(pedidos);
    }

    @Operation(summary = "Crear nuevo pedido", description = "Registra un nuevo pedido en el sistema")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Pedido creado exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "400", description = "Datos del pedido inválidos o incompletos", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @PostMapping("/completo")
    public Pedido crearPedido(@RequestBody PedidoDTO dto) {
        Pedido pedido = new Pedido();
        pedido.setIdCliente(dto.getIdCliente());
        pedido.setIdEstadoPedido(dto.getIdEstadoPedido());
        pedido.setIdMetodoPago(dto.getIdMetodoPago());
        pedido.setIdTipoEntrega(dto.getIdTipoEntrega());
        pedido.setIdDireccionEntrega(dto.getIdDireccionEntrega());
        pedido.setMontoSubtotal(dto.getMontoSubtotal());
        pedido.setMontoEnvio(dto.getMontoEnvio());
        pedido.setMontoTotal(dto.getMontoTotal());
        pedido.setFechaPedido(dto.getFechaHoraPedido());
        pedido.setNotaCliente(dto.getNotasCliente());

        List<DetallePedido> detalles = dto.getDetalles()
                .stream()
                .map(d -> new DetallePedido(
                        null,
                        null,
                        d.getIdProducto(),
                        d.getCantidad(),
                        d.getPrecioUnitario(),
                        d.getSubtotalLinea()
                ))
                .toList();

        return pedidoService.crearPedidoConDetalles(pedido, detalles);
    }

    @Operation(summary = "Cancelar/Eliminar pedido", description = "Elimina un pedido del sistema mediante su identificador único")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido eliminado exitosamente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID especificado", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'CLIENTE')")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelarPedido(
            @Parameter(description = "ID del pedido a eliminar", required = true) @PathVariable Long id) {
        try {
            pedidoService.eliminarPedido(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Actualizar pedido", description = "Actualiza un pedido existente en el sistema, ya sea Pendiente de Pago, Recibido, En preparación, En camino, Entregado, Cancelado")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Pedido actualizado exitosamente", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID especificado", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @PutMapping("/cambiar-estado/{idPedido}/estado/{idEstado}")
    public ResponseEntity<Pedido> cambiarEstadoPedido(@PathVariable Long idPedido, @PathVariable Long idEstado) {
        Pedido pedido = pedidoService.cambiarEstadoPedido(idPedido, idEstado);
        return ResponseEntity.ok(pedido);
    }


    // Confirmar pedido con ambos flujos para mensaje a wasap y sin mensaje a wasap.
    @Operation(summary = "Actualizar pedido a PAGADO",
            description = "Actualiza un pedido a Pagado y genera la venta automáticamente con boleta. " +
                          "Si se provee tiempoEspera, envía confirmación por WhatsApp al cliente. " +
                          "Si tiempoEspera es null, solo confirma el pedido sin enviar WhatsApp.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedido actualizado exitosamente, con venta y boleta realizada", content = @Content),
            @ApiResponse(responseCode = "404", description = "Pedido no encontrado con el ID especificado", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @PutMapping("/procesar/{idPedido}")
    public ResponseEntity<Pedido> actualizarEstadoPedidoPagado(
            @PathVariable Long idPedido,
            @RequestParam(required = false) Integer tiempoEspera, // null = sin WhatsApp
            @RequestHeader("X-Internal-Token") String token) {

        // 1. Actualizar estado a "pagado"
        Pedido pedido = pedidoService.cambiarEstadoPedido(idPedido, 2L);

        // 2. Generar venta automática
        try {
            ventaClienteService.crearVentaDesdePedido(idPedido, token);
        } catch (Exception e) {
            System.err.println("No se pudo crear la venta automáticamente: " + e.getMessage());
        }

        // 3. Enviar WhatsApp solo si se proporcionó tiempoEspera
        if (tiempoEspera != null) {
            try {
                String telefono = clienteClienteService.obtenerTelefonoCliente(pedido.getIdCliente(), token);
                if (telefono != null) {
                    whatsAppService.enviarConfirmacionPedido(telefono, idPedido, tiempoEspera);
                } else {
                    logger.warn("No se obtuvo teléfono para cliente {}, WhatsApp no enviado", pedido.getIdCliente());
                }
            } catch (Exception e) {
                logger.error("Error al enviar WhatsApp al cliente {}: {}", pedido.getIdCliente(), e.getMessage());
            }
        } else {
            logger.info("Pedido {} confirmado sin notificación WhatsApp", idPedido);
        }
        return ResponseEntity.ok(pedido);
    }

    @Operation(summary = "Listar detalles de pedidos por cliente", description = "Obtiene todos los pedidos asociados a un cliente específico")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Pedidos del cliente obtenidos exitosamente", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Pedido.class))),
            @ApiResponse(responseCode = "204", description = "El cliente no tiene productos seleccionados", content = @Content)
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @GetMapping("/detalles/cliente/{idCliente}")
    public ResponseEntity<List<DetallePedido>> obtenerDetallesCliente(@PathVariable Long idCliente) {
        List<DetallePedido> detalles = pedidoService.obtenerDetallesPorPedido(idCliente);
        if (detalles.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(detalles);
    }
}