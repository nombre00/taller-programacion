package com.goldenburgers.catalogo.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RecetaDetalleResponseDTO {
    private Long idDetalle;
    private Long idMateriaPrima;
    private String nombreMateriaPrima;
    private BigDecimal cantidad;
    private String unidadMedida;
    private BigDecimal costoUnitarioPromedio;
    private BigDecimal costoLinea; // cantidad * costoUnitarioPromedio
}