package com.example.GestionPedidos.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.example.GestionPedidos.model.DetallePedido;
import com.example.GestionPedidos.model.Pedido;
import com.example.GestionPedidos.repository.DetallePedidoRepository;
import com.example.GestionPedidos.repository.EstadoPedidoRepository;
import com.example.GestionPedidos.repository.PedidoRepository;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class PedidoService {

    @Autowired
    private PedidoRepository pedidoRepository;

    @Autowired
    private EstadoPedidoRepository estadoPedidoRepository;

    @Autowired
    private DetallePedidoRepository detallePedidoRepository;

    // Listar todos los pedidos
    public List<Pedido> listarPedidos() {
        return pedidoRepository.findAll();
    }

    // Obtener pedido por ID
    public Pedido obtenerPedidoPorId(Long id) {
        return pedidoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Pedido no encontrado con ID: " + id));
    }

    // Listar pedidos por cliente
    public List<Pedido> listarPedidosPorCliente(Long idCliente) {
        return pedidoRepository.findByIdCliente(idCliente);
    }

    // Crear nuevo pedido con detalles
    public Pedido crearPedidoConDetalles(Pedido pedido, List<DetallePedido> detalles) {

        // Estado inicial del pedido (Pendiente)
        pedido.setIdEstadoPedido(1L);

        // Si no viene fecha, colocar fecha actual
        if (pedido.getFechaPedido() == null) {
            pedido.setFechaPedido(new Timestamp(System.currentTimeMillis()));
        }

        // Guardar pedido principal
        Pedido pedidoGuardado = pedidoRepository.save(pedido);

        // Guardar los detalles del pedido
        if (detalles != null && !detalles.isEmpty()) {

            for (DetallePedido detalle : detalles) {

                // Asignar ID del pedido recién guardado
                detalle.setPedido(pedidoGuardado);

                // Calcular subtotal si no viene
                if (detalle.getSubtotalLinea() == null) {
                    detalle.setSubtotalLinea(
                            detalle.getCantidad() * detalle.getPrecioUnitario());
                }

                detallePedidoRepository.save(detalle);
            }
        }

        return pedidoGuardado;
    }


    // Eliminar pedido

    public void eliminarPedido(Long idPedido) {
        pedidoRepository.deleteById(idPedido);
    }

    // Cambiar estado de un pedido
    public Pedido cambiarEstadoPedido(Long idPedido, Long idEstado) {
        Pedido pedido = obtenerPedidoPorId(idPedido);

        // Validar que el estado exista
        estadoPedidoRepository.findById(idEstado)
                .orElseThrow(() -> new RuntimeException("Estado no encontrado con ID: " + idEstado));

        pedido.setIdEstadoPedido(idEstado);

        return pedidoRepository.save(pedido);
    }

    // Obtener los productos de un pedido
    public List<DetallePedido> obtenerDetallesPorPedido(Long idCliente) {

        List<Pedido> pedidos = pedidoRepository.findByIdCliente(idCliente);

        List<DetallePedido> detallesTotales = new ArrayList<>();

        for (Pedido pedido : pedidos) {

            List<DetallePedido> detalles = detallePedidoRepository.findByPedido_IdPedido(pedido.getIdPedido());

            detallesTotales.addAll(detalles);
        }

        return detallesTotales;
    }
}