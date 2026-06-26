package com.goldenburgers.gestionturnos.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
 
@Data
public class SemanaTipoRequestDTO {
 
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;
 
    private String descripcion;
}
 