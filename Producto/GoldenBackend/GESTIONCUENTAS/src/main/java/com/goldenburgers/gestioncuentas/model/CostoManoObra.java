package com.goldenburgers.gestioncuentas.model;


import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;


@Data
@Entity
@Table(name = "COSTO_MANO_OBRA")
public class CostoManoObra {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_costo_mo")
    private Long idCostoMo;

    // Referencia a ASIGNACION_TURNO en GESTIONTURNOS (sin FK cross-db)
    @Column(name = "id_asignacion_ref", nullable = false)
    private Long idAsignacionRef;

    // Referencia a POSICION en GESTIONTURNOS (sin FK cross-db)
    @Column(name = "id_posicion_ref", nullable = false)
    private Long idPosicionRef;

    @Column(name = "periodo_inicio", nullable = false)
    private LocalDate periodoInicio;

    @Column(name = "periodo_fin", nullable = false)
    private LocalDate periodoFin;

    @Column(name = "horas_trabajadas", nullable = false, precision = 8, scale = 2)
    private BigDecimal horasTrabajadas;

    @Column(name = "tarifa_hora", nullable = false, precision = 10, scale = 2)
    private BigDecimal tarifaHora;

    // total = horas_trabajadas * tarifa_hora
    @Column(name = "total", nullable = false, precision = 12, scale = 2)
    private BigDecimal total;
}
