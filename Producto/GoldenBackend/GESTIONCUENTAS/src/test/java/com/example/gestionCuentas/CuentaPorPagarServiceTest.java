package com.example.gestionCuentas;

import com.goldenburgers.gestioncuentas.dto.CuentaPorPagarResponseDTO;
import com.goldenburgers.gestioncuentas.model.CuentaPorPagar;
import com.goldenburgers.gestioncuentas.model.Proveedor;
import com.goldenburgers.gestioncuentas.repository.CuentaPorPagarRepository;
import com.goldenburgers.gestioncuentas.repository.ProveedorRepository;
import com.goldenburgers.gestioncuentas.service.CuentaPorPagarService;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuentaPorPagarServiceTest {

    @Mock
    private CuentaPorPagarRepository cuentaPorPagarRepository;

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private CuentaPorPagarService cuentaPorPagarService;

    private CuentaPorPagar cuenta;
    private Proveedor proveedor;

    @BeforeEach
    void setUp() {
        proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Distribuidora Sur");

        cuenta = new CuentaPorPagar();
        cuenta.setIdCuenta(1L);
        cuenta.setProveedor(proveedor);
        cuenta.setTipoGasto("MERCADERIA");
        cuenta.setDescripcion("Factura insumos enero");
        cuenta.setMontoTotal(new BigDecimal("100000.00"));
        cuenta.setIvaCredito(BigDecimal.ZERO);
        cuenta.setFechaEmision(LocalDate.of(2026, 1, 1));
        cuenta.setFechaVencimiento(LocalDate.of(2026, 1, 31));
        cuenta.setEstado("PENDIENTE");
        cuenta.setNumeroDocumento("FAC-001");
    }

    @Test
    void listarTodas_sinFiltroEstado_retornaTodas() {
        when(cuentaPorPagarRepository.findAll()).thenReturn(List.of(cuenta));

        List<CuentaPorPagarResponseDTO> resultado = cuentaPorPagarService.listarTodas(null);

        assertEquals(1, resultado.size());
        assertEquals("PENDIENTE", resultado.get(0).getEstado());
        verify(cuentaPorPagarRepository).findAll();
        verify(cuentaPorPagarRepository, never()).findByEstado(any());
    }

    @Test
    void listarTodas_conFiltroEstado_filtraPorEstado() {
        when(cuentaPorPagarRepository.findByEstado("PENDIENTE")).thenReturn(List.of(cuenta));

        List<CuentaPorPagarResponseDTO> resultado = cuentaPorPagarService.listarTodas("PENDIENTE");

        assertEquals(1, resultado.size());
        verify(cuentaPorPagarRepository).findByEstado("PENDIENTE");
        verify(cuentaPorPagarRepository, never()).findAll();
    }

    @Test
    void obtenerPorId_cuandoExiste_retornaCuenta() {
        when(cuentaPorPagarRepository.findById(1L)).thenReturn(Optional.of(cuenta));

        CuentaPorPagarResponseDTO resultado = cuentaPorPagarService.obtenerPorId(1L);

        assertEquals(1L, resultado.getIdCuenta());
        assertEquals("Distribuidora Sur", resultado.getNombreProveedor());
        assertEquals("FAC-001", resultado.getNumeroDocumento());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaExcepcion() {
        when(cuentaPorPagarRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cuentaPorPagarService.obtenerPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void listarPorProveedor_cuandoProveedorExiste_retornaLista() {
        when(proveedorRepository.existsById(1L)).thenReturn(true);
        when(cuentaPorPagarRepository.findByProveedor_IdProveedor(1L)).thenReturn(List.of(cuenta));

        List<CuentaPorPagarResponseDTO> resultado = cuentaPorPagarService.listarPorProveedor(1L);

        assertEquals(1, resultado.size());
        assertEquals(1L, resultado.get(0).getIdProveedor());
    }

    @Test
    void listarPorProveedor_cuandoProveedorNoExiste_lanzaExcepcion() {
        when(proveedorRepository.existsById(99L)).thenReturn(false);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> cuentaPorPagarService.listarPorProveedor(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void buscarConFiltros_conRangoFechas_retornaCuentasFiltradas() {
        LocalDate desde = LocalDate.of(2026, 1, 1);
        LocalDate hasta = LocalDate.of(2026, 1, 31);

        when(cuentaPorPagarRepository.buscarConFiltros(
                null, desde, hasta, null, null))
                .thenReturn(List.of(cuenta));

        List<CuentaPorPagarResponseDTO> resultado = cuentaPorPagarService.buscarConFiltros(
                null, desde, hasta, null, null);

        assertEquals(1, resultado.size());
        assertEquals(LocalDate.of(2026, 1, 1), resultado.get(0).getFechaEmision());
    }

    @Test
    void buscarConFiltros_conTodosLosFiltros_llamaRepositorioCorrectamente() {
        LocalDate emisionDesde = LocalDate.of(2026, 1, 1);
        LocalDate emisionHasta = LocalDate.of(2026, 1, 31);
        LocalDate vencDesde = LocalDate.of(2026, 1, 15);
        LocalDate vencHasta = LocalDate.of(2026, 2, 15);

        when(cuentaPorPagarRepository.buscarConFiltros(
                "PENDIENTE", emisionDesde, emisionHasta, vencDesde, vencHasta))
                .thenReturn(List.of(cuenta));

        List<CuentaPorPagarResponseDTO> resultado = cuentaPorPagarService.buscarConFiltros(
                "PENDIENTE", emisionDesde, emisionHasta, vencDesde, vencHasta);

        assertEquals(1, resultado.size());
        verify(cuentaPorPagarRepository).buscarConFiltros(
                "PENDIENTE", emisionDesde, emisionHasta, vencDesde, vencHasta);
    }
}