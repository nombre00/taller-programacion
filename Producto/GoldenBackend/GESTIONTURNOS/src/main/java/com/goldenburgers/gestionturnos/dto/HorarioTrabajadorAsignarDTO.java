package com.goldenburgers.gestionturnos.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class HorarioTrabajadorAsignarDTO {

    @NotNull(message = "El id del trabajador es obligatorio")
    private Long idTrabajador;
}