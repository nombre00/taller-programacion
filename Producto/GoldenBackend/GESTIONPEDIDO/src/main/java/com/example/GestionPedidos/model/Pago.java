package com.example.GestionPedidos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.sql.Timestamp;

/**
 * Entidad que representa un PAGO en la BD
 * Registra cada pago realizado a través de Mercado Pago, efectivo o transferencia
 */
@Entity
@Table(name = "Pago")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pago {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pago")
    private Long idPago;
    
    // ID del pedido que se está pagando
    // FK a la tabla PEDIDO
    @Column(name = "id_pedido", nullable = false)
    private Long idPedido;
    
    // Monto que se pagó
    @Column(name = "monto_pago", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoPago;

    // Estado del pago: PENDIENTE, PAGADO, CANCELADO
    @Column(name = "estado_pago", nullable = false)
    private Long estadoPago;
    
    // Método de pago: "webpay" "mercado_pago" o "efectivo" o "transferencia"
    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago; // "mercado_pago", "efectivo", "webpay"
    
    // ID de preferencia que genera Mercado Pago
    @Column(name = "id_preferencia_mpos", length = 500)
    private String idPreferenciaMpos;
    
    // ID del pago procesado por Mercado Pago
    @Column(name = "id_pago_mpos", length = 500)
    private String idPagoMpos;
    
    // JSON completo de la respuesta de Mercado Pago
    // Se guarda para auditoría y trazabilidad
    @Column(name = "respuesta_mercado_pago", columnDefinition = "CLOB")
    private String respuestaMercadoPago;
    
    // Fecha cuando se confirmó el pago
    // Solo tiene valor cuando Mercado Pago aprueba el pago
    @Column(name = "fecha_pago")
    private Timestamp fechaPago;
    
    // Fecha de creación del registro (automática con DEFAULT SYSDATE)
    @Column(name = "fecha_creacion", insertable = false, updatable = false)
    private Timestamp fechaCreacion;



    /**
     * ------------------------------------------------------------------------
     */
    // ==========================================
    // CAMPOS DE WEBPAY
    // ==========================================
    @Column(name = "token_webpay", length = 500)
    private String tokenWebpay;

    @Column(name = "fecha_pago_webpay")
    private Timestamp fechaPagoWebpay;

    // ==========================================
    // Agregar en DB
    // ALTER TABLE Pago ADD token_webpay VARCHAR2(500);
    //ALTER TABLE Pago ADD fecha_pago_webpay TIMESTAMP;
    //COMMIT;
    // ==========================================
}