package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.IngresoMercaderia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface IngresoMercaderiaRepository extends JpaRepository<IngresoMercaderia, Long> {
    List<IngresoMercaderia> findByCuenta_IdCuenta(Long idCuenta);
    List<IngresoMercaderia> findByEstado(String estado);
}
