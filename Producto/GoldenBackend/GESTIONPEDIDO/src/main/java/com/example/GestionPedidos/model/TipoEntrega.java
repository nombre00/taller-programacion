package com.example.GestionPedidos.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipoentrega")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoEntrega {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_tipo_entrega")
    private Long idTipoEntrega;

    @Column(name = "nombre_entrega", nullable = false, length = 50)
    private String nombreEntrega;
}