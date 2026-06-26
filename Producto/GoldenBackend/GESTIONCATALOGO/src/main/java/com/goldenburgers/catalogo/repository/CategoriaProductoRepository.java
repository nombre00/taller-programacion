package com.goldenburgers.catalogo.repository;

import com.goldenburgers.catalogo.model.CategoriaProducto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoriaProductoRepository extends JpaRepository<CategoriaProducto, Long> {
    
    Optional<CategoriaProducto> findByNombreCategoria(String nombreCategoria);
    
    boolean existsByNombreCategoria(String nombreCategoria);

    
}