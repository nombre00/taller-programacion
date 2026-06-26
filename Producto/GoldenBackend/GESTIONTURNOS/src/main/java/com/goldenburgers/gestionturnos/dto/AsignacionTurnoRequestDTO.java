package com.goldenburgers.gestionturnos.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
 
@Data
public class AsignacionTurnoRequestDTO {
 
    @NotNull(message = "El id de la semana tipo es obligatorio")
    private Long idSemana;
 
    @NotNull(message = "El id de la plantilla es obligatorio")
    private Long idPlantilla;
 
    @NotNull(message = "El día de la semana es obligatorio")
    @Min(value = 1, message = "El día debe ser entre 1 (lunes) y 7 (domingo)")
    @Max(value = 7, message = "El día debe ser entre 1 (lunes) y 7 (domingo)")
    private Integer diaSemana;
}
 