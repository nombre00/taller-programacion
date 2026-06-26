package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.CuentaPorPagar;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
 
public interface CuentaPorPagarRepository extends JpaRepository<CuentaPorPagar, Long> {
    List<CuentaPorPagar> findByEstado(String estado);
    List<CuentaPorPagar> findByProveedor_IdProveedor(Long idProveedor);
    // Útil para alertas de vencimiento
    List<CuentaPorPagar> findByEstadoAndFechaVencimientoBefore(String estado, LocalDate fecha);

    // Consultas de listas de cuentas según distintos parámetros.
    @Query("""
        SELECT c FROM CuentaPorPagar c
        WHERE (:estado IS NULL OR c.estado = :estado)
        AND (:fechaEmisionDesde IS NULL OR c.fechaEmision >= :fechaEmisionDesde)
        AND (:fechaEmisionHasta IS NULL OR c.fechaEmision <= :fechaEmisionHasta)
        AND (:fechaVencimientoDesde IS NULL OR c.fechaVencimiento >= :fechaVencimientoDesde)
        AND (:fechaVencimientoHasta IS NULL OR c.fechaVencimiento <= :fechaVencimientoHasta)
        """)
    List<CuentaPorPagar> buscarConFiltros(
        @Param("estado") String estado,
        @Param("fechaEmisionDesde") LocalDate fechaEmisionDesde,
        @Param("fechaEmisionHasta") LocalDate fechaEmisionHasta,
        @Param("fechaVencimientoDesde") LocalDate fechaVencimientoDesde,
        @Param("fechaVencimientoHasta") LocalDate fechaVencimientoHasta
    );
}
