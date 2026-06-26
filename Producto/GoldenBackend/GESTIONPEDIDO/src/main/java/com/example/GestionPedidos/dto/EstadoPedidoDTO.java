package com.example.GestionPedidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EstadoPedidoDTO {
    
    @JsonProperty("idEstadoPedido")
    private Long idEstadoPedido;
    
    @JsonProperty("nombreEstado")
    private String nombreEstado;
}
