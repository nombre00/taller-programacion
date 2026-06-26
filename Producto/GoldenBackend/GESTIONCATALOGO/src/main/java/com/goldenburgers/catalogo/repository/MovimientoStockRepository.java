package com.goldenburgers.catalogo.repository;

import com.goldenburgers.catalogo.model.MovimientoStock;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
 
public interface MovimientoStockRepository extends JpaRepository<MovimientoStock, Long> {
    List<MovimientoStock> findByMateriaPrima_IdMateriaPrima(Long idMateriaPrima);
    List<MovimientoStock> findByTipo(String tipo);
    List<MovimientoStock> findByOrigen(String origen);
    List<MovimientoStock> findByFechaBetween(LocalDate inicio, LocalDate fin);
    // Para trazabilidad: todos los movimientos de una venta o ingreso
    List<MovimientoStock> findByOrigenAndReferenciaId(String origen, Long referenciaId);
}
