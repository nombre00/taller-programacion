package com.goldenburgers.gestionturnos.dto;


import lombok.Data;
 
import java.time.LocalDate;
import java.time.LocalTime;
 
@Data
public class HorarioTrabajadorResponseDTO {
    private Long idHorario;
    private Long idTrabajador;
    private String nombreTrabajador;
    private Long idSlot;
    private String nombreSlot;
    private Long idPosicion;
    private String nombrePosicion;
    private Long idPlantilla;
    private String nombrePlantilla;
    private LocalTime horaInicio;
    private LocalTime horaTermino;
    private Long idAsignacion;
    private LocalDate fechaTrabajo;
    private String estado;
}
 