package com.goldenburgers.catalogo.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
 
@Data
public class MovimientoStockRequestDTO {
 
    @NotNull(message = "La materia prima es obligatoria")
    private Long idMateriaPrima;
 
    // ENTRADA | SALIDA | MERMA | AJUSTE
    @NotBlank
    @Pattern(regexp = "ENTRADA|SALIDA|MERMA|AJUSTE",
             message = "Tipo debe ser ENTRADA, SALIDA, MERMA o AJUSTE")
    private String tipo;
 
    @NotNull
    @DecimalMin(value = "0.001", message = "La cantidad debe ser mayor a 0")
    private BigDecimal cantidad;
 
    // Solo obligatorio si tipo = ENTRADA
    private BigDecimal costoUnitario;
 
    // VENTA | INGRESO_MERCADERIA | AJUSTE_MANUAL
    @NotBlank
    @Pattern(regexp = "VENTA|INGRESO_MERCADERIA|AJUSTE_MANUAL",
             message = "Origen inválido")
    private String origen;
 
    private Long referenciaId;
 
    @Size(max = 255)
    private String nota;
}
