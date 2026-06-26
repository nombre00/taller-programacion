package com.goldenburgers.gestioncuentas.dto;

import lombok.Data;
import java.math.BigDecimal;
// import java.time.LocalDate;

@Data
public class RegistrarCompraResponseDTO {
    private Long idCuenta;
    private String numeroDocumento;
    private BigDecimal montoTotal;
    private String estadoCuenta;
    private String estadoStock;   // CONFIRMADO | ERROR
    private String mensaje;
}
