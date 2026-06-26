package com.example.gestionCuentas;


import com.goldenburgers.gestioncuentas.dto.ProveedorRequestDTO;
import com.goldenburgers.gestioncuentas.dto.ProveedorResponseDTO;
import com.goldenburgers.gestioncuentas.model.Proveedor;
import com.goldenburgers.gestioncuentas.repository.ProveedorRepository;
import com.goldenburgers.gestioncuentas.service.ProveedorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProveedorServiceTest {

    @Mock
    private ProveedorRepository proveedorRepository;

    @InjectMocks
    private ProveedorService proveedorService;

    private Proveedor proveedor;
    private ProveedorRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        proveedor = new Proveedor();
        proveedor.setIdProveedor(1L);
        proveedor.setNombre("Distribuidora Sur");
        proveedor.setRut("76543210-9");
        proveedor.setEmail("contacto@sur.cl");
        proveedor.setTelefono("+56912345678");
        proveedor.setActivo(true);

        requestDTO = new ProveedorRequestDTO();
        requestDTO.setNombre("Distribuidora Sur");
        requestDTO.setRut("76543210-9");
        requestDTO.setEmail("contacto@sur.cl");
        requestDTO.setTelefono("+56912345678");
    }

    @Test
    void listarTodos_retornaListaDeProveedores() {
        when(proveedorRepository.findAll()).thenReturn(List.of(proveedor));

        List<ProveedorResponseDTO> resultado = proveedorService.listarTodos();

        assertEquals(1, resultado.size());
        assertEquals("Distribuidora Sur", resultado.get(0).getNombre());
        verify(proveedorRepository).findAll();
    }

    @Test
    void obtenerPorId_cuandoExiste_retornaProveedor() {
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));

        ProveedorResponseDTO resultado = proveedorService.obtenerPorId(1L);

        assertEquals(1L, resultado.getIdProveedor());
        assertEquals("76543210-9", resultado.getRut());
    }

    @Test
    void obtenerPorId_cuandoNoExiste_lanzaExcepcion() {
        when(proveedorRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> proveedorService.obtenerPorId(99L));

        assertTrue(ex.getMessage().contains("99"));
    }

    @Test
    void crear_cuandoRutNuevo_creaProveedorCorrectamente() {
        when(proveedorRepository.existsByRut("76543210-9")).thenReturn(false);
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedor);

        ProveedorResponseDTO resultado = proveedorService.crear(requestDTO);

        assertEquals("Distribuidora Sur", resultado.getNombre());
        assertTrue(resultado.getActivo());
        verify(proveedorRepository).save(any(Proveedor.class));
    }

    @Test
    void crear_cuandoRutDuplicado_lanzaExcepcion() {
        when(proveedorRepository.existsByRut("76543210-9")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> proveedorService.crear(requestDTO));

        assertTrue(ex.getMessage().contains("76543210-9"));
        verify(proveedorRepository, never()).save(any());
    }

    @Test
    void desactivar_cuandoExiste_poneActivoEnFalse() {
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.save(any(Proveedor.class))).thenReturn(proveedor);

        proveedorService.desactivar(1L);

        assertFalse(proveedor.getActivo());
        verify(proveedorRepository).save(proveedor);
    }

    @Test
    void actualizar_cuandoRutCambiaAUnoExistente_lanzaExcepcion() {
        requestDTO.setRut("11111111-1");
        when(proveedorRepository.findById(1L)).thenReturn(Optional.of(proveedor));
        when(proveedorRepository.existsByRut("11111111-1")).thenReturn(true);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> proveedorService.actualizar(1L, requestDTO));

        assertTrue(ex.getMessage().contains("11111111-1"));
        verify(proveedorRepository, never()).save(any());
    }
}