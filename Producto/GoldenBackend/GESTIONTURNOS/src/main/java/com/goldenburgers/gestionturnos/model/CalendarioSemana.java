package com.goldenburgers.gestionturnos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;


@Entity
@Table(name = "CALENDARIOSEMANA")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarioSemana {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CALENDARIO")
    private Long idCalendario;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_SEMANA", nullable = false)
    private SemanaTipo semana;

    @Column(name = "FECHA_INICIO", nullable = false)
    private LocalDate fechaInicio;

    @Column(name = "FECHA_FIN", nullable = false)
    private LocalDate fechaFin;

    @Column(name = "REPETICION_ANUAL", nullable = false)
    private Boolean repeticionAnual = false;
}
