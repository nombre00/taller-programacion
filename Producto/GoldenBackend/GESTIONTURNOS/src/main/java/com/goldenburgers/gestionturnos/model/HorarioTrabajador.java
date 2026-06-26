package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "HORARIOTRABAJADOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HorarioTrabajador {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_HORARIO")
    private Long idHorario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_TRABAJADOR", nullable = true)
    private TrabajadorLocal trabajador;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SLOT", nullable = false)
    private SlotTurno slot;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_ASIGNACION", nullable = false)
    private AsignacionTurno asignacion;

    @Column(name = "FECHA_TRABAJO", nullable = false)
    private LocalDate fechaTrabajo;

    @Column(name = "ESTADO", nullable = false, length = 50)
    private String estado; // pendiente / confirmado / ausente
}
