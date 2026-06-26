package com.example.GestionPedidos.dto;

import java.sql.Timestamp;
import java.util.List;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PedidoDTO {
    
    @JsonProperty("idCliente")
    private Long idCliente;
    
    @JsonProperty("idEstadoPedido")
    private Long idEstadoPedido;
    
    @JsonProperty("idMetodoPago")
    private Long idMetodoPago;
    
    @JsonProperty("idTipoEntrega")
    private Long idTipoEntrega;
    
    @JsonProperty("idDireccionEntrega")
    private Long idDireccionEntrega;
    
    @JsonProperty("montoSubtotal")
    private Double montoSubtotal;
    
    @JsonProperty("montoEnvio")
    private Double montoEnvio;
    
    @JsonProperty("montoTotal")
    private Double montoTotal;
    
    @JsonProperty("fechaHoraPedido")
    private Timestamp fechaHoraPedido;
    
    @JsonProperty("notasCliente")
    private String notasCliente;
    
    @JsonProperty("detalles")
    private List<DetallePedidoDTO> detalles;
}
