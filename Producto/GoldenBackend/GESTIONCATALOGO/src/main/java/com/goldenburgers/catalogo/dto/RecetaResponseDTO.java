package com.goldenburgers.catalogo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;
 
@Data
public class RecetaResponseDTO {
    private Long idReceta;
    private Long idProducto;
    private String nombreProducto;
    private String descripcion;
    private Boolean activo;
    private List<RecetaDetalleResponseDTO> detalles;
    // Costo total de la receta = suma de costoLinea de cada detalle
    private BigDecimal costoTotal;
}
