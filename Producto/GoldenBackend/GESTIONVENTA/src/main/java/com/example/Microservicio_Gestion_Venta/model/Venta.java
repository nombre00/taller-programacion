
package com.example.Microservicio_Gestion_Venta.model;
import java.sql.Timestamp;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "venta")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Venta {
    //Generar el ID autoincrementable y que sea clave primaria
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_venta;

    //Claves Foraneas
    @Column(name= "id_pedido", nullable = false) //El nombre lo coloca por defecto igual que el atributo, pero se puede cambiar
    private Long id_pedido;

    

    @Column(name= "monto_total_venta",nullable = false)
    private Double total_venta;

    @Column(nullable = false)
    private Timestamp fecha_venta;

    
    

    
}