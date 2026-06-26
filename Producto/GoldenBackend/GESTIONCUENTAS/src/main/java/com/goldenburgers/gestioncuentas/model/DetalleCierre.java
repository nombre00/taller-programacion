package com.goldenburgers.gestioncuentas.model;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;


@Data
@Entity
@Table(name = "DETALLE_CIERRE")
public class DetalleCierre {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_detalle")
    private Long idDetalle;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cierre", nullable = false)
    private CierreDiario cierreDiario;

    // INGRESO | EGRESO | COMISION | IVA | MANO_OBRA
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "concepto", nullable = false, length = 255)
    private String concepto;

    @Column(name = "monto", nullable = false, precision = 12, scale = 2)
    private BigDecimal monto;

    // Ej: "REGISTRO_INGRESO:45" o "CUENTA_POR_PAGAR:12"
    @Column(name = "referencia", length = 100)
    private String referencia;
}
