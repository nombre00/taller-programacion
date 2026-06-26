package com.example.Microservicio_Gestion_Venta.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "boleta")
@Data
@NoArgsConstructor
@AllArgsConstructor

public class Boleta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id_boleta;

    @ManyToOne
    @JoinColumn(name = "id_venta", nullable = false)
    private Venta venta;

    @Column(name = "folio_sii", nullable = false, unique = true)
    private String numero_sii;

    @Column(name = "url_boleta", nullable = true)
    private String url_documento;

    @Column(name = "iva")
    private Double iva = 0.19;

    @Column(name="totalconiva" ,nullable = true)
    private Double totalConIva;

    

}
