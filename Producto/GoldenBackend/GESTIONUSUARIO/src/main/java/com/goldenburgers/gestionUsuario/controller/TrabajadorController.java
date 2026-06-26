package com.goldenburgers.gestionUsuario.controller;

import com.goldenburgers.gestionUsuario.DTOs.ActualizarEmailRequest;
import com.goldenburgers.gestionUsuario.DTOs.ActualizarRolRequest;
import com.goldenburgers.gestionUsuario.DTOs.RegistrarTrabajador;
import com.goldenburgers.gestionUsuario.DTOs.TrabajadorDTO;
import com.goldenburgers.gestionUsuario.service.TrabajadorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controller REST para la gestión de Trabajadores
 *
 * Este controlador maneja las operaciones CRUD para trabajadores del sistema.
 * Los trabajadores tienen acceso privilegiado y pueden gestionar pedidos y clientes.
 *
 * Se integra con:
 * - TrabajadorService: para la lógica de negocio
 * - Firebase Authentication: para validar tokens JWT
 * - Spring Security: para control de acceso basado en roles
 *
 * Niveles de seguridad por operación:
 * - CREAR trabajador: Solo ADMIN (@PreAuthorize("hasRole('ADMIN')"))
 * - LEER trabajadores: ADMIN y TRABAJADOR (@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')"))
 * - ACTUALIZAR trabajador: Solo ADMIN
 * - ELIMINAR trabajador: Solo ADMIN
 *
 * Ruta base: /api/trabajadores
 */
@RestController
@RequestMapping("/api/trabajadores")
@Tag(name = "Trabajadores", description = "API para gestión de trabajadores del sistema")
@SecurityRequirement(name = "bearer-jwt") // Requiere token JWT en todas las peticiones
public class TrabajadorController {

    private static final Logger logger = LoggerFactory.getLogger(TrabajadorController.class);

    // Inyección del servicio que contiene la lógica de negocio para trabajadores
    @Autowired
    private TrabajadorService trabajadorService; 

    /**
     * POST /api/trabajadores - Registrar un nuevo trabajador en el sistema
     *
     * Endpoint para crear un trabajador. SOLO los ADMINISTRADORES pueden crear trabajadores.
     *
     * Flujo:
     * 1. Valida que el usuario tenga rol ADMIN
     * 2. Valida el request con @Valid (campos obligatorios y formatos)
     * 3. Verifica que idUsuario, email y RUT no existan en la BD
     * 4. Busca el rol "Trabajador" en la BD
     * 5. Crea el registro en tabla USUARIOS
     * 6. Crea el registro en tabla TRABAJADORES
     * 7. Retorna el TrabajadorDTO con los datos creados
     *
     * Validaciones en RegisterTrabajadorRequest:
     * - idUsuario: obligatorio (Firebase UID)
     * - email: obligatorio y formato válido
     * - nombreTrabajador: obligatorio
     * - rutTrabajador: obligatorio, formato XX.XXX.XXX-X (ej: 12.345.678-9)
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasRole('ADMIN')") - SOLO ADMINISTRADORES
     * - Los trabajadores NO pueden crear otros trabajadores
     * - Los clientes NO tienen acceso
     *
     * @param request DTO con los datos del trabajador a registrar
     * @return ResponseEntity con TrabajadorDTO (201 Created)
     * @throws IllegalArgumentException si el usuario/email/RUT ya existe o el rol no se encuentra (400)
     * @throws AccessDeniedException si el usuario no es ADMIN (403)
     */
    @Operation(
            summary = "Registrar un nuevo trabajador",
            description = "Crea un nuevo trabajador en el sistema. Requiere rol ADMIN."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Trabajador creado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN"),
            @ApiResponse(responseCode = "409", description = "El trabajador ya existe")
    })
    @PreAuthorize("hasRole('ADMIN')") // RESTRICCIÓN MÁXIMA: Solo ADMIN puede crear trabajadores
    @PostMapping
    public ResponseEntity<TrabajadorDTO> registerTrabajador(@Valid @RequestBody RegistrarTrabajador request) {
        logger.info("POST /api/trabajadores - Registrando nuevo trabajador");
        // Delega la lógica de negocio al servicio
        TrabajadorDTO trabajador = trabajadorService.registerTrabajador(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(trabajador);
    }

    /**
     * GET /api/trabajadores - Listar todos los trabajadores del sistema
     *
     * Obtiene la lista completa de trabajadores registrados.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") - ADMIN y TRABAJADOR pueden acceder
     * - Los clientes NO pueden ver la lista de trabajadores
     * - Los trabajadores pueden ver la lista para coordinarse entre ellos
     *
     * Uso típico:
     * - Panel de administración para gestionar trabajadores
     * - Asignación de pedidos a trabajadores
     *
     * @return ResponseEntity con List<TrabajadorDTO> (200 OK)
     * @throws AccessDeniedException si el usuario no tiene rol ADMIN o TRABAJADOR (403)
     */
    @Operation(
            summary = "Listar todos los trabajadores",
            description = "Obtiene una lista de todos los trabajadores registrados. Requiere rol ADMIN o TRABAJADOR."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Lista obtenida exitosamente"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN o TRABAJADOR")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") // RESTRICCIÓN: Solo ADMIN y TRABAJADOR
    @GetMapping
    public ResponseEntity<List<TrabajadorDTO>> getAllTrabajadores() {
        logger.info("GET /api/trabajadores - Listando todos los trabajadores");
        List<TrabajadorDTO> trabajadores = trabajadorService.getAllTrabajadores();
        return ResponseEntity.ok(trabajadores);
    }

    /**
     * GET /api/trabajadores/{id} - Obtener trabajador por ID
     *
     * Busca un trabajador específico por su ID de base de datos.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") - ADMIN y TRABAJADOR pueden acceder
     *
     * @param id ID del trabajador (clave primaria en BD)
     * @return ResponseEntity con TrabajadorDTO (200 OK)
     * @throws IllegalArgumentException si el trabajador no existe (400)
     * @throws AccessDeniedException si el usuario no tiene permisos (403)
     */
    @Operation(
            summary = "Obtener trabajador por ID",
            description = "Busca un trabajador específico por su ID de base de datos. Requiere rol ADMIN o TRABAJADOR."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trabajador encontrado",
                    content = @Content(schema = @Schema(implementation = TrabajadorDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Trabajador no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") // RESTRICCIÓN: Solo ADMIN y TRABAJADOR
    @GetMapping("/{id}")
    public ResponseEntity<TrabajadorDTO> getTrabajadorById(
            @Parameter(description = "ID del trabajador", required = true)
            @PathVariable Long id) {
        logger.info("GET /api/trabajadores/{} - Obteniendo trabajador por ID", id);
        TrabajadorDTO trabajador = trabajadorService.getTrabajadorById(id);
        return ResponseEntity.ok(trabajador);
    }

    /**
     * GET /api/trabajadores/usuario/{idUsuario} - Obtener trabajador por Firebase UID
     *
     * Busca un trabajador por su ID de usuario de Firebase Authentication.
     * Útil para obtener los datos del trabajador autenticado actualmente.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") - ADMIN y TRABAJADOR pueden acceder
     *
     * Uso típico:
     * - Al iniciar sesión, obtener los datos del trabajador con su Firebase UID
     * - Verificar información del usuario autenticado
     *
     * @param idUsuario Firebase UID del usuario
     * @return ResponseEntity con TrabajadorDTO (200 OK)
     * @throws IllegalArgumentException si el trabajador no existe (400)
     */
    @Operation(
            summary = "Obtener trabajador por Firebase UID",
            description = "Busca un trabajador por su ID de Firebase Authentication. Útil para obtener datos del trabajador autenticado."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trabajador encontrado",
                    content = @Content(schema = @Schema(implementation = TrabajadorDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Trabajador no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") // RESTRICCIÓN: Solo ADMIN y TRABAJADOR
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<TrabajadorDTO> getTrabajadorByUsuarioId(
            @Parameter(description = "Firebase UID del usuario", required = true)
            @PathVariable String idUsuario) {
        logger.info("GET /api/trabajadores/usuario/{} - Obteniendo trabajador por usuario", idUsuario);
        TrabajadorDTO trabajador = trabajadorService.getTrabajadorByUsuarioId(idUsuario);
        return ResponseEntity.ok(trabajador);
    }

    /**
     * GET /api/trabajadores/rut/{rut} - Obtener trabajador por RUT
     *
     * Busca un trabajador por su RUT chileno.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") - ADMIN y TRABAJADOR pueden acceder
     *
     * Formato de RUT esperado: XX.XXX.XXX-X (ej: 12.345.678-9)
     *
     * @param rut RUT del trabajador (con puntos y guión)
     * @return ResponseEntity con TrabajadorDTO (200 OK)
     * @throws IllegalArgumentException si el trabajador no existe (400)
     */
    @Operation(
            summary = "Obtener trabajador por RUT",
            description = "Busca un trabajador por su RUT chileno. Formato esperado: XX.XXX.XXX-X"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trabajador encontrado",
                    content = @Content(schema = @Schema(implementation = TrabajadorDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Trabajador no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") // RESTRICCIÓN: Solo ADMIN y TRABAJADOR
    @GetMapping("/rut/{rut}")
    public ResponseEntity<TrabajadorDTO> getTrabajadorByRut(
            @Parameter(description = "RUT del trabajador (formato: XX.XXX.XXX-X)", required = true, example = "12.345.678-9")
            @PathVariable String rut) {
        logger.info("GET /api/trabajadores/rut/{} - Obteniendo trabajador por RUT", rut);
        TrabajadorDTO trabajador = trabajadorService.getTrabajadorByRut(rut);
        return ResponseEntity.ok(trabajador);
    }

    /**
     * PUT /api/trabajadores/{id} - Actualizar datos de un trabajador
     *
     * Permite modificar el nombre y RUT de un trabajador existente.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasRole('ADMIN')") - SOLO ADMINISTRADORES
     * - Los trabajadores NO pueden modificar sus propios datos ni los de otros
     *
     * Parámetros:
     * - id: ID del trabajador a actualizar (obligatorio, en la URL)
     * - nombreTrabajador: Nuevo nombre (obligatorio, @RequestParam)
     * - rutTrabajador: Nuevo RUT (obligatorio, @RequestParam, formato XX.XXX.XXX-X)
     *
     * Validaciones:
     * - Si se cambia el RUT, verifica que no esté en uso por otro trabajador
     * - Si es el mismo RUT, no valida duplicados
     *
     * Flujo:
     * 1. Busca el trabajador por ID
     * 2. Valida que el nuevo RUT no esté en uso (si cambió)
     * 3. Actualiza nombre y RUT
     * 4. Guarda cambios en la BD
     * 5. Retorna el TrabajadorDTO actualizado
     *
     * @param id ID del trabajador a actualizar
     * @param nombreTrabajador Nuevo nombre
     * @param rutTrabajador Nuevo RUT (formato XX.XXX.XXX-X)
     * @return ResponseEntity con TrabajadorDTO actualizado (200 OK)
     * @throws IllegalArgumentException si el trabajador no existe o el RUT está duplicado (400)
     * @throws AccessDeniedException si el usuario no es ADMIN (403)
     */
    @Operation(
            summary = "Actualizar datos de un trabajador",
            description = "Permite a Admin modificar nombre y RUT de un trabajador. Solo Admin."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Trabajador actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = TrabajadorDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos o RUT duplicado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Trabajador no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')") // RESTRICCIÓN MÁXIMA: Solo ADMIN puede actualizar
    @PutMapping("/{id}")
    public ResponseEntity<TrabajadorDTO> updateTrabajador(
            @Parameter(description = "ID del trabajador", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo nombre del trabajador", required = true)
            @RequestParam String nombreTrabajador,
            @Parameter(description = "Nuevo RUT del trabajador (formato: XX.XXX.XXX-X)", required = true, example = "12.345.678-9")
            @RequestParam String rutTrabajador) {
        logger.info("PUT /api/trabajadores/{} - Actualizando trabajador", id);
        TrabajadorDTO trabajador = trabajadorService.updateTrabajador(id, nombreTrabajador, rutTrabajador);
        return ResponseEntity.ok(trabajador);
    }

    /**
     * PUT /api/trabajadores/{id}/email - Actualizar email de un trabajador (Solo Admin)
     *
     * Permite a ADMIN modificar el email de un trabajador.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasRole('ADMIN')") - SOLO ADMINISTRADORES
     *
     * Casos de uso:
     * - Trabajador olvidó contraseña de email anterior
     * - Corrección administrativa de emails incorrectos
     * - Actualización de emails corporativos
     *
     * IMPORTANTE:
     * - Este endpoint actualiza el email en la base de datos local
     * - NO actualiza el email en Firebase Authentication
     *
     * @param id ID del trabajador
     * @param request DTO con el nuevo email
     * @return ResponseEntity con TrabajadorDTO actualizado (200 OK)
     */
    @Operation(
            summary = "Actualizar email de un trabajador",
            description = "Permite a Admin modificar el email de un trabajador. Solo Admin."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = TrabajadorDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Email inválido o ya en uso"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Trabajador no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/email")
    public ResponseEntity<TrabajadorDTO> actualizarEmailTrabajador(
            @Parameter(description = "ID del trabajador", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEmailRequest request) {
        logger.info("PUT /api/trabajadores/{}/email - Actualizando email", id);
        TrabajadorDTO trabajador = trabajadorService.actualizarEmailTrabajador(id, request);
        return ResponseEntity.ok(trabajador);
    }

    /**
     * PUT /api/trabajadores/{id}/rol - Cambiar rol de un trabajador (Solo Admin)
     *
     * Permite a ADMIN cambiar el rol de un trabajador entre Trabajador y Admin.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasRole('ADMIN')") - SOLO ADMINISTRADORES
     *
     * Casos de uso:
     * - Promover trabajador a administrador
     * - Degradar administrador a trabajador
     *
     * Validaciones:
     * - Solo permite roles: Trabajador (2) o Admin (3)
     * - NO permite cambiar a Cliente (1)
     *
     * @param id ID del trabajador
     * @param request DTO con el ID del nuevo rol
     * @return ResponseEntity con TrabajadorDTO actualizado (200 OK)
     */
    @Operation(
            summary = "Cambiar rol de un trabajador",
            description = "Permite a Admin cambiar el rol entre Trabajador y Admin. Solo Admin."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Rol cambiado exitosamente",
                    content = @Content(schema = @Schema(implementation = TrabajadorDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Rol inválido (solo permite Trabajador o Admin)"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN"),
            @ApiResponse(responseCode = "404", description = "Trabajador o rol no encontrado")
    })
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/{id}/rol")
    public ResponseEntity<TrabajadorDTO> cambiarRolTrabajador(
            @Parameter(description = "ID del trabajador", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarRolRequest request) {
        logger.info("PUT /api/trabajadores/{}/rol - Cambiando rol", id);
        TrabajadorDTO trabajador = trabajadorService.cambiarRolTrabajador(id, request);
        return ResponseEntity.ok(trabajador);
    }

    /**
     * DELETE /api/trabajadores/{id} - Eliminar un trabajador del sistema
     *
     * Elimina permanentemente un trabajador y su usuario asociado.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasRole('ADMIN')") - SOLO ADMINISTRADORES
     * - Esta es la operación más sensible (solo ADMIN)
     *
     * ADVERTENCIA: Esta operación elimina:
     * - El registro del trabajador
     * - El usuario asociado (dependiendo de la configuración de cascada)
     *
     * IMPORTANTE: Considerar el impacto si el trabajador tiene:
     * - Pedidos asignados
     * - Historial de atención al cliente
     *
     * Flujo:
     * 1. Verifica que el trabajador existe
     * 2. Elimina el trabajador de la BD
     * 3. Retorna 204 No Content
     *
     * @param id ID del trabajador a eliminar
     * @return ResponseEntity<Void> (204 No Content)
     * @throws IllegalArgumentException si el trabajador no existe (400)
     * @throws AccessDeniedException si el usuario no es ADMIN (403)
     */
    @Operation(
            summary = "Eliminar un trabajador",
            description = "Elimina permanentemente un trabajador del sistema. Solo Admin. ADVERTENCIA: Operación irreversible."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Trabajador eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Trabajador no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')") // RESTRICCIÓN MÁXIMA: Solo ADMIN puede eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteTrabajador(
            @Parameter(description = "ID del trabajador a eliminar", required = true)
            @PathVariable Long id) {
        logger.info("DELETE /api/trabajadores/{} - Eliminando trabajador", id);
        trabajadorService.deleteTrabajador(id);
        return ResponseEntity.noContent().build(); // 204 No Content (sin cuerpo)
    }
}
