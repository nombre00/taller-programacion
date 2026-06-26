package com.goldenburgers.catalogo.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
 
@Data
public class MovimientoStockResponseDTO {
    private Long idMovimiento;
    private Long idMateriaPrima;
    private String nombreMateriaPrima;
    private String tipo;
    private BigDecimal cantidad;
    private BigDecimal costoUnitario;
    private String origen;
    private Long referenciaId;
    private LocalDate fecha;
    private String nota;
    // Stock resultante después del movimiento (calculado en service)
    private BigDecimal stockResultante;
}
