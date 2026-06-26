package com.goldenburgers.catalogo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class IngresarPorCompraRequestDTO {

    @NotNull
    private Long idIngresoMercaderia;

    @NotEmpty
    private List<IngresarPorCompraItemDTO> items;
}