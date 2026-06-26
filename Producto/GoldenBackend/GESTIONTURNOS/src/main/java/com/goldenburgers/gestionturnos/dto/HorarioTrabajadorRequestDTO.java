package com.goldenburgers.gestionturnos.dto;


import jakarta.validation.constraints.*;
import lombok.Data;
 
import java.time.LocalDate;
 
@Data
public class HorarioTrabajadorRequestDTO {
 
    @NotNull(message = "El id del trabajador es obligatorio")
    private Long idTrabajador;
 
    @NotNull(message = "El id del slot es obligatorio")
    private Long idSlot;
 
    @NotNull(message = "El id de la asignación es obligatorio")
    private Long idAsignacion;
 
    @NotNull(message = "La fecha de trabajo es obligatoria")
    private LocalDate fechaTrabajo;
 
    @NotBlank(message = "El estado es obligatorio")
    private String estado; // pendiente / confirmado / ausente
}
 