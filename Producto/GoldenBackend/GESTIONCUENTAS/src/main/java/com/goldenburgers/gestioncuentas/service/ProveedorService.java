package com.goldenburgers.gestioncuentas.service;

import com.goldenburgers.gestioncuentas.dto.ProveedorRequestDTO;
import com.goldenburgers.gestioncuentas.dto.ProveedorResponseDTO;
import com.goldenburgers.gestioncuentas.model.Proveedor;
import com.goldenburgers.gestioncuentas.repository.ProveedorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProveedorService {

    private final ProveedorRepository proveedorRepository;

    public List<ProveedorResponseDTO> listarTodos() {
        return proveedorRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    public ProveedorResponseDTO obtenerPorId(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
        return toResponse(proveedor);
    }

    public ProveedorResponseDTO crear(ProveedorRequestDTO dto) {
        if (proveedorRepository.existsByRut(dto.getRut())) {
            throw new RuntimeException("Ya existe un proveedor con el RUT: " + dto.getRut());
        }
        Proveedor proveedor = new Proveedor();
        proveedor.setNombre(dto.getNombre());
        proveedor.setRut(dto.getRut());
        proveedor.setEmail(dto.getEmail());
        proveedor.setTelefono(dto.getTelefono());
        proveedor.setActivo(true);
        return toResponse(proveedorRepository.save(proveedor));
    }

    public ProveedorResponseDTO actualizar(Long id, ProveedorRequestDTO dto) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));

        // Si el RUT cambió, verificar que no esté en uso por otro proveedor
        if (!proveedor.getRut().equals(dto.getRut()) && proveedorRepository.existsByRut(dto.getRut())) {
            throw new RuntimeException("Ya existe un proveedor con el RUT: " + dto.getRut());
        }

        proveedor.setNombre(dto.getNombre());
        proveedor.setRut(dto.getRut());
        proveedor.setEmail(dto.getEmail());
        proveedor.setTelefono(dto.getTelefono());
        return toResponse(proveedorRepository.save(proveedor));
    }

    public void desactivar(Long id) {
        Proveedor proveedor = proveedorRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Proveedor no encontrado con id: " + id));
        proveedor.setActivo(false);
        proveedorRepository.save(proveedor);
    }

    private ProveedorResponseDTO toResponse(Proveedor proveedor) {
        ProveedorResponseDTO dto = new ProveedorResponseDTO();
        dto.setIdProveedor(proveedor.getIdProveedor());
        dto.setNombre(proveedor.getNombre());
        dto.setRut(proveedor.getRut());
        dto.setEmail(proveedor.getEmail());
        dto.setTelefono(proveedor.getTelefono());
        dto.setActivo(proveedor.getActivo());
        return dto;
    }
}