package com.goldenburgers.gestionturnos.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
 
@Data
public class TrabajadorLocalRequestDTO {
 
    @NotNull(message = "El id del trabajador es obligatorio (debe coincidir con GESTIONUSUARIO)")
    private Long idTrabajador;
 
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
 
    @NotNull(message = "El id de la posición es obligatorio")
    private Long idPosicion;
}
 