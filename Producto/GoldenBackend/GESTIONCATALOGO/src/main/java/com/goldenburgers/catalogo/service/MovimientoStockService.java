package com.goldenburgers.catalogo.service;

import com.goldenburgers.catalogo.dto.MovimientoStockRequestDTO;
import com.goldenburgers.catalogo.dto.MovimientoStockResponseDTO;
import com.goldenburgers.catalogo.model.*;
import com.goldenburgers.catalogo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MovimientoStockService {

    private final MovimientoStockRepository movimientoStockRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;
    private final RecetaRepository recetaRepository;
    private final RecetaDetalleRepository recetaDetalleRepository;

    // ── CRUD básico ───────────────────────────────────────────

    @Transactional(readOnly = true)
    public List<MovimientoStockResponseDTO> listarTodos() {
        return movimientoStockRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<MovimientoStockResponseDTO> listarPorMateriaPrima(Long idMateriaPrima) {
        return movimientoStockRepository.findByMateriaPrima_IdMateriaPrima(idMateriaPrima)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public MovimientoStockResponseDTO obtenerPorId(Long id) {
        MovimientoStock mov = movimientoStockRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("MovimientoStock no encontrado: id=" + id));
        return toResponseDTO(mov);
    }

    @Transactional
    public MovimientoStockResponseDTO registrar(MovimientoStockRequestDTO request) {
        MateriaPrima mp = materiaPrimaRepository.findById(request.getIdMateriaPrima())
                .orElseThrow(() -> new RuntimeException(
                        "MateriaPrima no encontrada: id=" + request.getIdMateriaPrima()));

        MovimientoStock mov = new MovimientoStock();
        mov.setMateriaPrima(mp);
        mov.setTipo(request.getTipo());
        mov.setCantidad(request.getCantidad());
        mov.setCostoUnitario(request.getCostoUnitario());
        mov.setOrigen(request.getOrigen());
        mov.setReferenciaId(request.getReferenciaId());
        mov.setFecha(LocalDate.now());
        mov.setNota(request.getNota());

        aplicarMovimiento(mp, mov);

        movimientoStockRepository.save(mov);
        materiaPrimaRepository.save(mp);

        return toResponseDTO(mov);
    }

    // ── Endpoint interno para GESTIONVENTA ───────────────────

    @Transactional
    public void descontarStockPorVenta(Long idProducto, BigDecimal cantidadVendida, Long idVenta) {
        Optional<Receta> recetaOpt = recetaRepository.findByProducto_IdProducto(idProducto);
        if (recetaOpt.isEmpty()) return; // Sin receta → no se descuenta stock

        List<RecetaDetalle> detalles = recetaDetalleRepository
                .findByReceta_IdReceta(recetaOpt.get().getIdReceta());

        for (RecetaDetalle detalle : detalles) {
            MateriaPrima mp = detalle.getMateriaPrima();
            BigDecimal cantidadADescontar = detalle.getCantidad().multiply(cantidadVendida);

            MovimientoStock mov = new MovimientoStock();
            mov.setMateriaPrima(mp);
            mov.setTipo("SALIDA");
            mov.setCantidad(cantidadADescontar);
            mov.setOrigen("VENTA");
            mov.setReferenciaId(idVenta);
            mov.setFecha(LocalDate.now());

            mp.setStockActual(mp.getStockActual().subtract(cantidadADescontar));

            movimientoStockRepository.save(mov);
            materiaPrimaRepository.save(mp);
        }
    }

    // ── Helpers ───────────────────────────────────────────────

    private void aplicarMovimiento(MateriaPrima mp, MovimientoStock mov) {
        switch (mov.getTipo()) {
            case "ENTRADA" -> {
                // Actualizar costo promedio ponderado antes de sumar stock
                if (mov.getCostoUnitario() != null && mov.getCostoUnitario().compareTo(BigDecimal.ZERO) > 0) {
                    BigDecimal stockActual = mp.getStockActual();
                    BigDecimal costoActual = mp.getCostoUnitarioPromedio();
                    BigDecimal cantidadNueva = mov.getCantidad();
                    BigDecimal costoNuevo = mov.getCostoUnitario();

                    BigDecimal totalAnterior = stockActual.multiply(costoActual);
                    BigDecimal totalNuevo = cantidadNueva.multiply(costoNuevo);
                    BigDecimal stockTotal = stockActual.add(cantidadNueva);

                    if (stockTotal.compareTo(BigDecimal.ZERO) > 0) {
                        BigDecimal promedio = totalAnterior.add(totalNuevo)
                                .divide(stockTotal, 2, RoundingMode.HALF_UP);
                        mp.setCostoUnitarioPromedio(promedio);
                    }
                }
                mp.setStockActual(mp.getStockActual().add(mov.getCantidad()));
            }
            case "SALIDA", "MERMA" ->
                mp.setStockActual(mp.getStockActual().subtract(mov.getCantidad()));
            case "AJUSTE" ->
                mp.setStockActual(mov.getCantidad()); // El ajuste setea el stock directamente
        }
    }

    private MovimientoStockResponseDTO toResponseDTO(MovimientoStock mov) {
        MovimientoStockResponseDTO dto = new MovimientoStockResponseDTO();
        dto.setIdMovimiento(mov.getIdMovimiento());
        dto.setIdMateriaPrima(mov.getMateriaPrima().getIdMateriaPrima());
        dto.setNombreMateriaPrima(mov.getMateriaPrima().getNombre());
        dto.setTipo(mov.getTipo());
        dto.setCantidad(mov.getCantidad());
        dto.setCostoUnitario(mov.getCostoUnitario());
        dto.setOrigen(mov.getOrigen());
        dto.setReferenciaId(mov.getReferenciaId());
        dto.setFecha(mov.getFecha());
        dto.setNota(mov.getNota());
        dto.setStockResultante(mov.getMateriaPrima().getStockActual());
        return dto;
    }
}