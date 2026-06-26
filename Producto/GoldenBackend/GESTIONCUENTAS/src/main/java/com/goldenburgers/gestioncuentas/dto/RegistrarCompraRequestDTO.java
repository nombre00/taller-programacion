package com.goldenburgers.gestioncuentas.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
public class RegistrarCompraRequestDTO {

    @NotNull
    private Long idProveedor;

    @NotBlank
    @Size(max = 50)
    private String numeroDocumento;

    @NotNull
    private LocalDate fechaEmision;

    @NotNull
    private LocalDate fechaVencimiento;

    // IVA crédito fiscal de la factura
    @NotNull
    @DecimalMin("0.0")
    private BigDecimal ivaCredito;

    @NotEmpty
    private List<ItemCompraDTO> items;
}