package com.example.GestionPedidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MetodoPagoDTO {
    
    @JsonProperty("idMetodoPago")
    private Long idMetodoPago;
    
    @JsonProperty("nombreMetodo")
    private String nombreMetodo;
}
