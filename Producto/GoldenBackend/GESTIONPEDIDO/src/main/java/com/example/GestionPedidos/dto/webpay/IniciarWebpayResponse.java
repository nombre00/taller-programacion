package com.example.GestionPedidos.dto.webpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class IniciarWebpayResponse {

    // Token que genera Webpay para identificar la transacción
    private String token;

    // URL de Webpay a donde redirigir al usuario con formulario POST
    private String urlPago;

    // ID del pedido que se está pagando
    private Long idPedido;

    // Monto total del pedido
    private Double monto;

    // Mensaje informativo
    private String mensaje;

}
