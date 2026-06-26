package com.example.GestionPedidos.controller;

// Módulos del servicio importados
import com.example.GestionPedidos.dto.CrearPreferenciaRequest;
import com.example.GestionPedidos.dto.webpay.IniciarWebpayResponse;
import com.example.GestionPedidos.model.Pago;
import com.example.GestionPedidos.service.PagoService;

// Webpay
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;

// Swagger
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

// Logs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Framekork
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

// Utilidades
import java.util.HashMap;
import java.util.List;
import java.util.Map;



/**
 * Controlador REST para gestionar los pagos con Mercado Pago
 * Este controlador maneja todos los endpoints relacionados con pagos
 */
@RestController
@RequestMapping("/api/pagos")  // Todas las rutas empiezan con /api/pagos
@Tag(name = "Pagos", description = "Gestión de pagos con Mercado Pago")
@SecurityRequirement(name = "bearer-jwt")  // Requiere autenticación JWT
public class PagoController {

    Logger logger = LoggerFactory.getLogger(PagoController.class);

    // Inyección de dependencias - Spring crea automáticamente el servicio
    @Autowired
    private PagoService pagoService;

    /**
     * Endpoint para crear una preferencia de pago en Mercado Pago
     * POST /api/pagos/crear-preferencia
     * Pueden acceder: ADMIN, TRABAJADOR y CLIENTE
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @PostMapping("/crear-preferencia")
    @Operation(summary = "Crear preferencia de pago")
    public ResponseEntity<?> crearPreferenciaPago(@RequestBody CrearPreferenciaRequest request) {
        try {
            // Llamar al servicio para crear la preferencia
            Pago pago = pagoService.crearPreferenciaPago(request);
            
            // Crear un objeto Map para la respuesta JSON
            Map<String, Object> response = new HashMap<>();
            response.put("idPago", pago.getIdPago());
            response.put("idPedido", pago.getIdPedido());
            response.put("montoPago", pago.getMontoPago());
            response.put("estadoPago", pago.getEstadoPago());
            response.put("idPreferenciaMpos", pago.getIdPreferenciaMpos());
            // La URL de pago de Mercado Pago - el cliente debe ir a esta URL para pagar
            response.put("urlPago", pago.getRespuestaMercadoPago());
            
            // Retornar respuesta exitosa con código HTTP 201 (Created)
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            // Si algo sale mal, retornar error 400 (Bad Request)
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al crear la preferencia: " + e.getMessage());
            logger.error("Error al crear la preferencia de pago: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Endpoint para listar todos los pagos registrados
     * GET /api/pagos
     * Solo ADMIN y TRABAJADOR pueden ver todos los pagos
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @GetMapping
    @Operation(summary = "Listar todos los pagos")
    public ResponseEntity<List<Pago>> listarTodosLosPagos() {
        List<Pago> pagos = pagoService.obtenerTodosLosPagos();
        return ResponseEntity.ok(pagos);  // Retorna código 200 (OK)
    }

    /**
     * Endpoint para obtener un pago específico por su ID
     * GET /api/pagos/{id}
     * Ejemplo: GET /api/pagos/5
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener pago por ID")
    public ResponseEntity<?> obtenerPagoPorId(@PathVariable Long id) {
        // Si encuentra el pago, retorna 200 OK con el pago
        // Si no lo encuentra, retorna 404 Not Found
        return pagoService.obtenerPagoPorId(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Endpoint para obtener todos los pagos de un pedido específico
     * GET /api/pagos/pedido/{idPedido}
     * Ejemplo: GET /api/pagos/pedido/127
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @GetMapping("/pedido/{idPedido}")
    @Operation(summary = "Obtener pagos por pedido")
    public ResponseEntity<List<Pago>> obtenerPagosPorPedido(@PathVariable Long idPedido) {
        List<Pago> pagos = pagoService.obtenerPagosPorPedido(idPedido);
        return ResponseEntity.ok(pagos);
    }

    /**
     * Endpoint para actualizar el estado de un pago
     * PUT /api/pagos/{id}/estado?estado=2&idPagoMpos=xxx&respuestaMp=xxx
     * 
     * CAMBIO IMPORTANTE: El parámetro "estado" ahora es Long en lugar de String
     * Antes recibía: "PENDIENTE", "PAGADO", "RECHAZADO"
     * Ahora recibe: 1, 2, 3 (números que corresponden a la tabla ESTADO_PAGO)
     * - 1 = Pendiente
     * - 2 = Aprobado
     * - 3 = Rechazado
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PutMapping("/{id}/estado")
    @Operation(summary = "Actualizar estado del pago")
    public ResponseEntity<?> actualizarEstadoPago(
            @PathVariable Long id,  // ID del pago a actualizar
            @RequestParam Long estado,  // ✅ CAMBIÉ: De String a Long - ahora espera 1, 2 o 3
            @RequestParam(required = false) String idPagoMpos,  // ID del pago de Mercado Pago (opcional)
            @RequestParam(required = false) String respuestaMp) {  // Respuesta de MP (opcional)
        try {
            // Llamar al servicio para actualizar el estado
            Pago pagoActualizado = pagoService.actualizarEstadoPago(id, estado, idPagoMpos, respuestaMp);
            return ResponseEntity.ok(pagoActualizado);
        } catch (Exception e) {
            // Si hay error, retornar 400 Bad Request
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al actualizar el estado: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Endpoint para cancelar/eliminar un pago
     * DELETE /api/pagos/{id}
     * Solo ADMIN puede cancelar pagos
     * En realidad no elimina el registro, solo cambia el estado a "Rechazado" (3)
     */
    @PreAuthorize("hasAnyRole('ADMIN')")
    @DeleteMapping("/{id}")
    @Operation(summary = "Cancelar pago")
    public ResponseEntity<?> cancelarPago(@PathVariable Long id) {
        try {
            pagoService.cancelarPago(id);
            Map<String, String> response = new HashMap<>();
            response.put("mensaje", "Pago cancelado exitosamente");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al cancelar el pago: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Webhook de Mercado Pago - Endpoint PÚBLICO (sin autenticación)
     * POST /api/pagos/webhook
     * 
     * Este endpoint NO tiene @PreAuthorize
     * Mercado Pago llama a este endpoint para notificar cambios en el estado del pago
     * No tiene token JWT porque es Mercado Pago quien lo llama, no un usuario
     * 
     * Cuando un cliente paga en Mercado Pago, MP envía una notificación a este endpoint
     * para que actualicemos el estado del pago en nuestra base de datos
     */
    @PostMapping("/webhook")
    @Operation(summary = "Webhook de Mercado Pago", security = {})  // security = {} indica que es público
    public ResponseEntity<?> webhookMercadoPago(
            @RequestParam(value = "type", required = false) String tipo,  // Tipo de notificación
            @RequestParam(value = "id", required = false) Long id,  // ID de la notificación
            @RequestParam(value = "data.id", required = false) String idPago) {  // ID del pago
        try {
            // Procesar la notificación de Mercado Pago
            pagoService.procesarWebhook(tipo, id, idPago);
            return ResponseEntity.ok().build();  // Retorna 200 OK vacío
        } catch (Exception e) {
            // Si hay error, retornar 500 Internal Server Error
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }


    // Incorporando lo hecho por la ori.
    // ============================================================
    // WEBPAY
    // ============================================================
    /**
     * Iniciar pago Webpay POST /api/pagos/webpay/iniciar/{idPedido}
     */
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR', 'CLIENTE')")
    @PostMapping("/webpay/iniciar/{idPedido}")
    @Operation(summary = "Iniciar pago Webpay")
    public ResponseEntity<?> iniciarPagoWebpay(@PathVariable Long idPedido) {
        try {
            IniciarWebpayResponse response = pagoService.iniciarPagoWebpay(idPedido);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "Error al iniciar pago Webpay: " + e.getMessage());
            logger.error("Error al iniciar pago Webpay: ", e);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
        }
    }

    /**
     * Confirmar pago Webpay - SIN autenticación JWT
     *
     * GET /api/pagos/webpay/confirmar
     *
     * Casos posibles que envía Transbank: 1. Pago exitoso: token_ws=xxx 2.
     * Usuario cancela en formulario: TBK_TOKEN=xxx, TBK_ORDEN_COMPRA=xxx,
     * TBK_ID_SESION=xxx 3. Timeout (>10 min): TBK_TOKEN=xxx,
     * TBK_ORDEN_COMPRA=xxx, TBK_ID_SESION=xxx
     */
    @RequestMapping(
            value = "/webpay/confirmar",
            method = {RequestMethod.GET, RequestMethod.POST})
    @Operation(summary = "Confirmar pago Webpay (callback de Transbank)", security = {})
    public ResponseEntity<?> confirmarPagoWebpay(
            @RequestParam(value = "token_ws", required = false) String tokenWs,
            @RequestParam(value = "TBK_TOKEN", required = false) String tbkToken,
            @RequestParam(value = "TBK_ORDEN_COMPRA", required = false) String tbkOrdenCompra,
            @RequestParam(value = "TBK_ID_SESION", required = false) String tbkIdSesion) {

        logger.info("=== ENTRÓ A confirmarPagoWebpay ===");
        

        // ================================================
        // CASO 1: El usuario canceló en el formulario de Webpay
        // Transbank envía TBK_TOKEN en lugar de token_ws
        // ================================================
        if (tbkToken != null && tokenWs == null) {
            logger.warn("Usuario canceló el pago en formulario Webpay. TBK_TOKEN: {}, Orden: {}",
                    tbkToken, tbkOrdenCompra);

            try {
                // Marcar el pago como rechazado/cancelado en la BD
                pagoService.cancelarPagoWebpayPorToken(tbkToken);
            } catch (Exception e) {
                logger.error("Error al cancelar pago Webpay: {}", e.getMessage());
            }

            // Redirigir al frontend con estado cancelado
            String urlFrontend = "http://localhost:5173/#/pago/webpay/retorno"
                    + "?status=cancelled"
                    + "&orden=" + (tbkOrdenCompra != null ? tbkOrdenCompra : "");

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", urlFrontend)
                    .build();
        }

        // ================================================
        // CASO 2: No llegó ningún token (caso inesperado)
        // ================================================
        if (tokenWs == null) {
            logger.error("Confirmación Webpay sin token_ws ni TBK_TOKEN");

            String urlFrontend = "http://localhost:5173/#/pago/webpay/retorno?status=error";
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", urlFrontend)
                    .build();
        }

        // ================================================
        // CASO 3: Pago procesado (exitoso o rechazado por banco)
        // ================================================
        try {
            WebpayPlusTransactionCommitResponse response = pagoService.confirmarPagoWebpay(tokenWs);

            if (response.getResponseCode() == 0) {
                // PAGO APROBADO
                logger.info("Pago Webpay aprobado. Orden: {}", response.getBuyOrder());

                String urlFrontend = "http://localhost:5173/#/pago/webpay/retorno"
                        + "?status=success"
                        + "&orden=" + response.getBuyOrder()
                        + "&monto=" + response.getAmount();

                return ResponseEntity
                        .status(HttpStatus.FOUND)
                        .header("Location", urlFrontend)
                        .build();
            }

            // PAGO RECHAZADO POR EL BANCO
            logger.warn("Pago Webpay rechazado. Código: {}, Orden: {}",
                    response.getResponseCode(), response.getBuyOrder());

            String urlFrontend = "http://localhost:5173/#/pago/webpay/retorno"
                    + "?status=failed"
                    + "&orden=" + response.getBuyOrder();

            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", urlFrontend)
                    .build();

        } catch (Exception e) {
            logger.error("Error al confirmar pago Webpay: {}", e.getMessage(), e);

            String urlFrontend = "http://localhost:5173/#/pago/webpay/retorno?status=error";
            return ResponseEntity
                    .status(HttpStatus.FOUND)
                    .header("Location", urlFrontend)
                    .build();
        }
    }
}