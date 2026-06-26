package com.goldenburgers.gestioncuentas.model;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Entity
@Table(name = "INGRESO_MERCADERIA")
public class IngresoMercaderia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso_merc")
    private Long idIngresoMerc;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_cuenta", nullable = false)
    private CuentaPorPagar cuenta;

    // Referencia a MATERIA_PRIMA en GESTIONCATALOGO (sin FK cross-db)
    @Column(name = "id_materia_prima_ref", nullable = false)
    private Long idMateriaPrimaRef;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    @Column(name = "unidad_medida", nullable = false, length = 20)
    private String unidadMedida;

    @Column(name = "costo_unitario", nullable = false, precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();

    // PENDIENTE | CONFIRMADO | ERROR
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";
}
