package com.goldenburgers.gestioncuentas.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class PagoProveedorRequestDTO {

    @NotNull(message = "El id de la cuenta es obligatorio")
    private Long idCuenta;

    @NotNull(message = "El monto pagado es obligatorio")
    @DecimalMin(value = "0.01", message = "El monto pagado debe ser mayor a cero")
    private BigDecimal montoPagado;

    @NotBlank(message = "El método de pago es obligatorio")
    private String metodoPago;

    private String comprobanteRef;
}