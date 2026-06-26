package com.goldenburgers.gestioncuentas.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RegistrarIngresoRequestDTO {

    @NotNull
    private Long idVentaRef;

    @NotNull
    private BigDecimal montoBruto;

    @NotNull
    private BigDecimal ivaDebito;

    @NotBlank
    private String canal;
}