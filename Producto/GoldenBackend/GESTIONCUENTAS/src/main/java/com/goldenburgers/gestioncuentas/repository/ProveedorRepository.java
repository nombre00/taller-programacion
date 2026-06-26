package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.Proveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
 
public interface ProveedorRepository extends JpaRepository<Proveedor, Long> {
    Optional<Proveedor> findByRut(String rut);
    boolean existsByRut(String rut);
}
