package com.goldenburgers.gestionUsuario.service;

import com.goldenburgers.gestionUsuario.DTOs.*;
import com.goldenburgers.gestionUsuario.model.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper para convertir entre entidades y DTOs 
 * que sirve como comnunicación entre capas de servicio y controladores.
 */
@Component
public class EntityMapper {
    
    // ==================== ROL ====================
    public RolDTO toDTO(Rol rol) {
        if (rol == null) return null;
        return new RolDTO(
                rol.getIdRol(),
                rol.getNombreRol()
        );
    }

    // ==================== CIUDAD ====================
    public CiudadDTO toDTO(Ciudad ciudad) {
        if (ciudad == null) return null;
        return new CiudadDTO(
                ciudad.getIdCiudad(),
                ciudad.getNombreCiudad()
        );
    }

    // ==================== USUARIO ====================
    public UsuarioDTO toDTO(Usuario usuario) {
        if (usuario == null) return null;
        return new UsuarioDTO(
                usuario.getIdUsuario(),
                toDTO(usuario.getRol()),
                usuario.getEmail(),
                usuario.getFechaCreacion()
        );
    }

    // ==================== DIRECCION CLIENTE ====================
    public DireccionClienteDTO toDTO(DireccionCliente direccion) {
        if (direccion == null) return null;
        return new DireccionClienteDTO(
                direccion.getIdDireccion(),
                direccion.getCliente() != null ? direccion.getCliente().getIdCliente() : null,
                toDTO(direccion.getCiudad()),
                direccion.getDireccion(),
                direccion.getAlias()
        );
    }

    public List<DireccionClienteDTO> toDireccionDTOList(List<DireccionCliente> direcciones) {
        if (direcciones == null) return null;
        return direcciones.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== CLIENTE ====================
    public ClienteDTO toDTO(Cliente cliente) {
        if (cliente == null) return null;
        return new ClienteDTO(
                cliente.getIdCliente(),
                toDTO(cliente.getUsuario()),
                cliente.getNombreCliente(),
                cliente.getTelefonoCliente(),
                toDireccionDTOList(cliente.getDirecciones())
        );
    }

    public List<ClienteDTO> toClienteDTOList(List<Cliente> clientes) {
        if (clientes == null) return null;
        return clientes.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // ==================== TRABAJADOR ====================
    public TrabajadorDTO toDTO(Trabajador trabajador) {
        if (trabajador == null) return null;
        return new TrabajadorDTO(
                trabajador.getIdTrabajador(),
                toDTO(trabajador.getUsuario()),
                trabajador.getNombreTrabajador(),
                trabajador.getRutTrabajador()
        );
    }

    public List<TrabajadorDTO> toTrabajadorDTOList(List<Trabajador> trabajadores) {
        if (trabajadores == null) return null;
        return trabajadores.stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }
}
