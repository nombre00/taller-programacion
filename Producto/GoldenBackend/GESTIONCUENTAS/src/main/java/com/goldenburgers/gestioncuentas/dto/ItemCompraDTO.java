package com.goldenburgers.gestioncuentas.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class ItemCompraDTO {

    @NotNull
    private Long idMateriaPrima;

    @NotBlank
    @Size(max = 20)
    private String unidadMedida;

    @NotNull
    @DecimalMin("0.001")
    private BigDecimal cantidad;

    @NotNull
    @DecimalMin("0.01")
    private BigDecimal costoUnitario;
}