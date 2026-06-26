package com.example.GestionPedidos.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.GestionPedidos.model.DetallePedido;

@Repository
public interface DetallePedidoRepository extends JpaRepository<DetallePedido, Long> {

    // Listar detalles por ID de pedido
    List<DetallePedido> findByPedido_IdPedido(Long idPedido);
    

    
    
}