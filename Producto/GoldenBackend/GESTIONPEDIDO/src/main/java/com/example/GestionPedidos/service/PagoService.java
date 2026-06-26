package com.example.GestionPedidos.service; 

// Módulos del servicio importados
import com.example.GestionPedidos.dto.CrearPreferenciaRequest; 
import com.example.GestionPedidos.dto.webpay.IniciarWebpayResponse;
import com.example.GestionPedidos.model.Pago;
import com.example.GestionPedidos.model.Pedido;
import com.example.GestionPedidos.repository.PagoRepository;
import com.example.GestionPedidos.repository.PedidoRepository;
import com.fasterxml.jackson.databind.ObjectMapper;

// Importaciones mercado pago
import com.mercadopago.MercadoPagoConfig;
import com.mercadopago.client.preference.PreferenceBackUrlsRequest;
import com.mercadopago.client.preference.PreferenceClient;
import com.mercadopago.client.preference.PreferenceItemRequest;
import com.mercadopago.client.preference.PreferenceRequest;
import com.mercadopago.exceptions.MPApiException;
import com.mercadopago.resources.preference.Preference;

// Importaciones webpay
import cl.transbank.common.IntegrationType;
import cl.transbank.webpay.common.WebpayOptions;
import cl.transbank.webpay.webpayplus.WebpayPlus;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCommitResponse;
import cl.transbank.webpay.webpayplus.responses.WebpayPlusTransactionCreateResponse;

// Logs
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

