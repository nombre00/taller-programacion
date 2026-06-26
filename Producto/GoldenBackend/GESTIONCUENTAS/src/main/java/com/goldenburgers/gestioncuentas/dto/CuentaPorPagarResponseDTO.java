package com.goldenburgers.gestioncuentas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CuentaPorPagarResponseDTO {

    private Long idCuenta;
    private Long idProveedor;
    private String nombreProveedor;
    private String tipoGasto;
    private String descripcion;
    private BigDecimal montoTotal;
    private BigDecimal ivaCredito;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String estado;
    private String numeroDocumento;
}