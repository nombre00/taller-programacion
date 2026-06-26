package com.example.Microservicio_Gestion_Venta.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.Microservicio_Gestion_Venta.model.Venta;

@Repository

public interface VentaRepository  extends JpaRepository<Venta, Long> {

    
}
