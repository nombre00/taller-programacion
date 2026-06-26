package com.example.Microservicio_Gestion_Venta.service;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.Microservicio_Gestion_Venta.model.Boleta;
import com.example.Microservicio_Gestion_Venta.model.Venta;
import com.example.Microservicio_Gestion_Venta.repository.BoletaRepository;
import com.example.Microservicio_Gestion_Venta.repository.VentaRepository;

@Service
@Transactional
public class VentaService {

    @Autowired
    private VentaRepository ventaRepository;

    @Autowired
    private BoletaRepository boletaRepository;

    @Autowired
    private PedidoClienteService pedidoClienteService;

    @Autowired
    private CatalogoClienteService catalogoClienteService;

    @Autowired
    private CuentasClienteService cuentasClienteService;

    // CRUD

    public List<Venta> listarVentas() {
        return ventaRepository.findAll();
    }

    public Venta guardarVenta(Venta venta) {
        return ventaRepository.save(venta);
    }

    public void eliminarVenta(Long id) {
        ventaRepository.deleteById(id);
    }

    public Venta obtenerVentaPorId(Long id) {
        return ventaRepository.findById(id).orElse(null);
    }

    public Venta crearVentaDesdePedidoconBoleta(Long idPedido, String token) {
        try {
            System.out.println("Iniciando creación de venta desde pedido: " + idPedido);

            // 1. Consultar el pedido desde GESTIONPEDIDO
            Map<String, Object> pedidoData = pedidoClienteService.obtenerPedidoCompleto(idPedido, token);

            // 2. Extraer datos del pedido
            Double montoTotal = getDoubleValue(pedidoData.get("montoTotal"));
            if (montoTotal == null || montoTotal <= 0) {
                throw new RuntimeException("El monto del pedido es inválido");
            }

            // Extraer items del pedido para descontar stock
            // Se asume que pedidoData tiene una lista "items" con idProducto y cantidad
            List<Map<String, Object>> items = (List<Map<String, Object>>) pedidoData.get("items");

            // 3. Crear registro de Venta
            Venta venta = new Venta();
            venta.setId_pedido(idPedido);
            venta.setTotal_venta(montoTotal);
            venta.setFecha_venta(Timestamp.valueOf(LocalDateTime.now()));
            Venta ventaGuardada = ventaRepository.save(venta);

            // 4. Crear registro de Boleta
            Boleta boleta = new Boleta();
            boleta.setVenta(ventaGuardada);
            Double iva = montoTotal * 0.19;
            boleta.setIva(iva);
            boleta.setNumero_sii(generarNumeroSII(ventaGuardada.getId_venta()));
            boleta.setUrl_documento(null);
            boleta.setTotalConIva(montoTotal + iva);
            boletaRepository.save(boleta);
            System.out.println("Boleta creada: " + boleta.getNumero_sii());

            // 5. Descontar stock en GESTIONCATALOGO
            if (items != null && !items.isEmpty()) {
                try {
                    Map<String, Object> catalogoRequest = new HashMap<>();
                    catalogoRequest.put("idVenta", ventaGuardada.getId_venta());
                    catalogoRequest.put("items", items);
                    catalogoClienteService.descontarPorVenta(catalogoRequest, token);
                    System.out.println("✅ Stock descontado para venta id=" + ventaGuardada.getId_venta());
                } catch (Exception e) {
                    System.err.println("⚠️ Error al descontar stock en GESTIONCATALOGO: " + e.getMessage());
                    // No se lanza excepción — la venta ya está creada, se loguea para revisión manual
                }
            } else {
                System.out.println("⚠️ No se encontraron items en el pedido para descontar stock");
            }

            // 6. Registrar ingreso en GESTIONCUENTAS
            try {
                Map<String, Object> cuentasRequest = new HashMap<>();
                cuentasRequest.put("idVentaRef", ventaGuardada.getId_venta());
                cuentasRequest.put("montoBruto", montoTotal);
                cuentasRequest.put("ivaDebito", iva);
                cuentasRequest.put("canal", "LOCAL"); // valor por defecto, ajustar según negocio
                cuentasClienteService.registrarIngreso(cuentasRequest, token);
                System.out.println("✅ Ingreso registrado en GESTIONCUENTAS para venta id=" + ventaGuardada.getId_venta());
            } catch (Exception e) {
                System.err.println("⚠️ Error al registrar ingreso en GESTIONCUENTAS: " + e.getMessage());
                // No se lanza excepción — se loguea para revisión manual
            }

            System.out.println("Venta y Boleta creadas exitosamente para pedido: " + idPedido);
            return ventaGuardada;

        } catch (Exception e) {
            System.err.println("Error al crear venta desde pedido: " + e.getMessage());
            throw new RuntimeException("Error al crear venta desde pedido: " + e.getMessage(), e);
        }
    }

    private String generarNumeroSII(Long idVenta) {
        int anio = LocalDateTime.now().getYear();
        return String.format("BOL-%d-%08d", anio, idVenta);
    }

    private Double getDoubleValue(Object value) {
        if (value == null) return null;
        if (value instanceof Integer) return ((Integer) value).doubleValue();
        if (value instanceof Double) return (Double) value;
        if (value instanceof String) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}
