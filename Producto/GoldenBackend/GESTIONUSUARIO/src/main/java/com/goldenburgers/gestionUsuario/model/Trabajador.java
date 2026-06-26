package com.goldenburgers.gestionUsuario.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "TRABAJADOR")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Trabajador {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_TRABAJADOR")
    private Long idTrabajador;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_USUARIO", nullable = false, unique = true)
    private Usuario usuario;

    @Column(name = "NOMBRE_TRABAJADOR", nullable = false, length = 255)
    private String nombreTrabajador;

    @Column(name = "RUT_TRABAJADOR", nullable = false, unique = true, length = 12)
    private String rutTrabajador;
}
