package com.goldenburgers.catalogo.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Data;
import java.util.List;

@Data
public class RecetaRequestDTO {

    @NotNull(message = "El producto es obligatorio")
    private Long idProducto;

    @Size(max = 255)
    private String descripcion;

    @NotEmpty(message = "La receta debe tener al menos un ingrediente")
    @Valid
    private List<RecetaDetalleRequestDTO> detalles;
}
