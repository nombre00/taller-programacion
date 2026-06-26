package com.goldenburgers.gestionturnos.dto;


import lombok.Data;
 
@Data
public class SemanaTipoResponseDTO {
    private Long idSemana;
    private String nombre;
    private String descripcion;
    private Boolean activo;
}
 