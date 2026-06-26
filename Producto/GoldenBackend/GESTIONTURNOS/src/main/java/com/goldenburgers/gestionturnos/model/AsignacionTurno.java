package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "ASIGNACIONTURNO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AsignacionTurno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_ASIGNACION")
    private Long idAsignacion;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SEMANA", nullable = false)
    private SemanaTipo semana;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANTILLA", nullable = false)
    private PlantillaTurno plantilla;

    @Column(name = "DIA_SEMANA", nullable = false)
    private Integer diaSemana; // 1=lunes ... 7=domingo

    @OneToMany(mappedBy = "asignacion", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HorarioTrabajador> horarios = new ArrayList<>();
}
