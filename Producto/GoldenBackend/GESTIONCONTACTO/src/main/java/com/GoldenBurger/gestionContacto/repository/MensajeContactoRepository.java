package com.GoldenBurger.gestionContacto.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.GoldenBurger.gestionContacto.model.MensajeContacto;
import java.util.List;


public interface MensajeContactoRepository extends JpaRepository<MensajeContacto, Long> {
    List<MensajeContacto> findByLeido(Integer leido);
}
