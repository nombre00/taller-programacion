package com.goldenburgers.gestioncuentas.service;

import com.goldenburgers.gestioncuentas.dto.CuentaPorPagarResponseDTO;
import com.goldenburgers.gestioncuentas.model.CuentaPorPagar;
import com.goldenburgers.gestioncuentas.repository.CuentaPorPagarRepository;
import com.goldenburgers.gestioncuentas.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CuentaPorPagarService {

    private final CuentaPorPagarRepository cuentaPorPagarRepository;
    private final ProveedorRepository proveedorRepository;

    public List<CuentaPorPagarResponseDTO> listarTodas(String estado) {
        List<CuentaPorPagar> cuentas = (estado != null && !estado.isBlank())
                ? cuentaPorPagarRepository.findByEstado(estado)
                : cuentaPorPagarRepository.findAll();
        return cuentas.stream().map(this::toResponse).collect(Collectors.toList());
    }

    public CuentaPorPagarResponseDTO obtenerPorId(Long id) {
        CuentaPorPagar cuenta = cuentaPorPagarRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con id: " + id));
        return toResponse(cuenta);
    }

    public List<CuentaPorPagarResponseDTO> listarPorProveedor(Long idProveedor) {
        if (!proveedorRepository.existsById(idProveedor)) {
            throw new RuntimeException("Proveedor no encontrado con id: " + idProveedor);
        }
        return cuentaPorPagarRepository.findByProveedor_IdProveedor(idProveedor)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public List<CuentaPorPagarResponseDTO> buscarConFiltros(
            String estado,
            LocalDate fechaEmisionDesde,
            LocalDate fechaEmisionHasta,
            LocalDate fechaVencimientoDesde,
            LocalDate fechaVencimientoHasta) {

        return cuentaPorPagarRepository.buscarConFiltros(
                        (estado != null && !estado.isBlank()) ? estado : null,
                        fechaEmisionDesde,
                        fechaEmisionHasta,
                        fechaVencimientoDesde,
                        fechaVencimientoHasta)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private CuentaPorPagarResponseDTO toResponse(CuentaPorPagar cuenta) {
        CuentaPorPagarResponseDTO dto = new CuentaPorPagarResponseDTO();
        dto.setIdCuenta(cuenta.getIdCuenta());
        dto.setIdProveedor(cuenta.getProveedor().getIdProveedor());
        dto.setNombreProveedor(cuenta.getProveedor().getNombre());
        dto.setTipoGasto(cuenta.getTipoGasto());
        dto.setDescripcion(cuenta.getDescripcion());
        dto.setMontoTotal(cuenta.getMontoTotal());
        dto.setIvaCredito(cuenta.getIvaCredito());
        dto.setFechaEmision(cuenta.getFechaEmision());
        dto.setFechaVencimiento(cuenta.getFechaVencimiento());
        dto.setEstado(cuenta.getEstado());
        dto.setNumeroDocumento(cuenta.getNumeroDocumento());
        return dto;
    }
}