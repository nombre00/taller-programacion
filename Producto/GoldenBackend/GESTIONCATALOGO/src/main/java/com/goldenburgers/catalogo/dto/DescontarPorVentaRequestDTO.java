package com.goldenburgers.catalogo.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class DescontarPorVentaRequestDTO {

    @NotNull
    private Long idVenta;

    @NotEmpty
    private List<DescontarPorVentaItemDTO> items;
}