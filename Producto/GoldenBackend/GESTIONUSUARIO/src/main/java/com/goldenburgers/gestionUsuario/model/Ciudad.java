package com.goldenburgers.gestionUsuario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "CIUDAD")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Ciudad {
     @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_CIUDAD")
    private Long idCiudad;

    @Column(name = "NOMBRE_CIUDAD", nullable = false, length = 255)
    private String nombreCiudad;

    // Relación con DireccionCliente
    @OneToMany(mappedBy = "ciudad", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DireccionCliente> direcciones = new ArrayList<>();

}
