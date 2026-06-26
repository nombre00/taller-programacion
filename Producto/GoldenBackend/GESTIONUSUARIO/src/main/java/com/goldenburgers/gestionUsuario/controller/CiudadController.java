package com.goldenburgers.gestionUsuario.controller;

import com.goldenburgers.gestionUsuario.DTOs.CiudadDTO;
import com.goldenburgers.gestionUsuario.service.CiudadService;
//import io.swagger.v3.oas.annotations.Operation;
//import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para la gestión de Ciudades
 * (Solo lectura - las ciudades son
 * datos fijos)
 */
@RestController
@RequestMapping("/api/ciudades")
@Tag(name = "Ciudades", description = "API de consulta de ciudades disponibles (solo lectura)")
public class CiudadController {

    private static final Logger logger = LoggerFactory.getLogger(CiudadController.class);

    @Autowired
    private CiudadService ciudadService;

    /**
     * GET /api/ciudades - Listar todas las ciudades
     */
    @GetMapping
    public ResponseEntity<List<CiudadDTO>> getAllCiudades() {
        logger.info("GET /api/ciudades - Listando todas las ciudades");
        List<CiudadDTO> ciudades = ciudadService.getAllCiudades();
        return ResponseEntity.ok(ciudades);
    }

    /**
     * GET /api/ciudades/{id} - Obtener ciudad por ID
     */
    @GetMapping("/{id}")
    public ResponseEntity<CiudadDTO> getCiudadById(@PathVariable Long id) {
        logger.info("GET /api/ciudades/{} - Obteniendo ciudad por ID", id);
        CiudadDTO ciudad = ciudadService.getCiudadById(id);
        return ResponseEntity.ok(ciudad);
    }

    /**
     * GET /api/ciudades/nombre/{nombre} - Obtener ciudad por nombre
     */
    @GetMapping("/nombre/{nombre}")
    public ResponseEntity<CiudadDTO> getCiudadByNombre(@PathVariable String nombre) {
        logger.info("GET /api/ciudades/nombre/{} - Obteniendo ciudad por nombre", nombre);
        CiudadDTO ciudad = ciudadService.getCiudadByNombre(nombre);
        return ResponseEntity.ok(ciudad);
    }
}
