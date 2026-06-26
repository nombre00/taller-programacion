package com.goldenburgers.gestioncuentas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class IngresarCompraItemDTO {
    private Long idMateriaPrima;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
}
