package com.goldenburgers.catalogo.service;

import com.goldenburgers.catalogo.dto.*;
import com.goldenburgers.catalogo.model.*;
import com.goldenburgers.catalogo.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class RecetaService {

    private final RecetaRepository recetaRepository;
    private final RecetaDetalleRepository recetaDetalleRepository;
    private final ProductoRepository productoRepository;
    private final MateriaPrimaRepository materiaPrimaRepository;

    public List<RecetaResponseDTO> listarTodas() {
        return recetaRepository.findAll()
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }

    public RecetaResponseDTO obtenerPorId(Long id) {
        Receta receta = recetaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Receta no encontrada: id=" + id));
        return toResponseDTO(receta);
    }

    @Transactional
    public RecetaResponseDTO crear(RecetaRequestDTO request) {

        // Un producto solo puede tener una receta
        if (recetaRepository.existsByProducto_IdProducto(request.getIdProducto())) {
            throw new RuntimeException(
                "Ya existe una receta para el producto id=" + request.getIdProducto()
            );
        }

        Producto producto = productoRepository.findById(request.getIdProducto())
            .orElseThrow(() -> new RuntimeException(
                "Producto no encontrado: id=" + request.getIdProducto()
            ));

        Receta receta = new Receta();
        receta.setProducto(producto);
        receta.setDescripcion(request.getDescripcion());
        receta.setActivo(true);
        recetaRepository.save(receta);

        guardarDetalles(receta, request.getDetalles());

        return toResponseDTO(recetaRepository.findById(receta.getIdReceta()).orElseThrow());
    }

    @Transactional
    public RecetaResponseDTO actualizar(Long id, RecetaRequestDTO request) {
        Receta receta = recetaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Receta no encontrada: id=" + id));

        receta.setDescripcion(request.getDescripcion());
        // idProducto no se cambia en el PUT — una receta está fija a su producto

        // Reemplazar detalles: borrar los anteriores y crear los nuevos
        recetaDetalleRepository.deleteAll(
            recetaDetalleRepository.findByReceta_IdReceta(id)
        );
        guardarDetalles(receta, request.getDetalles());

        return toResponseDTO(recetaRepository.findById(id).orElseThrow());
    }

    @Transactional
    public void desactivar(Long id) {
        Receta receta = recetaRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Receta no encontrada: id=" + id));
        receta.setActivo(false);
        recetaRepository.save(receta);
    }

    // ── Helpers ───────────────────────────────────────────────

    private void guardarDetalles(Receta receta, List<RecetaDetalleRequestDTO> items) {
        for (RecetaDetalleRequestDTO item : items) {
            MateriaPrima mp = materiaPrimaRepository.findById(item.getIdMateriaPrima())
                .orElseThrow(() -> new RuntimeException(
                    "MateriaPrima no encontrada: id=" + item.getIdMateriaPrima()
                ));
            RecetaDetalle detalle = new RecetaDetalle();
            detalle.setReceta(receta);
            detalle.setMateriaPrima(mp);
            detalle.setCantidad(item.getCantidad());
            detalle.setUnidadMedida(item.getUnidadMedida());
            recetaDetalleRepository.save(detalle);
        }
    }

    private RecetaDetalleResponseDTO toDetalleResponseDTO(RecetaDetalle d) {
        MateriaPrima mp = d.getMateriaPrima();
        BigDecimal costo = mp.getCostoUnitarioPromedio() != null 
            ? mp.getCostoUnitarioPromedio() 
            : BigDecimal.ZERO;
        BigDecimal costoLinea = d.getCantidad().multiply(costo);

        RecetaDetalleResponseDTO dto = new RecetaDetalleResponseDTO();
        dto.setIdDetalle(d.getIdDetalle());
        dto.setIdMateriaPrima(mp.getIdMateriaPrima());
        dto.setNombreMateriaPrima(mp.getNombre());
        dto.setCantidad(d.getCantidad());
        dto.setUnidadMedida(d.getUnidadMedida());
        dto.setCostoUnitarioPromedio(costo);
        dto.setCostoLinea(costoLinea);
        return dto;
    }

    private RecetaResponseDTO toResponseDTO(Receta receta) {
        List<RecetaDetalleResponseDTO> detalles = recetaDetalleRepository
            .findByReceta_IdReceta(receta.getIdReceta())
            .stream()
            .map(this::toDetalleResponseDTO)
            .toList();

        BigDecimal costoTotal = detalles.stream()
            .map(RecetaDetalleResponseDTO::getCostoLinea)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        RecetaResponseDTO dto = new RecetaResponseDTO();
        dto.setIdReceta(receta.getIdReceta());
        dto.setIdProducto(receta.getProducto().getIdProducto());
        dto.setNombreProducto(receta.getProducto().getNombreProducto());
        dto.setDescripcion(receta.getDescripcion());
        dto.setActivo(receta.getActivo());
        dto.setDetalles(detalles);
        dto.setCostoTotal(costoTotal);
        return dto;
    }
}