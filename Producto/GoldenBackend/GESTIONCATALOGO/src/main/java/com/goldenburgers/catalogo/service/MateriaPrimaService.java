package com.goldenburgers.catalogo.service;

import com.goldenburgers.catalogo.dto.DescontarPorVentaItemDTO;
import com.goldenburgers.catalogo.dto.DescontarPorVentaRequestDTO;
import com.goldenburgers.catalogo.dto.IngresarPorCompraRequestDTO;
import com.goldenburgers.catalogo.dto.IngresarPorCompraItemDTO;
import com.goldenburgers.catalogo.dto.StockOperacionResponseDTO;
import com.goldenburgers.catalogo.model.MateriaPrima;
import com.goldenburgers.catalogo.model.MovimientoStock;
import com.goldenburgers.catalogo.model.RecetaDetalle;
import com.goldenburgers.catalogo.repository.MateriaPrimaRepository;
import com.goldenburgers.catalogo.repository.MovimientoStockRepository;
import com.goldenburgers.catalogo.repository.RecetaDetalleRepository;
import com.goldenburgers.catalogo.repository.RecetaRepository;
import com.goldenburgers.catalogo.model.Receta;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;

import com.goldenburgers.catalogo.dto.MateriaPrimaRequestDTO;
import com.goldenburgers.catalogo.dto.MateriaPrimaResponseDTO;
// import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MateriaPrimaService {

    private final MateriaPrimaRepository materiaPrimaRepository;
    private final MovimientoStockRepository movimientoStockRepository;
    private final RecetaRepository recetaRepository;
    private final RecetaDetalleRepository recetaDetalleRepository;

    @Transactional
    public StockOperacionResponseDTO ingresarPorCompra(IngresarPorCompraRequestDTO request) {

        for (IngresarPorCompraItemDTO item : request.getItems()) {

            MateriaPrima mp = materiaPrimaRepository.findById(item.getIdMateriaPrima())
                .orElseThrow(() -> new RuntimeException(
                    "MateriaPrima no encontrada: id=" + item.getIdMateriaPrima()
                ));

            BigDecimal stockActual = mp.getStockActual();
            BigDecimal costoPrev   = mp.getCostoUnitarioPromedio();
            BigDecimal cantidad    = item.getCantidad();
            BigDecimal costoNuevo  = item.getCostoUnitario();

            BigDecimal nuevoPromedio;
            if (stockActual.compareTo(BigDecimal.ZERO) == 0) {
                nuevoPromedio = costoNuevo;
            } else {
                nuevoPromedio = (stockActual.multiply(costoPrev)
                        .add(cantidad.multiply(costoNuevo)))
                        .divide(stockActual.add(cantidad), 2, RoundingMode.HALF_UP);
            }

            mp.setStockActual(stockActual.add(cantidad));
            mp.setCostoUnitarioPromedio(nuevoPromedio);
            materiaPrimaRepository.save(mp);

            MovimientoStock mov = new MovimientoStock();
            mov.setMateriaPrima(mp);
            mov.setTipo("ENTRADA");
            mov.setCantidad(cantidad);
            mov.setCostoUnitario(costoNuevo);
            mov.setOrigen("INGRESO_MERCADERIA");
            mov.setReferenciaId(request.getIdIngresoMercaderia());
            mov.setFecha(LocalDate.now());
            movimientoStockRepository.save(mov);

            log.info("Ingreso registrado — MateriaPrima id={} | cantidad={} | nuevoStock={} | nuevoCostoPromedio={}",
                mp.getIdMateriaPrima(), cantidad, mp.getStockActual(), nuevoPromedio);
        }

        StockOperacionResponseDTO response = new StockOperacionResponseDTO();
        response.setExitoso(true);
        response.setMensaje("Stock actualizado correctamente");
        return response;
    }

    @Transactional
    public StockOperacionResponseDTO descontarPorVenta(DescontarPorVentaRequestDTO request) {

        for (DescontarPorVentaItemDTO item : request.getItems()) {

            // 1. Buscar la receta del producto
            Receta receta = recetaRepository.findByProducto_IdProducto(item.getIdProducto())
                .orElseThrow(() -> new RuntimeException(
                    "Receta no encontrada para producto id=" + item.getIdProducto()
                ));

            // 2. Buscar los detalles (ingredientes) de esa receta
            List<RecetaDetalle> detalles = recetaDetalleRepository
                .findByReceta_IdReceta(receta.getIdReceta());

            // 3. Por cada ingrediente, descontar cantidad * cantidadVendida
            for (RecetaDetalle detalle : detalles) {

                MateriaPrima mp = detalle.getMateriaPrima();
                BigDecimal cantidadADescontar = detalle.getCantidad()
                    .multiply(BigDecimal.valueOf(item.getCantidad()));

                // 4. Descontar stock
                BigDecimal nuevoStock = mp.getStockActual().subtract(cantidadADescontar);
                mp.setStockActual(nuevoStock);
                materiaPrimaRepository.save(mp);

                // 5. Registrar MovimientoStock tipo SALIDA
                MovimientoStock mov = new MovimientoStock();
                mov.setMateriaPrima(mp);
                mov.setTipo("SALIDA");
                mov.setCantidad(cantidadADescontar);
                mov.setOrigen("VENTA");
                mov.setReferenciaId(request.getIdVenta());
                mov.setFecha(LocalDate.now());
                movimientoStockRepository.save(mov);

                // 6. Alerta si stock bajo
                if (nuevoStock.compareTo(mp.getStockMinimo()) <= 0) {
                    log.warn("STOCK BAJO — MateriaPrima id={} nombre={} | stockActual={} | stockMinimo={}",
                        mp.getIdMateriaPrima(), mp.getNombre(), nuevoStock, mp.getStockMinimo());
                }

                log.info("Stock descontado — MateriaPrima id={} | cantidadDescontada={} | nuevoStock={}",
                    mp.getIdMateriaPrima(), cantidadADescontar, nuevoStock);
            }
        }

        StockOperacionResponseDTO response = new StockOperacionResponseDTO();
        response.setExitoso(true);
        response.setMensaje("Stock actualizado correctamente");
        return response;
    }



    // ── CRUD ──────────────────────────────────────────────────

