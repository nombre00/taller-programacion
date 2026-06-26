package com.goldenburgers.catalogo.repository;

import com.goldenburgers.catalogo.model.Receta;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
 
public interface RecetaRepository extends JpaRepository<Receta, Long> {
    Optional<Receta> findByProducto_IdProducto(Long idProducto);
    boolean existsByProducto_IdProducto(Long idProducto);
}
