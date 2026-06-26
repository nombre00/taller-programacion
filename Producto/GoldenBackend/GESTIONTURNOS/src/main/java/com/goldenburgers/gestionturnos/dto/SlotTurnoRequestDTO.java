package com.goldenburgers.gestionturnos.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
 
@Data
public class SlotTurnoRequestDTO {
 
    @NotNull(message = "El id de la plantilla es obligatorio")
    private Long idPlantilla;
 
    @NotNull(message = "El id de la posición es obligatorio")
    private Long idPosicion;
 
    private String nombre;
 
    @NotNull(message = "La cantidad es obligatoria")
    @Min(value = 1, message = "La cantidad debe ser al menos 1")
    private Integer cantidad;
}
 