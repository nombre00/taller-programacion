package com.goldenburgers.gestionturnos.dto;

 
import lombok.Data;
 
import java.math.BigDecimal;
 
@Data
public class PosicionResponseDTO {
    private Long idPosicion;
    private String nombre;
    private String descripcion;
    private BigDecimal sueldo;
    private String color;
}
 