package com.goldenburgers.gestionturnos.dto;

 
import jakarta.validation.constraints.*;
import lombok.Data;
 
import java.time.LocalTime;
 
@Data
public class PlantillaTurnoRequestDTO {
 
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
 
    @NotNull(message = "La hora de inicio es obligatoria")
    private LocalTime horaInicio;
 
    @NotNull(message = "La hora de término es obligatoria")
    private LocalTime horaTermino;
 
    private String descripcion;
}
 