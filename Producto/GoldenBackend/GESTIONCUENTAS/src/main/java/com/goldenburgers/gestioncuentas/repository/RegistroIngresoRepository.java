package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.RegistroIngreso;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
 
public interface RegistroIngresoRepository extends JpaRepository<RegistroIngreso, Long> {
    List<RegistroIngreso> findByFechaIngresoBetween(LocalDate inicio, LocalDate fin);
    List<RegistroIngreso> findByEstado(String estado);
    List<RegistroIngreso> findByTipo(String tipo);
    List<RegistroIngreso> findByCanal(String canal);
}
