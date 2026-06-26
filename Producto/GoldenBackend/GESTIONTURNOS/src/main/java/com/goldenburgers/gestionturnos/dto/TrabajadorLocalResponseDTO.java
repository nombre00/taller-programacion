package com.goldenburgers.gestionturnos.dto;


import lombok.Data;
 
@Data
public class TrabajadorLocalResponseDTO {
    private Long idTrabajador;
    private String nombre;
    private Boolean activo;
    private Long idPosicion;
    private String nombrePosicion;
}
 

