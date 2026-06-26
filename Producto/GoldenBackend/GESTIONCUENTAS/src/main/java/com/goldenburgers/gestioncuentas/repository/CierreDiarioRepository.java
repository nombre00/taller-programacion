package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.CierreDiario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;

public interface CierreDiarioRepository extends JpaRepository<CierreDiario, Long> {
    List<CierreDiario> findByFechaCierre(LocalDate fechaCierre);
    List<CierreDiario> findByFechaCierreAndEstado(LocalDate fechaCierre, String estado);
    boolean existsByFechaCierreAndTurno(LocalDate fechaCierre, String turno);
}