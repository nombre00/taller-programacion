package com.goldenburgers.gestionturnos.dto;

 
import lombok.Data;
 
import java.time.LocalTime;
import java.util.List;
 
@Data
public class PlantillaTurnoResponseDTO {
    private Long idPlantilla;
    private String nombre;
    private LocalTime horaInicio;
    private LocalTime horaTermino;
    private String descripcion;
    private List<SlotTurnoResponseDTO> slots;
}
 