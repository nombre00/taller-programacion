package com.goldenburgers.gestionUsuario.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "DIRECCIONCLIENTE")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DireccionCliente {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ID_DIRECCION")
    private Long idDireccion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ID_CLIENTE", nullable = false)
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ID_CIUDAD", nullable = false)
    private Ciudad ciudad;

    @Column(name = "DIRECCION", nullable = false, length = 500)
    private String direccion;

    @Column(name = "ALIAS", length = 100)
    private String alias; // ej. "Casa", "Oficina"
}
