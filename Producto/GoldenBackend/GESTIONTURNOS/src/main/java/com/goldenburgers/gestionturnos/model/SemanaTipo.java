package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "SEMANATIPO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SemanaTipo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SEMANA")
    private Long idSemana;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @Column(name = "ACTIVO", nullable = false)
    private Boolean activo = true;

    @OneToMany(mappedBy = "semana", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AsignacionTurno> asignaciones = new ArrayList<>();

    @OneToMany(mappedBy = "semana", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<CalendarioSemana> calendarios = new ArrayList<>();
}
