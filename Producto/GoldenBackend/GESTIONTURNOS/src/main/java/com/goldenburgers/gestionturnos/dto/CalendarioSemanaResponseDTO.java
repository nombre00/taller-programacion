package com.goldenburgers.gestionturnos.dto;


import lombok.Data;
 
import java.time.LocalDate;
 
@Data
public class CalendarioSemanaResponseDTO {
    private Long idCalendario;
    private Long idSemana;
    private String nombreSemana;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean repeticionAnual;
}
 