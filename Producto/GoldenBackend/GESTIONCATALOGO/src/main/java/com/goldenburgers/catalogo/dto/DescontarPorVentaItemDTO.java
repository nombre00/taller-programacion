package com.goldenburgers.catalogo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class DescontarPorVentaItemDTO {

    @NotNull
    private Long idProducto;

    @NotNull
    @Min(1)
    private Integer cantidad;
}