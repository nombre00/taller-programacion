package com.example.GestionPedidos.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoEntregaDTO {
    
    @JsonProperty("idTipoEntrega")
    private Long idTipoEntrega;
    
    @JsonProperty("nombreEntrega")
    private String nombreEntrega;
}
