package com.goldenburgers.gestionturnos.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
 
import java.time.LocalDate;
 
@Data
public class CalendarioSemanaRequestDTO {
 
    @NotNull(message = "El id de la semana tipo es obligatorio")
    private Long idSemana;
 
    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;
 
    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;
 
    private Boolean repeticionAnual = false;
}
 