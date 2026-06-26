package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "POSICION")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Posicion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_POSICION")
    private Long idPosicion;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "SUELDO", nullable = false, precision = 10, scale = 2)
    private BigDecimal sueldo;

    @Column(name = "COLOR", length = 7)
    private String color; // formato #RRGGBB

    @OneToMany(mappedBy = "posicion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SlotTurno> slots = new ArrayList<>();
}
