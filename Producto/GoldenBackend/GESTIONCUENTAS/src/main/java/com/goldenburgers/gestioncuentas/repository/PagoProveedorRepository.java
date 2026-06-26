package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.PagoProveedor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface PagoProveedorRepository extends JpaRepository<PagoProveedor, Long> {
    List<PagoProveedor> findByCuenta_IdCuenta(Long idCuenta);
    List<PagoProveedor> findByCuentaIdCuenta(Long idCuenta);
}
