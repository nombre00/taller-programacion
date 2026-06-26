package com.goldenburgers.gestioncuentas.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PagoProveedorResponseDTO {

    private Long idPagoProv;
    private Long idCuenta;
    private String descripcionCuenta;
    private String nombreProveedor;
    private LocalDate fechaPago;
    private BigDecimal montoPagado;
    private String metodoPago;
    private String comprobanteRef;
}