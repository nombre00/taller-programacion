package com.goldenburgers.catalogo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MateriaPrimaRequestDTO {

    @NotBlank
    @Size(max = 100)
    private String nombre;

    @NotBlank
    @Size(max = 20)
    private String unidadMedida;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal stockMinimo;

    @DecimalMin("0.0")
    private BigDecimal stockInicial; // opcional, default 0 en el service
}