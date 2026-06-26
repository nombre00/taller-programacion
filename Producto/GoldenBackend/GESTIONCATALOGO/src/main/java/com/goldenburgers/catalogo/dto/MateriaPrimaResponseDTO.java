package com.goldenburgers.catalogo.dto;

 
import lombok.Data;
import java.math.BigDecimal;
 
@Data
public class MateriaPrimaResponseDTO {
    private Long idMateriaPrima;
    private String nombre;
    private String unidadMedida;
    private BigDecimal stockActual;
    private BigDecimal stockMinimo;
    private BigDecimal costoUnitarioPromedio;
    private Boolean activo;
    // Indica si el stock está bajo el mínimo (calculado en el service)
    private Boolean stockBajo;
}
