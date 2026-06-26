package com.goldenburgers.gestioncuentas.model;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Entity
@Table(name = "REGISTRO_INGRESO")
public class RegistroIngreso {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_ingreso")
    private Long idIngreso;

    // Referencia a la venta en GESTIONVENTA (sin FK cross-db)
    @Column(name = "id_venta_ref", nullable = false)
    private Long idVentaRef;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_comision")
    private ComisionPlataforma comision;

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();

    @Column(name = "monto_bruto", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoBruto;

    // Descuento aplicado (promociones). Reduce el ingreso real, no es egreso.
    @Column(name = "monto_descuento", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoDescuento = BigDecimal.ZERO;

    @Column(name = "monto_comision", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoComision = BigDecimal.ZERO;

    // monto_neto = monto_bruto - monto_descuento - monto_comision
    @Column(name = "monto_neto", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoNeto;

    @Column(name = "canal", nullable = false, length = 50)
    private String canal;

    @Column(name = "iva_debito", nullable = false, precision = 12, scale = 2)
    private BigDecimal ivaDebito = BigDecimal.ZERO;

    // VENTA | DEVOLUCION | PROPINA
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo = "VENTA";

    // PENDIENTE | CONCILIADO
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "PENDIENTE";
}
