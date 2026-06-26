package com.goldenburgers.catalogo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class IngresarPorCompraItemDTO {

    @NotNull
    private Long idMateriaPrima;

    @NotNull
    @DecimalMin(value = "0.001")
    private BigDecimal cantidad;

    @NotNull
    @DecimalMin(value = "0.01")
    private BigDecimal costoUnitario;
}
