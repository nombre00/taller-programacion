package com.goldenburgers.gestioncuentas.service;

import com.goldenburgers.gestioncuentas.dto.PagoProveedorRequestDTO;
import com.goldenburgers.gestioncuentas.dto.PagoProveedorResponseDTO;
import com.goldenburgers.gestioncuentas.model.CuentaPorPagar;
import com.goldenburgers.gestioncuentas.model.PagoProveedor;
import com.goldenburgers.gestioncuentas.repository.CuentaPorPagarRepository;
import com.goldenburgers.gestioncuentas.repository.PagoProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PagoProveedorService {

    private final PagoProveedorRepository pagoProveedorRepository;
    private final CuentaPorPagarRepository cuentaPorPagarRepository;

    @Transactional
    public PagoProveedorResponseDTO registrarPago(PagoProveedorRequestDTO dto) {
        CuentaPorPagar cuenta = cuentaPorPagarRepository.findById(dto.getIdCuenta())
                .orElseThrow(() -> new RuntimeException("Cuenta no encontrada con id: " + dto.getIdCuenta()));

        if (cuenta.getEstado().equals("PAGADO") || cuenta.getEstado().equals("ANULADO")) {
            throw new RuntimeException("La cuenta ya está en estado: " + cuenta.getEstado());
        }

        if (dto.getMontoPagado().compareTo(cuenta.getMontoTotal()) != 0) {
            throw new RuntimeException(
                "El monto pagado (" + dto.getMontoPagado() + ") debe ser igual al monto total de la cuenta (" + cuenta.getMontoTotal() + ")"
            );
        }

        PagoProveedor pago = new PagoProveedor();
        pago.setCuenta(cuenta);
        pago.setFechaPago(LocalDate.now());
        pago.setMontoPagado(dto.getMontoPagado());
        pago.setMetodoPago(dto.getMetodoPago());
        pago.setComprobanteRef(dto.getComprobanteRef());

        pagoProveedorRepository.save(pago);

        cuenta.setEstado("PAGADO");
        cuentaPorPagarRepository.save(cuenta);

        return toResponse(pago);
    }

    public List<PagoProveedorResponseDTO> listarPorCuenta(Long idCuenta) {
        if (!cuentaPorPagarRepository.existsById(idCuenta)) {
            throw new RuntimeException("Cuenta no encontrada con id: " + idCuenta);
        }
        return pagoProveedorRepository.findByCuentaIdCuenta(idCuenta)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private PagoProveedorResponseDTO toResponse(PagoProveedor pago) {
        PagoProveedorResponseDTO dto = new PagoProveedorResponseDTO();
        dto.setIdPagoProv(pago.getIdPagoProv());
        dto.setIdCuenta(pago.getCuenta().getIdCuenta());
        dto.setDescripcionCuenta(pago.getCuenta().getDescripcion());
        dto.setNombreProveedor(pago.getCuenta().getProveedor().getNombre());
        dto.setFechaPago(pago.getFechaPago());
        dto.setMontoPagado(pago.getMontoPagado());
        dto.setMetodoPago(pago.getMetodoPago());
        dto.setComprobanteRef(pago.getComprobanteRef());
        return dto;
    }
}