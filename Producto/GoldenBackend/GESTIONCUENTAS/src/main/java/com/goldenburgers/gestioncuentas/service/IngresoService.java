package com.goldenburgers.gestioncuentas.service;

import com.goldenburgers.gestioncuentas.dto.RegistrarIngresoRequestDTO;
import com.goldenburgers.gestioncuentas.dto.RegistrarIngresoResponseDTO;
import com.goldenburgers.gestioncuentas.model.RegistroIngreso;
import com.goldenburgers.gestioncuentas.repository.RegistroIngresoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class IngresoService {

    private final RegistroIngresoRepository registroIngresoRepository;

    @Transactional
    public RegistrarIngresoResponseDTO registrarIngreso(RegistrarIngresoRequestDTO request) {

        // montoNeto = montoBruto - montoDescuento - montoComision
        // Por ahora descuento y comision son 0
        BigDecimal montoNeto = request.getMontoBruto()
            .subtract(BigDecimal.ZERO)  // descuento
            .subtract(BigDecimal.ZERO); // comision

        RegistroIngreso ingreso = new RegistroIngreso();
        ingreso.setIdVentaRef(request.getIdVentaRef());
        ingreso.setMontoBruto(request.getMontoBruto());
        ingreso.setMontoDescuento(BigDecimal.ZERO);
        ingreso.setMontoComision(BigDecimal.ZERO);
        ingreso.setMontoNeto(montoNeto);
        ingreso.setIvaDebito(request.getIvaDebito());
        ingreso.setCanal(request.getCanal());
        ingreso.setTipo("VENTA");
        ingreso.setEstado("PENDIENTE");
        registroIngresoRepository.save(ingreso);

        log.info("RegistroIngreso creado — idVentaRef={} | montoBruto={} | montoNeto={}",
            request.getIdVentaRef(), request.getMontoBruto(), montoNeto);

        RegistrarIngresoResponseDTO response = new RegistrarIngresoResponseDTO();
        response.setIdIngreso(ingreso.getIdIngreso());
        response.setMontoNeto(montoNeto);
        response.setEstado(ingreso.getEstado());
        response.setMensaje("Ingreso registrado correctamente");
        return response;
    }
}