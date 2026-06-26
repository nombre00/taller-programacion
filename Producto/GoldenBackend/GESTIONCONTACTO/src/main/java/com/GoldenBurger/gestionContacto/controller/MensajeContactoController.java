package com.GoldenBurger.gestionContacto.controller;

import com.GoldenBurger.gestionContacto.model.MensajeContacto;
import com.GoldenBurger.gestionContacto.service.MensajeContactoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/mensajes")
@Tag(name = "Gestión de Contacto", description = "Operaciones del microservicio de mensajes de contacto")
public class MensajeContactoController {

    private final MensajeContactoService service;

    public MensajeContactoController(MensajeContactoService service) {
        this.service = service;
    }

    // ================================================================
    // GET: LISTAR TODOS LOS MENSAJES (ADMIN)
    // ================================================================
    @Operation(
            summary = "Listar todos los mensajes",
            description = "Retorna todos los mensajes enviados desde el formulario público. *Requiere rol ADMIN*.",
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
                    @ApiResponse(responseCode = "403", description = "No autorizado (requiere ADMIN)")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping
    public List<MensajeContacto> listarMensajes() {
        return service.listarMensajes();
    }

    // ================================================================
    // GET: OBTENER MENSAJE POR ID (ADMIN)
    // ================================================================
    @Operation(
            summary = "Obtener mensaje por ID",
            description = "Retorna un mensaje en base a su identificador. *Requiere rol ADMIN*."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/{id}")
    public Optional<MensajeContacto> obtenerMensaje(
            @Parameter(description = "ID del mensaje", example = "10")
            @PathVariable Long id
    ) {
        return service.obtenerPorId(id);
    }

    // ================================================================
    // POST: CREAR MENSAJE (PÚBLICO)
    // ================================================================
    @Operation(
            summary = "Enviar mensaje de contacto",
            description = "Guarda un nuevo mensaje enviado desde el formulario público. No requiere autenticación.",
            requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Datos enviados desde la página de contacto",
                    required = true,
                    content = @Content(
                            mediaType = "application/json",
                            schema = @Schema(implementation = MensajeContacto.class)
                    )
            ),
            responses = {
                    @ApiResponse(responseCode = "200", description = "Mensaje guardado correctamente")
            }
    )
    @PostMapping
    public MensajeContacto crearMensaje(@RequestBody MensajeContacto mensaje) {
        return service.guardar(mensaje);
    }

    // ================================================================
    // GET: LISTAR POR ESTADO (ADMIN)
    // ================================================================
    @Operation(
            summary = "Listar mensajes por estado",
            description = """
                Retorna mensajes filtrados por estado:
                • 0 = No leído
                • 1 = Leído
                • 2 = Respondido
                *Requiere rol ADMIN*.
                """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Listado filtrado correctamente"),
                    @ApiResponse(responseCode = "403", description = "No autorizado - requiere rol ADMIN")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/estado/{estado}")
    public List<MensajeContacto> listarPorEstado(
            @Parameter(
                    description = "Estado del mensaje (0=No leído, 1=Leído, 2=Respondido)",
                    example = "1"
            )
            @PathVariable Integer estado
    ) {
        return service.listarPorEstado(estado);
    }

    // ================================================================
    // PUT: ACTUALIZAR MENSAJE COMPLETO
    // ================================================================
    @Operation(
            summary = "Actualizar mensaje completo",
            description = "Permite modificar un mensaje existente. *Requiere rol ADMIN*."
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}")
    public MensajeContacto actualizarMensaje(
            @PathVariable Long id,
            @RequestBody MensajeContacto mensaje
    ) {
        mensaje.setIdMensaje(id);
        return service.guardar(mensaje);
    }

    // ================================================================
    // PUT: ACTUALIZAR SOLO ESTADO (ADMIN)
    // ================================================================
    @Operation(
            summary = "Actualizar estado del mensaje",
            description = """
                Actualiza solo el estado del mensaje:
                • 0 = No leído
                • 1 = Leído
                • 2 = Respondido
                *Requiere rol ADMIN*.
                """,
            responses = {
                    @ApiResponse(responseCode = "200", description = "Estado actualizado correctamente"),
                    @ApiResponse(responseCode = "404", description = "Mensaje no encontrado")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/estado/{estado}")
    public MensajeContacto actualizarEstado(
            @Parameter(description = "ID del mensaje", example = "5")
            @PathVariable Long id,

            @Parameter(description = "Nuevo estado (0, 1 o 2)", example = "1")
            @PathVariable Integer estado
    ) {
        return service.actualizarEstado(id, estado);
    }

    // ================================================================
    // DELETE: ELIMINAR MENSAJE
    // ================================================================
    @Operation(
            summary = "Eliminar mensaje",
            description = "Elimina un mensaje por su ID. *Requiere rol ADMIN*.",
            responses = {
                    @ApiResponse(responseCode = "204", description = "Mensaje eliminado"),
                    @ApiResponse(responseCode = "403", description = "No autorizado")
            }
    )
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/{id}")
    public void eliminarMensaje(
            @Parameter(description = "ID del mensaje a eliminar", example = "3")
            @PathVariable Long id
    ) {
        service.eliminar(id);
    }
}
