package com.goldenburgers.gestioncuentas.dto;

import lombok.Data;

@Data
public class ProveedorResponseDTO {

    private Long idProveedor;
    private String nombre;
    private String rut;
    private String email;
    private String telefono;
    private Boolean activo;
}