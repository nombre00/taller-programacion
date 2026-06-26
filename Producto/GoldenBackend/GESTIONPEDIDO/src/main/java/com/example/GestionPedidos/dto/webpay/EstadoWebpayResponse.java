package com.example.GestionPedidos.dto.webpay;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor

public class EstadoWebpayResponse {

    private String token;
    private Long idPedido;

    // Estado del pago en tu BD: "PENDIENTE", "APROBADO", "RECHAZADO"
    private String estadoPago;

    // Monto del pago
    private Double monto;

    // Mensaje informativo
    private String mensaje;

}
