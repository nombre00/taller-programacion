package com.goldenburgers.gestioncuentas.service;

import com.goldenburgers.gestioncuentas.dto.*;
import com.goldenburgers.gestioncuentas.model.CuentaPorPagar;
import com.goldenburgers.gestioncuentas.model.IngresoMercaderia;
import com.goldenburgers.gestioncuentas.model.Proveedor;
import com.goldenburgers.gestioncuentas.repository.CuentaPorPagarRepository;
import com.goldenburgers.gestioncuentas.repository.IngresoMercaderiaRepository;
import com.goldenburgers.gestioncuentas.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CompraService {

    private final ProveedorRepository proveedorRepository;
    private final CuentaPorPagarRepository cuentaPorPagarRepository;
    private final IngresoMercaderiaRepository ingresoMercaderiaRepository;
    private final CatalogoClienteService catalogoClienteService;

    @Transactional
    public RegistrarCompraResponseDTO registrarCompra(RegistrarCompraRequestDTO request, String token) {

        // 1. Validar que el proveedor existe
        Proveedor proveedor = proveedorRepository.findById(request.getIdProveedor())
            .orElseThrow(() -> new RuntimeException(
                "Proveedor no encontrado: id=" + request.getIdProveedor()
            ));

        // 2. Calcular monto total (suma de cantidad * costoUnitario por ítem)
        BigDecimal montoTotal = request.getItems().stream()
            .map(i -> i.getCantidad().multiply(i.getCostoUnitario()))
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 3. Crear CuentaPorPagar
        CuentaPorPagar cuenta = new CuentaPorPagar();
        cuenta.setProveedor(proveedor);
        cuenta.setTipoGasto("MERCADERIA");
        cuenta.setDescripcion("Compra de materia prima - doc: " + request.getNumeroDocumento());
        cuenta.setMontoTotal(montoTotal);
        cuenta.setIvaCredito(request.getIvaCredito());
        cuenta.setFechaEmision(request.getFechaEmision());
        cuenta.setFechaVencimiento(request.getFechaVencimiento());
        cuenta.setEstado("PENDIENTE");
        cuenta.setNumeroDocumento(request.getNumeroDocumento());
        cuenta = cuentaPorPagarRepository.save(cuenta);

        // 4. Crear un IngresoMercaderia por cada ítem en estado PENDIENTE
        List<IngresoMercaderia> ingresos = new ArrayList<>();
        for (ItemCompraDTO item : request.getItems()) {
            IngresoMercaderia ingreso = new IngresoMercaderia();
            ingreso.setCuenta(cuenta);
            ingreso.setIdMateriaPrimaRef(item.getIdMateriaPrima());
            ingreso.setCantidad(item.getCantidad());
            ingreso.setUnidadMedida(item.getUnidadMedida());
            ingreso.setCostoUnitario(item.getCostoUnitario());
            ingreso.setEstado("PENDIENTE");
            ingresos.add(ingresoMercaderiaRepository.save(ingreso));
        }

        // 5. Construir request para GESTIONCATALOGO
        IngresarCompraRequestDTO catalogoRequest = new IngresarCompraRequestDTO();
        catalogoRequest.setIdIngresoMercaderia(cuenta.getIdCuenta());

        List<IngresarCompraItemDTO> catalogoItems = new ArrayList<>();
        for (ItemCompraDTO item : request.getItems()) {
            IngresarCompraItemDTO ci = new IngresarCompraItemDTO();
            ci.setIdMateriaPrima(item.getIdMateriaPrima());
            ci.setCantidad(item.getCantidad());
            ci.setCostoUnitario(item.getCostoUnitario());
            catalogoItems.add(ci);
        }
        catalogoRequest.setItems(catalogoItems);

        // 6. Llamar a GESTIONCATALOGO
        String estadoStock;
        String mensajeFinal;

        try {
            StockOperacionResponseDTO catalogoResponse =
                catalogoClienteService.ingresarPorCompra(catalogoRequest, token);

            if (Boolean.TRUE.equals(catalogoResponse.getExitoso())) {
                ingresos.forEach(i -> {
                    i.setEstado("CONFIRMADO");
                    ingresoMercaderiaRepository.save(i);
                });
                estadoStock = "CONFIRMADO";
                mensajeFinal = "Compra registrada y stock actualizado correctamente";
                log.info("Compra registrada OK — cuenta id={} | monto={}", cuenta.getIdCuenta(), montoTotal);
            } else {
                ingresos.forEach(i -> {
                    i.setEstado("ERROR");
                    ingresoMercaderiaRepository.save(i);
                });
                estadoStock = "ERROR";
                mensajeFinal = "Compra registrada pero el stock no pudo actualizarse";
                log.warn("GESTIONCATALOGO respondió exitoso=false — cuenta id={}", cuenta.getIdCuenta());
            }

        } catch (Exception e) {
            ingresos.forEach(i -> {
                i.setEstado("ERROR");
                ingresoMercaderiaRepository.save(i);
            });
            estadoStock = "ERROR";
            mensajeFinal = "Compra registrada pero el stock no pudo actualizarse: " + e.getMessage();
            log.error("Error al llamar a GESTIONCATALOGO — cuenta id={} | error={}",
                cuenta.getIdCuenta(), e.getMessage());
        }

        // 7. Armar response
        RegistrarCompraResponseDTO response = new RegistrarCompraResponseDTO();
        response.setIdCuenta(cuenta.getIdCuenta());
        response.setNumeroDocumento(cuenta.getNumeroDocumento());
        response.setMontoTotal(montoTotal);
        response.setEstadoCuenta(cuenta.getEstado());
        response.setEstadoStock(estadoStock);
        response.setMensaje(mensajeFinal);
        return response;
    }
}
