package com.goldenburgers.gestioncuentas.model;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Entity
@Table(name = "CIERRE_DIARIO")
public class CierreDiario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_cierre")
    private Long idCierre;

    @Column(name = "fecha_cierre", nullable = false)
    private LocalDate fechaCierre;

    // MAÑANA | TARDE | NOCHE | UNICO
    // Por ahora siempre será UNICO, pero queda listo para multi-turno
    @Column(name = "turno", nullable = false, length = 20)
    private String turno = "UNICO";

    @Column(name = "id_trabajador_ref")
    private Long idTrabajadorRef;

    @Column(name = "total_ingresos", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalIngresos = BigDecimal.ZERO;

    @Column(name = "total_egresos", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalEgresos = BigDecimal.ZERO;

    @Column(name = "total_comisiones", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalComisiones = BigDecimal.ZERO;

    @Column(name = "total_iva_debito", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalIvaDebito = BigDecimal.ZERO;

    @Column(name = "total_iva_credito", nullable = false, precision = 14, scale = 2)
    private BigDecimal totalIvaCredito = BigDecimal.ZERO;

    // resultado_neto = total_ingresos - total_egresos - total_comisiones
    @Column(name = "resultado_neto", nullable = false, precision = 14, scale = 2)
    private BigDecimal resultadoNeto = BigDecimal.ZERO;

    @Column(name = "monto_contado", precision = 12, scale = 2)
    private BigDecimal montoContado = BigDecimal.ZERO;

    @Column(name = "diferencia_caja", precision = 12, scale = 2)
    private BigDecimal diferenciaCaja = BigDecimal.ZERO;

    // BORRADOR | CERRADO
    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "BORRADOR";

    @Column(name = "generado_por", length = 100)
    private String generadoPor;

    @OneToMany(mappedBy = "cierreDiario", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DetalleCierre> detalles;
}