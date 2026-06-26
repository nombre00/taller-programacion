package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "SLOTTURNO")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SlotTurno {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_SLOT")
    private Long idSlot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_PLANTILLA", nullable = false)
    private PlantillaTurno plantilla;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_POSICION", nullable = false)
    private Posicion posicion;

    @Column(name = "NOMBRE", length = 100)
    private String nombre;

    @Column(name = "CANTIDAD", nullable = false)
    private Integer cantidad;

    @OneToMany(mappedBy = "slot", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HorarioTrabajador> horarios = new ArrayList<>();
}
