package com.example.GestionPedidos.repository;

import com.example.GestionPedidos.model.Pago;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.List;

@Repository
public interface PagoRepository extends JpaRepository<Pago, Long> {
    
    // Buscar pagos por ID de pedido
    List<Pago> findByIdPedido(Long idPedido);
    
    // Buscar pago por ID de preferencia de Mercado Pago
    Optional<Pago> findByIdPreferenciaMpos(String idPreferenciaMpos);
    
    // Buscar pago por ID de pago de Mercado Pago
    Optional<Pago> findByIdPagoMpos(String idPagoMpos);
    
    // Buscar pagos por estado
    List<Pago> findByEstadoPago(Long estadoPago);
    
    // Buscar pagos por método de pago
    List<Pago> findByMetodoPago(String metodoPago);

    // Para Webpay: buscar pedido por su token  ((nuevo))
    Optional<Pago> findByTokenWebpay(String tokenWebpay);
}