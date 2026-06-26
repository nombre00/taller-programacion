package com.goldenburgers.gestioncuentas.model;


import jakarta.persistence.*;
import lombok.Data;


@Data
@Entity
@Table(name = "COMISION_PLATAFORMA")
public class ComisionPlataforma {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_comision")
    private Long idComision;

    @Column(name = "metodo_pago_ref", nullable = false, unique = true, length = 50)
    private String metodoPagoRef;

    @Column(name = "nombre_plataforma", nullable = false, length = 100)
    private String nombrePlataforma;

    // Ej: 0.1500 = 15%
    @Column(name = "porcentaje", nullable = false, precision = 5, scale = 4)
    private java.math.BigDecimal porcentaje;

    @Column(name = "activo", nullable = false)
    private Boolean activo = true;
}