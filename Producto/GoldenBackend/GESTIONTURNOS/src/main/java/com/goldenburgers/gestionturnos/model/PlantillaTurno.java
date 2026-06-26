package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "PLANTILLATURNO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlantillaTurno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_PLANTILLA")
    private Long idPlantilla;

    @Column(name = "NOMBRE", nullable = false, length = 100)
    private String nombre;

    @Column(name = "HORA_INICIO", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "HORA_TERMINO", nullable = false)
    private LocalTime horaTermino;

    @Column(name = "DESCRIPCION", length = 255)
    private String descripcion;

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SlotTurno> slots = new ArrayList<>();

    @OneToMany(mappedBy = "plantilla", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<AsignacionTurno> asignaciones = new ArrayList<>();
}
