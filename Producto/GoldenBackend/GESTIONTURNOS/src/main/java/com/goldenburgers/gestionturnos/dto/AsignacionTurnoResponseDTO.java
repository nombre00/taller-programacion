package com.goldenburgers.gestionturnos.dto;


import lombok.Data;
 
@Data
public class AsignacionTurnoResponseDTO {
    private Long idAsignacion;
    private Long idSemana;
    private String nombreSemana;
    private Long idPlantilla;
    private String nombrePlantilla;
    private Integer diaSemana; // 1=lunes ... 7=domingo
}
 