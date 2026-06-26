package com.goldenburgers.catalogo.repository;

import com.goldenburgers.catalogo.model.RecetaDetalle;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
 
public interface RecetaDetalleRepository extends JpaRepository<RecetaDetalle, Long> {
    List<RecetaDetalle> findByReceta_IdReceta(Long idReceta);
    // Útil para saber qué recetas usan una materia prima específica
    List<RecetaDetalle> findByMateriaPrima_IdMateriaPrima(Long idMateriaPrima);
}
