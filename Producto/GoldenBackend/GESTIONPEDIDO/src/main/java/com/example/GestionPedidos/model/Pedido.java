package com.example.GestionPedidos.model;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonManagedReference;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Pedido {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_pedido")
    private Long idPedido;

    @Column(name = "id_cliente", nullable = false)
    private Long idCliente;

    @Column(name = "id_estado_pedido", nullable = false)
    private Long idEstadoPedido;

    @Column(name = "id_metodo_pago", nullable = false)
    private Long idMetodoPago;

    @Column(name = "id_tipo_entrega", nullable = false)
    private Long idTipoEntrega;

    @Column(name = "id_direccion_cliente", nullable = true)
    private Long idDireccionEntrega;

    @Column(name = "monto_subtotal", nullable = false)
    private Double montoSubtotal;

    @Column(name = "monto_envio", nullable = false)
    private Double montoEnvio;

    @Column(name = "monto_total", nullable = false)
    private Double montoTotal;

    @Column(name = "fecha_pedido")
    private Timestamp fechaPedido;

    @Column(name = "nota_cliente")
    private String notaCliente;

    @OneToMany(mappedBy = "pedido", cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    @JsonManagedReference
    private List<DetallePedido> detalles = new ArrayList<>();
    }
