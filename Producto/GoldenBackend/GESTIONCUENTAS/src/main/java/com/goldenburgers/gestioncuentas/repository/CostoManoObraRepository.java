package com.goldenburgers.gestioncuentas.repository;

import com.goldenburgers.gestioncuentas.model.CostoManoObra;
import org.springframework.data.jpa.repository.JpaRepository;
import java.time.LocalDate;
import java.util.List;
 
public interface CostoManoObraRepository extends JpaRepository<CostoManoObra, Long> {
    List<CostoManoObra> findByPeriodoInicioBetween(LocalDate inicio, LocalDate fin);
    List<CostoManoObra> findByIdPosicionRef(Long idPosicionRef);
}
