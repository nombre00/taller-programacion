package com.example.GestionPedidos.dto.webpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class ConfirmarWebpayResponse {

    // true = pago aprobado, false = rechazado
    private boolean aprobado;

    // 0 = aprobado, cualquier otro valor = rechazado
    private int responseCode;

    // Código de autorización del banco
    private String authorizationCode;

    // Monto que se cobró
    private double amount;

    // Tipo de pago: VD=débito, VN=crédito sin cuotas, VC=crédito con cuotas
    private String paymentTypeCode;

    // Número de cuotas (0 si es débito o crédito sin cuotas)
    private int sharesNumber;

    // Token de la transacción
    private String token;

    // ID del pedido asociado
    private Long idPedido;

    // Mensaje para mostrar al usuario
    private String mensaje;

}
