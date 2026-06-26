package com.goldenburgers.gestioncuentas.model;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Entity
@Table(name = "CUENTA_POR_PAGAR")
public class CuentaPorPagar {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cuenta")
    private Long idCuenta;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_proveedor", nullable = false)
    private Proveedor proveedor;

    // MERCADERIA | SERVICIO | ARRIENDO | MARKETING | OTRO
    @Column(name = "tipo_gasto", nullable = false, length = 20)
    private String tipoGasto;

    @Column(name = "descripcion", length = 255)
    private String descripcion;

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(name = "iva_credito", nullable = false, precision = 12, scale = 2)
    private BigDecimal ivaCredito = BigDecimal.ZERO;

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision;

    @Column(name = "fecha_vencimiento", nullable = false)
    private LocalDate fechaVencimiento;

    // PENDIENTE | PAGADO | VENCIDO | ANULADO
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";

    @Column(name = "numero_documento", length = 50)
    private String numeroDocumento;
}
