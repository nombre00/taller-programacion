package com.example.GestionPedidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetallePedidoDTO {
    
    @JsonProperty("idProducto")
    private Long idProducto;
    
    @JsonProperty("cantidad")
    private Integer cantidad;
    
    @JsonProperty("precioUnitario")
    private Double precioUnitario;
    
    @JsonProperty("subtotalLinea")
    private Double subtotalLinea;
}
