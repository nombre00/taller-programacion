package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;


@Entity
@Table(name = "TRABAJADORLOCAL")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TrabajadorLocal {
    @Id
    @Column(name = "ID_TRABAJADOR")
    private Long idTrabajador; // mismo ID que en gestionUsuario, no se autogenera

    @Column(name = "NOMBRE", nullable = false, length = 150)
    private String nombre;

    @Column(name = "ACTIVO", columnDefinition = "TINYINT(1) DEFAULT 1")
    private Boolean activo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_POSICION", nullable = true)
    private Posicion posicion;

    @OneToMany(mappedBy = "trabajador", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HorarioTrabajador> horarios = new ArrayList<>();
}
