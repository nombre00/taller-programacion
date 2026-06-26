package com.example.GestionPedidos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CrearPreferenciaRequest {
    private Long idPedido;
    private BigDecimal montoPago;
    private String descripcion;
    private String emailPagador;
    private String nombrePagador;
}