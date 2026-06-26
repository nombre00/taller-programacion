package com.goldenburgers.catalogo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
 
@Data
public class RecetaDetalleRequestDTO {
 
    @NotNull(message = "La materia prima es obligatoria")
    private Long idMateriaPrima;
 
    @NotNull
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad;
 
    @NotBlank
    @Size(max = 20)
    private String unidadMedida;
}
