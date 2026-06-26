package com.example.gestionCuentas;

import com.goldenburgers.gestioncuentas.dto.PagoProveedorRequestDTO;
import com.goldenburgers.gestioncuentas.dto.PagoProveedorResponseDTO;
import com.goldenburgers.gestioncuentas.model.CuentaPorPagar;
import com.goldenburgers.gestioncuentas.model.PagoProveedor;
import com.goldenburgers.gestioncuentas.model.Proveedor;
import com.goldenburgers.gestioncuentas.repository.CuentaPorPagarRepository;
import com.goldenburgers.gestioncuentas.repository.PagoProveedorRepository;
import com.goldenburgers.gestioncuentas.service.PagoProveedorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoProveedorServiceTest {

    @Mock
    private PagoProveedorRepository pagoProveedorRepository;

    @Mock
    private CuentaPorPagarRepository cuentaPorPagarRepository;

    @InjectMocks
    private PagoProveedorService pagoProveedorService;

    private CuentaPorPagar cuenta;
    private PagoProveedorRequestDTO requestDTO;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Distribuidora Sur");

        cuenta = new CuentaPorPagar();
        cuenta.setIdCuenta(1L);
        cuenta.setProveedor(proveedor);
        cuenta.setDescripcion("Factura insumos enero");
        cuenta.setMontoTotal(new BigDecimal("100000.00"));
        cuenta.setEstado("PENDIENTE");

        requestDTO = new PagoProveedorRequestDTO();
        requestDTO.setIdCuenta(1L);
        requestDTO.setMontoPagado(new BigDecimal("100000.00"));
        requestDTO.setMetodoPago("TRANSFERENCIA");
        requestDTO.setComprobanteRef("TRF-001");
    }

    @Test
    void registrarPago_cuandoDatosValidos_creaPayoYActualizaCuenta() {
        PagoProveedor pagoGuardado = new PagoProveedor();
        pagoGuardado.setIdPagoProv(1L);
        pagoGuardado.setCuenta(cuenta);
        pagoGuardado.setFechaPago(LocalDate.now());
        pagoGuardado.setMontoPagado(new BigDecimal("100000.00"));
        pagoGuardado.setMetodoPago("TRANSFERENCIA");
        pagoGuardado.setComprobanteRef("TRF-001");

        when(cuentaPorPagarRepository.findById(1L)).thenReturn(Optional.of(cuenta));
        when(pagoProveedorRepository.save(any(PagoProveedor.class))).thenReturn(pagoGuardado);

        PagoProveedorResponseDTO resultado = pagoProveedorService.registrarPago(requestDTO);

        assertEquals("PAGADO", cuenta.getEstado());
        assertEquals("TRANSFERENCIA", resultado.getMetodoPago());
        verify(cuentaPorPagarRepository).save(cuenta);
        verify(pagoProveedorRepository).save(any(PagoProveedor.class));
    }

    @Test
    void registrarPago_cuandoCuentaYaPagada_lanzaExcepcion() {
        cuenta.setEstado("PAGADO");
        when(cuentaPorPagarRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoProveedorService.registrarPago(requestDTO));

        assertTrue(ex.getMessage().contains("PAGADO"));
        verify(pagoProveedorRepository, never()).save(any());
    }

    @Test
    void registrarPago_cuandoCuentaAnulada_lanzaExcepcion() {
        cuenta.setEstado("ANULADO");
        when(cuentaPorPagarRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoProveedorService.registrarPago(requestDTO));

        assertTrue(ex.getMessage().contains("ANULADO"));
        verify(pagoProveedorRepository, never()).save(any());
    }

    @Test
    void registrarPago_cuandoMontoNoCoinciде_lanzaExcepcion() {
        requestDTO.setMontoPagado(new BigDecimal("50000.00"));
        when(cuentaPorPagarRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoProveedorService.registrarPago(requestDTO));

        assertTrue(ex.getMessage().contains("50000.00"));
        verify(pagoProveedorRepository, never()).save(any());
    }

    @Test
    void registrarPago_cuandoCuentaNoExiste_lanzaExcepcion() {
        when(cuentaPorPagarRepository.findById(99L)).thenReturn(Optional.empty());
        requestDTO.setIdCuenta(99L);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> pagoProveedorService.registrarPago(requestDTO));

        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void listarPorCuenta_cuandoCuentaExiste_retornaLista() {
        PagoProveedor pago = new PagoProveedor();
        pago.setIdPagoProv(1L);
        pago.setCuenta(cuenta);
        pago.setFechaPago(LocalDate.now());
        pago.setMontoPagado(new BigDecimal("100000.00"));
        pago.setMetodoPago("TRANSFERENCIA");

        when(cuentaPorPagarRepository.existsById(1L)).thenReturn(true);
        when(pagoProveedorRepository.findByCuentaIdCuenta(1L)).thenReturn(List.of(pago));

        List<PagoProveedorResponseDTO> resultado = pagoProveedorService.listarPorCuenta(1L);

        assertEquals(1, resultado.size());
        assertEquals(new BigDecimal("100000.00"), resultado.get(0).getMontoPagado());
    }
}