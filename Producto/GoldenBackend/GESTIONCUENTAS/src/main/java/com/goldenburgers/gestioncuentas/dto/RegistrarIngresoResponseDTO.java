package com.goldenburgers.gestioncuentas.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class RegistrarIngresoResponseDTO {
    private Long idIngreso;
    private BigDecimal montoNeto;
    private String estado;
    private String mensaje;
}