public List<MateriaPrimaResponseDTO> listarTodas() {
    return materiaPrimaRepository.findAll()
        .stream()
        .map(this::toResponseDTO)
        .toList();
}

public MateriaPrimaResponseDTO obtenerPorId(Long id) {
    MateriaPrima mp = materiaPrimaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("MateriaPrima no encontrada: id=" + id));
    return toResponseDTO(mp);
}

@Transactional
public MateriaPrimaResponseDTO crear(MateriaPrimaRequestDTO request) {
    MateriaPrima mp = new MateriaPrima();
    mp.setNombre(request.getNombre());
    mp.setUnidadMedida(request.getUnidadMedida());
    mp.setStockMinimo(request.getStockMinimo());
    mp.setStockActual(
        request.getStockInicial() != null ? request.getStockInicial() : BigDecimal.ZERO
    );
    mp.setCostoUnitarioPromedio(BigDecimal.ZERO);
    mp.setActivo(true);
    return toResponseDTO(materiaPrimaRepository.save(mp));
}

@Transactional
public MateriaPrimaResponseDTO actualizar(Long id, MateriaPrimaRequestDTO request) {
    MateriaPrima mp = materiaPrimaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("MateriaPrima no encontrada: id=" + id));
    mp.setNombre(request.getNombre());
    mp.setUnidadMedida(request.getUnidadMedida());
    mp.setStockMinimo(request.getStockMinimo());
    // stockInicial no se toca en el PUT — el stock solo se mueve via compras/ventas/mermas
    return toResponseDTO(materiaPrimaRepository.save(mp));
}

@Transactional
public void desactivar(Long id) {
    MateriaPrima mp = materiaPrimaRepository.findById(id)
        .orElseThrow(() -> new RuntimeException("MateriaPrima no encontrada: id=" + id));
    mp.setActivo(false);
    materiaPrimaRepository.save(mp);
}

// ── Mapper privado ─────────────────────────────────────────

private MateriaPrimaResponseDTO toResponseDTO(MateriaPrima mp) {
    MateriaPrimaResponseDTO dto = new MateriaPrimaResponseDTO();
    dto.setIdMateriaPrima(mp.getIdMateriaPrima());
    dto.setNombre(mp.getNombre());
    dto.setUnidadMedida(mp.getUnidadMedida());
    dto.setStockActual(mp.getStockActual());
    dto.setStockMinimo(mp.getStockMinimo());
    dto.setCostoUnitarioPromedio(mp.getCostoUnitarioPromedio());
    dto.setActivo(mp.getActivo());
    dto.setStockBajo(mp.getStockActual().compareTo(mp.getStockMinimo()) <= 0);
    return dto;
}
}
