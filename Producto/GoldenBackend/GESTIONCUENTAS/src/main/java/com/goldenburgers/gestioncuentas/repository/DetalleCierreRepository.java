package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.DetalleCierre;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface DetalleCierreRepository extends JpaRepository<DetalleCierre, Long> {
    List<DetalleCierre> findByCierreDiarioIdCierre(Long idCierre);
}