// Framework
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// Utilidades.
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class PagoService {

    Logger logger = LoggerFactory.getLogger(PagoService.class);

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private PedidoRepository pedidoRepository;
    
    @Autowired
    private ObjectMapper objectMapper;


    // ============================================================
    // MERCADO PAGO
    // ============================================================
    @Value("${mercadopago.access.token}")
    private String mercadoPagoAccessToken;


    // ============================================================
    // WEBPAY
    // ============================================================
    @Value("${webpay.commerce.code}")
    private String commerceCode;

    @Value("${webpay.api.key}")
    private String apiKey;

    @Value("${webpay.environment}")
    private String environment;

    @Value("${webpay.return.url}")
    private String webpayReturnUrl;

    @Value("${webpay.final.url}")
    private String webpayFinalUrl;


    // ============================================================
    // IDs de estados en tabla EstadoPedido
    // 1 = Pendiente de Pago
    // 2 = Recibido/Pagado
    // 3 = En preparación
    // 4 = En camino
    // 5 = Entregado
    // 6 = Cancelado
    // ============================================================
    private static final Long ESTADO_PEDIDO_PAGADO = 2L;
    private static final Long ESTADO_PEDIDO_CANCELADO = 6L;

    // IDs de estados en tabla EstadoPago
    // 1 = Pendiente | 2 = Aprobado | 3 = Rechazado
    private static final Long ESTADO_PAGO_PENDIENTE = 1L;
    private static final Long ESTADO_PAGO_APROBADO = 2L;
    private static final Long ESTADO_PAGO_RECHAZADO = 3L;




    /**
     * Crear una preferencia de pago en Mercado Pago
     */
    @Transactional
    public Pago crearPreferenciaPago(CrearPreferenciaRequest request) {
        try {
            System.out.println("=== Iniciando creación de preferencia ===");
            System.out.println("Token MP configurado: " + mercadoPagoAccessToken.substring(0, 20) + "...");
            System.out.println("Pedido ID: " + request.getIdPedido());
            System.out.println("Monto: " + request.getMontoPago());
            System.out.println("Descripción: " + request.getDescripcion());
            
            // Configurar el token de Mercado Pago
            MercadoPagoConfig.setAccessToken(mercadoPagoAccessToken);

            // Crear el item de la preferencia
            PreferenceItemRequest itemRequest = PreferenceItemRequest.builder()
                    .id(String.valueOf(request.getIdPedido()))
                    .title(request.getDescripcion() != null ? request.getDescripcion() : "Pedido #" + request.getIdPedido())
                    .description(request.getDescripcion())
                    .quantity(1)
                    .currencyId("CLP")
                    .unitPrice(request.getMontoPago())
                    .build();

            System.out.println("Item de preferencia creado correctamente");
            
            List<PreferenceItemRequest> items = new ArrayList<>();
            items.add(itemRequest);

            // Configurar URLs de retorno
            PreferenceBackUrlsRequest backUrls = PreferenceBackUrlsRequest.builder()
                    .success("http://localhost:4200/pago/exito")
                    .failure("http://localhost:4200/pago/fallo")
                    .pending("http://localhost:4200/pago/pendiente")
                    .build();

            System.out.println("URLs de retorno configuradas");
            
            // Crear la preferencia
            PreferenceRequest preferenceRequest = PreferenceRequest.builder()
                    .items(items)
                    .backUrls(backUrls)
                    // No usamos autoReturn porque no acepta localhost
                    .externalReference(String.valueOf(request.getIdPedido()))
                    .build();

            System.out.println("Enviando request a API de Mercado Pago...");
            
            // Crear el cliente y la preferencia
            PreferenceClient client = new PreferenceClient();
            Preference preference = client.create(preferenceRequest);

            System.out.println("✓ Preferencia creada exitosamente!");
            System.out.println("ID Preferencia: " + preference.getId());
            System.out.println("Init Point: " + preference.getInitPoint());
            
            // Convertir la respuesta a JSON para guardarla
            String respuestaJson = objectMapper.writeValueAsString(preference);

            // Crear el pago en la base de datos
            Pago pago = new Pago();
            pago.setIdPedido(request.getIdPedido());
            pago.setMontoPago(request.getMontoPago());
            pago.setEstadoPago(1L); // 1 = Pendiente (según tabla ESTADO_PAGO)
            pago.setMetodoPago("mercado_pago");
            pago.setIdPreferenciaMpos(preference.getId());
            pago.setRespuestaMercadoPago(respuestaJson);

            // Guardar en la base de datos
            Pago pagoGuardado = pagoRepository.save(pago);

            System.out.println("Pago guardado en BD con ID: " + pagoGuardado.getIdPago());
            System.out.println("=== Proceso completado exitosamente ===");
             
            // Agregar la URL de pago para la respuesta al frontend
            pagoGuardado.setRespuestaMercadoPago(preference.getInitPoint());

            return pagoGuardado;

        } catch (MPApiException e) {

            logger.error("Error de API de Mercado Pago: ", e);

            System.err.println("=== ERROR DE API DE MERCADO PAGO ===");
            System.err.println("Status Code: " + e.getStatusCode());
            System.err.println("Mensaje: " + e.getMessage());
            
            if (e.getApiResponse() != null) {
                System.err.println("Contenido del error: " + e.getApiResponse().getContent());
            }
            
            System.err.println("Stack trace:");
            e.printStackTrace();
            System.err.println("=======================================");
            
            String errorDetail = e.getApiResponse() != null ? 
                e.getApiResponse().getContent() : e.getMessage();
            throw new RuntimeException("Error de Mercado Pago: " + errorDetail, e);
            
        } catch (Exception e) {
            System.err.println("=== ERROR al crear preferencia de pago ===");
            System.err.println("Tipo de excepción: " + e.getClass().getName());
            System.err.println("Mensaje de error: " + e.getMessage());
            System.err.println("Stack trace completo:");
            e.printStackTrace();
            System.err.println("===========================================");
            
            throw new RuntimeException("Error al crear preferencia de pago: " + e.getMessage(), e);
        }
    }

    /**
     * Obtener todos los pagos
     */
    public List<Pago> obtenerTodosLosPagos() {
        return pagoRepository.findAll();
    }

    /**
     * Obtener pago por ID
     */
    public Optional<Pago> obtenerPagoPorId(Long id) {
        return pagoRepository.findById(id);
    }

    /**
     * Obtener pagos por ID de pedido
     */
    public List<Pago> obtenerPagosPorPedido(Long idPedido) {
        return pagoRepository.findByIdPedido(idPedido);
    }

    // Nuevo
    public Optional<Pago> obtenerPagoPorToken(String token) {
        return pagoRepository.findByTokenWebpay(token);
    }

    /**
     * Obtener pago por ID de preferencia de Mercado Pago
     */
    public Optional<Pago> obtenerPagoPorPreferencia(String idPreferencia) {
        return pagoRepository.findByIdPreferenciaMpos(idPreferencia);
    }

    /**
     * Actualizar estado del pago
     * CAMBIÉ: nuevoEstado ahora es Long en lugar de String
     */
    @Transactional
    public Pago actualizarEstadoPago(Long idPago, Long nuevoEstado, String idPagoMpos, String respuestaMp) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + idPago));
        
        pago.setEstadoPago(nuevoEstado);
        
        if (idPagoMpos != null && !idPagoMpos.isEmpty()) {
            pago.setIdPagoMpos(idPagoMpos);
        }
        
        if (respuestaMp != null && !respuestaMp.isEmpty()) {
            pago.setRespuestaMercadoPago(respuestaMp);
        }
        
        // Si el pago fue aprobado (2), registrar la fecha
        // CAMBIÉ: Ahora compara con 2L en lugar de String
        if (nuevoEstado.equals(2L) && pago.getFechaPago() == null) {
            pago.setFechaPago(new Timestamp(System.currentTimeMillis()));
        }
        
        return pagoRepository.save(pago);
    }

    /**
     * Eliminar/Cancelar pago
     * CAMBIÉ: Ahora usa 3L (Rechazado) en lugar de String
     */
    @Transactional
    public void cancelarPago(Long idPago) {
        Pago pago = pagoRepository.findById(idPago)
                .orElseThrow(() -> new RuntimeException("Pago no encontrado con ID: " + idPago));
        
        // Cambiar estado a rechazado (3) en lugar de eliminar
        pago.setEstadoPago(3L); // CAMBIÉ: 3 = Rechazado
        pagoRepository.save(pago);
    }

    /**
     * Webhook de Mercado Pago (para recibir notificaciones)
     */
    @Transactional
    public void procesarWebhook(String tipo, Long id, String idPago) {
        System.out.println("Webhook recibido - Tipo: " + tipo + ", ID: " + id + ", ID Pago: " + idPago);
        
        if ("payment".equals(tipo) && idPago != null) {
            Optional<Pago> pagoOpt = pagoRepository.findByIdPagoMpos(idPago);
            if (pagoOpt.isPresent()) {
                System.out.println("Procesando pago: " + idPago);
            }
        }
    }



    // Gestion webpay
    // ============================================================
    // CONFIGURACIÓN WEBPAY
    // ============================================================
    private WebpayPlus.Transaction getTransaction() {
        IntegrationType integrationType = "production".equalsIgnoreCase(environment)
                ? IntegrationType.LIVE
                : IntegrationType.TEST;

        WebpayOptions options = new WebpayOptions(commerceCode, apiKey, integrationType);
        return new WebpayPlus.Transaction(options);
    }


    // ============================================================
    // WEBPAY - INICIAR
    // ============================================================
    @Transactional
    public IniciarWebpayResponse iniciarPagoWebpay(Long idPedido) {
        try {
            logger.info("=== Iniciando pago Webpay para pedido: {} ===", idPedido);

            Pedido pedido = pedidoRepository.findById(idPedido)
                    .orElseThrow(() -> new RuntimeException("Pedido no encontrado: " + idPedido));

            // Evitar crear dos pagos Webpay pendientes para el mismo pedido
            boolean yaExiste = pagoRepository.findByIdPedido(idPedido)
                    .stream()
                    .anyMatch(p -> "webpay".equals(p.getMetodoPago())
                    && ESTADO_PAGO_PENDIENTE.equals(p.getEstadoPago()));

            if (yaExiste) {
                throw new RuntimeException("Ya existe un pago Webpay pendiente para este pedido");
            }

            String ordenCompra = "PEDIDO-" + idPedido + "-" + System.currentTimeMillis();
            String sessionId = "SESSION-" + idPedido;
            double monto = pedido.getMontoTotal();

            logger.info("Commerce Code: {}", commerceCode);
            logger.info("Environment: {}", environment);
            logger.info("Return URL: {}", webpayReturnUrl);
            logger.info("Monto: {}", monto);

            WebpayPlus.Transaction transaction = getTransaction();
            WebpayPlusTransactionCreateResponse tbkResponse
                    = transaction.create(ordenCompra, sessionId, monto, webpayReturnUrl);

            // Guardar pago en BD con estado PENDIENTE
            Pago pago = new Pago();
            pago.setIdPedido(idPedido);
            pago.setMontoPago(BigDecimal.valueOf(monto));
            pago.setEstadoPago(ESTADO_PAGO_PENDIENTE);
            pago.setMetodoPago("webpay");
            pago.setTokenWebpay(tbkResponse.getToken());
            pagoRepository.save(pago);

            logger.info("Transacción Webpay creada. Orden: {}", ordenCompra);

            return IniciarWebpayResponse.builder()
                    .token(tbkResponse.getToken())
                    .urlPago(tbkResponse.getUrl())
                    .idPedido(idPedido)
                    .monto(monto)
                    .mensaje("Transacción creada correctamente. Redirigir al usuario a urlPago con el token.")
                    .build();

        } catch (Exception e) {
            logger.error("Error completo", e);
            throw new RuntimeException("Error al iniciar pago Webpay: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // WEBPAY - CONFIRMAR
    // ============================================================
    /**
     * Confirma el pago con Webpay usando el token_ws. CORRECCIÓN: Ahora también
     * actualiza el estado del Pedido (no solo el Pago).
     */
    @Transactional
    public WebpayPlusTransactionCommitResponse confirmarPagoWebpay(String token) {
        try {
            logger.info("=== Confirmando pago Webpay. Token: {} ===", token);

            WebpayPlus.Transaction transaction = getTransaction();
            WebpayPlusTransactionCommitResponse response = transaction.commit(token);

            // Buscar el pago por el token almacenado al iniciar
            Pago pago = pagoRepository.findByTokenWebpay(token)
                    .orElseThrow(() -> new RuntimeException(
                    "Pago no encontrado para token: " + token));

            if (response.getResponseCode() == 0) {
                // =============================================
                // PAGO APROBADO
                // =============================================
                logger.info("Pago Webpay APROBADO. Orden: {}, Monto: {}",
                        response.getBuyOrder(), response.getAmount());

                // 1. Actualizar estado del Pago → Aprobado
                pago.setEstadoPago(ESTADO_PAGO_APROBADO);
                pago.setFechaPago(new Timestamp(System.currentTimeMillis()));
                pago.setFechaPagoWebpay(new Timestamp(System.currentTimeMillis()));
                pagoRepository.save(pago);

                // 2. CORRECCIÓN CRÍTICA: También actualizar el estado del Pedido → Pagado/Recibido
                //    Antes solo se actualizaba el Pago y el Pedido quedaba en "Pendiente de Pago"
                pedidoRepository.findById(pago.getIdPedido()).ifPresent(pedido -> {
                    pedido.setIdEstadoPedido(ESTADO_PEDIDO_PAGADO);
                    pedidoRepository.save(pedido);
                    logger.info("Estado del Pedido {} actualizado a PAGADO", pago.getIdPedido());
                });

            } else {
                // =============================================
                // PAGO RECHAZADO POR EL BANCO
                // =============================================
                logger.warn("Pago Webpay RECHAZADO. Código: {}, Orden: {}",
                        response.getResponseCode(), response.getBuyOrder());

                pago.setEstadoPago(ESTADO_PAGO_RECHAZADO);
                pagoRepository.save(pago);

                // El pedido vuelve a estado Cancelado
                pedidoRepository.findById(pago.getIdPedido()).ifPresent(pedido -> {
                    pedido.setIdEstadoPedido(ESTADO_PEDIDO_CANCELADO);
                    pedidoRepository.save(pedido);
                    logger.info("Estado del Pedido {} actualizado a CANCELADO", pago.getIdPedido());
                });
            }

            return response;

        } catch (Exception e) {
            logger.error("Error al confirmar pago Webpay: {}", e.getMessage(), e);
            throw new RuntimeException("Error al confirmar pago Webpay: " + e.getMessage(), e);
        }
    }

    // ============================================================
    // WEBPAY - CANCELACIÓN POR EL USUARIO EN EL FORMULARIO
    // ============================================================
    /**
     * NUEVO MÉTODO: Se llama cuando el usuario cancela en el formulario de
     * Webpay. Transbank envía TBK_TOKEN en lugar de token_ws. Marca el pago y
     * el pedido como cancelados.
     */
    @Transactional
    public void cancelarPagoWebpayPorToken(String tbkToken) {
        logger.info("=== Cancelando pago Webpay por TBK_TOKEN: {} ===", tbkToken);

        pagoRepository.findByTokenWebpay(tbkToken).ifPresentOrElse(
                pago -> {
                    // Marcar pago como rechazado
                    pago.setEstadoPago(ESTADO_PAGO_RECHAZADO);
                    pagoRepository.save(pago);

                    // Marcar pedido como cancelado
                    pedidoRepository.findById(pago.getIdPedido()).ifPresent(pedido -> {
                        pedido.setIdEstadoPedido(ESTADO_PEDIDO_CANCELADO);
                        pedidoRepository.save(pedido);
                        logger.info("Pedido {} cancelado por abandono en formulario Webpay",
                                pago.getIdPedido());
                    });
                },
                () -> logger.warn("No se encontró pago para TBK_TOKEN: {}", tbkToken)
        );
    }
}