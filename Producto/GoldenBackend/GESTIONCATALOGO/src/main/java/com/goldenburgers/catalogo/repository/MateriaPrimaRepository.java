package com.goldenburgers.catalogo.repository;

import com.goldenburgers.catalogo.model.MateriaPrima;
import org.springframework.data.jpa.repository.JpaRepository;
import java.math.BigDecimal;
import java.util.List;
 
public interface MateriaPrimaRepository extends JpaRepository<MateriaPrima, Long> {
    List<MateriaPrima> findByActivoTrue();
    // Útil para alertas de stock bajo
    List<MateriaPrima> findByStockActualLessThanEqualAndActivoTrue(BigDecimal stockMinimo);
}
