package com.goldenburgers.catalogo.repository;

import com.goldenburgers.catalogo.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductoRepository extends JpaRepository<Producto, Long> {
    
    List<Producto> findByIdCategoria(Long idCategoria);
    
    List<Producto> findByDisponible(Integer disponible);
    
    List<Producto> findByNombreProductoContainingIgnoreCase(String nombre);
    
    List<Producto> findByIdCategoriaAndDisponible(Long idCategoria, Integer disponible);
    
    @Modifying
    @Query("UPDATE Producto p SET p.disponible = :disponible WHERE p.idProducto = :id")
    void actualizarDisponibilidad(@Param("id") Long id, @Param("disponible") Integer disponible);
}