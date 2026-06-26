package com.goldenburgers.gestionUsuario.controller;

import com.goldenburgers.gestionUsuario.DTOs.RolDTO;
import com.goldenburgers.gestionUsuario.service.RolService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para la gestión de Roles
 * (Solo lectura - los roles son datos fijos)
 */
@RestController
@RequestMapping("/api/roles")
@Tag(name = "Roles", description = "API de consulta de roles del sistema (solo lectura)")
public class RolController {

    private static final Logger logger = LoggerFactory.getLogger(RolController.class);

    @Autowired
    private RolService rolService;

    /**
     * GET /api/roles - Listar todos los roles
     */
    @Operation(
            summary = "Listar todos los roles",
            description = "Obtiene todos los roles disponibles en el sistema (CLIENTE, TRABAJADOR, ADMIN). Este endpoint es público."
    )
    @ApiResponse(responseCode = "200", description = "Lista de roles obtenida exitosamente")
    @GetMapping
    public ResponseEntity<List<RolDTO>> getAllRoles() {
        logger.info("GET /api/roles - Listando todos los roles");
        List<RolDTO> roles = rolService.getAllRoles();
        return ResponseEntity.ok(roles);
    }

    /**
     * GET /api/roles/{id} - Obtener rol por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<RolDTO> getRolById(@PathVariable Long id) {
        logger.info("GET /api/roles/{} - Obteniendo rol por ID", id);
        RolDTO rol = rolService.getRolById(id);
        return ResponseEntity.ok(rol);
    }

    /**
     * GET /api/roles/nombre/{nombre} - Obtener rol por nombre
     */
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<RolDTO> getRolByNombre(@PathVariable String nombre) {
        logger.info("GET /api/roles/nombre/{} - Obteniendo rol por nombre", nombre);
        RolDTO rol = rolService.getRolByNombre(nombre);
        return ResponseEntity.ok(rol);
    }
}
