package com.goldenburgers.gestionturnos.dto;


import lombok.Data;
 
@Data
public class SlotTurnoResponseDTO {
    private Long idSlot;
    private Long idPlantilla;
    private String nombrePlantilla;
    private Long idPosicion;
    private String nombrePosicion;
    private String colorPosicion;
    private String nombre;
    private Integer cantidad;
}
 