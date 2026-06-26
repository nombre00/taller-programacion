package com.goldenburgers.catalogo.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "MOVIMIENTO_STOCK")
public class MovimientoStock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_movimiento")
    private Long idMovimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "id_materia_prima", nullable = false)
    private MateriaPrima materiaPrima;

    // ENTRADA | SALIDA | MERMA | AJUSTE
    @Column(name = "tipo", nullable = false, length = 20)
    private String tipo;

    @Column(name = "cantidad", nullable = false, precision = 10, scale = 3)
    private BigDecimal cantidad;

    // Solo aplica en ENTRADA (para actualizar costo promedio)
    @Column(name = "costo_unitario", precision = 10, scale = 2)
    private BigDecimal costoUnitario;

    // VENTA | INGRESO_MERCADERIA | AJUSTE_MANUAL
    @Column(name = "origen", nullable = false, length = 30)
    private String origen;

    // ID del documento origen (id_venta, id_ingreso_merc, etc.)
    @Column(name = "referencia_id")
    private Long referenciaId;

    @Column(name = "fecha", nullable = false)
    private LocalDate fecha = LocalDate.now();

    @Column(name = "nota", length = 255)
    private String nota;
}
