package com.goldenburgers.gestioncuentas.dto;

import lombok.Data;
import java.util.List;

@Data
public class IngresarCompraRequestDTO {
    private Long idIngresoMercaderia;
    private List<IngresarCompraItemDTO> items;
}
