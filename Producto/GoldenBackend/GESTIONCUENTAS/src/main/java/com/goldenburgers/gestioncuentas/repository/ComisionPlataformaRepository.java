package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.ComisionPlataforma;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
 
public interface ComisionPlataformaRepository extends JpaRepository<ComisionPlataforma, Long> {
    Optional<ComisionPlataforma> findByMetodoPagoRef(String metodoPagoRef);
}
