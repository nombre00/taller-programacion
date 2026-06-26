package com.goldenburgers.gestionUsuario.controller;

import com.goldenburgers.gestionUsuario.DTOs.ActualizarDireccionRequest;
import com.goldenburgers.gestionUsuario.DTOs.ActualizarEmailRequest;
import com.goldenburgers.gestionUsuario.DTOs.ActualizarPerfilCliente;
import com.goldenburgers.gestionUsuario.DTOs.ClienteDTO;
import com.goldenburgers.gestionUsuario.DTOs.CrearDireccionCliente;
import com.goldenburgers.gestionUsuario.DTOs.DireccionClienteDTO;
import com.goldenburgers.gestionUsuario.DTOs.RegistrarCliente;
import com.goldenburgers.gestionUsuario.service.ClienteService;
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
 * Controller REST para la gestión de Clientes
 *
 * Este controlador maneja todas las operaciones CRUD para clientes del sistema.
 * Se integra con:
 * - ClienteService: para la lógica de negocio
 * - Firebase Authentication: para validar tokens JWT (obligatorio en todos los endpoints)
 * - Spring Security: para control de acceso basado en roles
 *
 * Seguridad:
 * - Todos los endpoints requieren autenticación JWT (@SecurityRequirement)
 * - Algunos endpoints tienen restricciones adicionales por rol (@PreAuthorize)
 *
 * Ruta base: /api/clientes
 */
@RestController
@RequestMapping("/api/clientes")
@Tag(name = "Clientes", description = "API para gestión de clientes del sistema")
@SecurityRequirement(name = "bearer-jwt") // Requiere token JWT en todas las peticiones
public class ClienteController {

    private static final Logger logger = LoggerFactory.getLogger(ClienteController.class);

    // Inyección del servicio que contiene la lógica de negocio para clientes
    @Autowired
    private ClienteService clienteService;

    /**
     * POST /api/clientes - Registrar un nuevo cliente en el sistema
     *
     * Endpoint para crear un nuevo cliente. Requiere que el usuario ya esté
     * registrado en Firebase Authentication.
     *
     * Flujo:
     * 1. Valida el request con @Valid (verifica campos obligatorios y formatos)
     * 2. Verifica que el idUsuario y email no existan en la BD
     * 3. Busca el rol "Cliente" en la BD
     * 4. Crea el registro en tabla USUARIOS
     * 5. Crea el registro en tabla CLIENTES
     * 6. Retorna el ClienteDTO con los datos creados
     *
     * Validaciones en RegisterClienteRequest:
     * - idUsuario: obligatorio (Firebase UID)
     * - email: obligatorio y formato válido
     * - nombreCliente: obligatorio
     * - telefonoCliente: opcional, debe ser 9 dígitos
     *
     * Restricciones de seguridad:
     * - Requiere token JWT válido
     * - NO tiene restricción de rol (cualquier usuario autenticado puede registrarse como cliente)
     *
     * @param request DTO con los datos del cliente a registrar
     * @return ResponseEntity con ClienteDTO (201 Created) si es exitoso
     * @throws IllegalArgumentException si el usuario/email ya existe o el rol no se encuentra
     */
    @Operation(
            summary = "Registrar un nuevo cliente",
            description = "Crea un nuevo cliente en el sistema. El usuario debe estar previamente registrado en Firebase Authentication."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Cliente creado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos en la petición"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token faltante o inválido"
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "El cliente ya existe"
            )
    })
    @PostMapping
    public ResponseEntity<ClienteDTO> registerCliente(@Valid @RequestBody RegistrarCliente request) {
        logger.info("POST /api/clientes - Registrando nuevo cliente");
        // Delega la lógica de negocio al servicio
        ClienteDTO cliente = clienteService.registerCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
    }

    /**
     * GET /api/clientes - Listar todos los clientes del sistema
     *
     * Obtiene la lista completa de clientes registrados en el sistema.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") - SOLO ADMIN y TRABAJADOR pueden acceder
     * - Los clientes normales NO pueden ver la lista completa
     * - Si un usuario con rol CLIENTE intenta acceder, recibirá error 403 Forbidden
     *
     * Flujo:
     * 1. Spring Security valida el token JWT
     * 2. Verifica que el usuario tenga rol ADMIN o TRABAJADOR
     * 3. Llama al servicio para obtener todos los clientes
     * 4. Retorna la lista en formato JSON
     *
     * @return ResponseEntity con List<ClienteDTO> (200 OK)
     * @throws AccessDeniedException si el usuario no tiene rol ADMIN o TRABAJADOR (403)
     */
    @Operation(
            summary = "Listar todos los clientes",
            description = "Obtiene una lista de todos los clientes registrados en el sistema. Requiere rol ADMIN o TRABAJADOR."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de clientes obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            ),
            @ApiResponse(
                    responseCode = "403",
                    description = "No tiene permisos - Requiere rol ADMIN o TRABAJADOR"
            )
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") // RESTRICCIÓN: Solo ADMIN y TRABAJADOR
    @GetMapping
    public ResponseEntity<List<ClienteDTO>> getAllClientes() {
        logger.info("GET /api/clientes - Listando todos los clientes");
        List<ClienteDTO> clientes = clienteService.getAllClientes();
        return ResponseEntity.ok(clientes);
    }



      /**
   * POST /api/clientes/admin - Registrar un nuevo cliente (Solo Admin)
   *
   * Endpoint protegido para que ADMIN cree clientes para otros usuarios.
   * A diferencia del endpoint público POST /api/clientes, este requiere:
   * - Token JWT válido del admin
   * - Rol ADMIN
   *
   * Flujo:
   * 1. Admin crea usuario en Firebase (obtiene UID)
   * 2. Admin llama a este endpoint con el UID del nuevo usuario
   * 3. Se crea el usuario en Oracle con rol "Cliente"
   * 4. Se crea el registro de cliente
   *
   * @param request DTO con los datos del cliente a registrar
   * @return ResponseEntity con ClienteDTO (201 Created) si es exitoso
   */
  @Operation(
          summary = "Registrar un nuevo cliente (Admin)",
          description = "Permite a un administrador crear un cliente para otro usuario. Requiere rol ADMIN."
  )
  @ApiResponses(value = {
          @ApiResponse(
                  responseCode = "201",
                  description = "Cliente creado exitosamente",
                  content = @Content(schema = @Schema(implementation = ClienteDTO.class))
          ),
          @ApiResponse(
                  responseCode = "400",
                  description = "Datos inválidos en la petición"
          ),
          @ApiResponse(
                  responseCode = "401",
                  description = "No autenticado - Token faltante o inválido"
          ),
          @ApiResponse(
                  responseCode = "403",
                  description = "No tiene permisos - Requiere rol ADMIN"
          ),
          @ApiResponse(
                  responseCode = "409",
                  description = "El cliente ya existe"
          )
  })
  @PreAuthorize("hasRole('ADMIN')") // RESTRICCIÓN: Solo ADMIN
  @PostMapping("/admin")
  public ResponseEntity<ClienteDTO> registerClienteByAdmin(@Valid @RequestBody RegistrarCliente request) {
      logger.info("POST /api/clientes/admin - Admin registrando nuevo cliente");
      // Reutiliza la misma lógica del servicio
      ClienteDTO cliente = clienteService.registerCliente(request);
      return ResponseEntity.status(HttpStatus.CREATED).body(cliente);
  }

    /**
     * GET /api/clientes/{id} - Obtener cliente por ID
     */
    @Operation(
            summary = "Obtener cliente por ID",
            description = "Obtiene la información detallada de un cliente específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado"
            )
    })
    @GetMapping("/{id}")
    public ResponseEntity<ClienteDTO> getClienteById(
            @Parameter(description = "ID del cliente", required = true, example = "1")
            @PathVariable Long id) {
        logger.info("GET /api/clientes/{} - Obteniendo cliente por ID", id);
        ClienteDTO cliente = clienteService.getClienteById(id);
        return ResponseEntity.ok(cliente);
    }

    /**
     * GET /api/clientes/usuario/{idUsuario} - Obtener cliente por Firebase UID
     */
    @Operation(
            summary = "Obtener cliente por Firebase UID",
            description = "Busca un cliente por su ID de usuario de Firebase Authentication"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<ClienteDTO> getClienteByUsuarioId(
            @Parameter(description = "Firebase UID del usuario", required = true)
            @PathVariable String idUsuario) {
        logger.info("GET /api/clientes/usuario/{} - Obteniendo cliente por usuario", idUsuario);
        ClienteDTO cliente = clienteService.getClienteByUsuarioId(idUsuario);
        return ResponseEntity.ok(cliente);
    }

    /**
     * GET /api/clientes/email/{email} - Obtener cliente por email
     */
    @Operation(
            summary = "Obtener cliente por email",
            description = "Busca un cliente por su dirección de correo electrónico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente encontrado",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/email/{email}")
    public ResponseEntity<ClienteDTO> getClienteByEmail(
            @Parameter(description = "Email del cliente", required = true)
            @PathVariable String email) {
        logger.info("GET /api/clientes/email/{} - Obteniendo cliente por email", email);
        ClienteDTO cliente = clienteService.getClienteByEmail(email);
        return ResponseEntity.ok(cliente);
    }

    /**
     * PUT /api/clientes/{id} - Actualizar datos de un cliente existente
     *
     * Permite modificar el nombre y teléfono de un cliente.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") - SOLO ADMIN y TRABAJADOR
     * - Los clientes NO pueden modificar sus propios datos directamente
     *
     * Parámetros:
     * - id: ID del cliente a actualizar (obligatorio, en la URL)
     * - nombreCliente: Nuevo nombre (obligatorio, @RequestParam)
     * - telefonoCliente: Nuevo teléfono (opcional, @RequestParam)
     *
     * Flujo:
     * 1. Busca el cliente por ID
     * 2. Actualiza los campos nombre y teléfono
     * 3. Guarda los cambios en la BD
     * 4. Retorna el ClienteDTO actualizado
     *
     * @param id ID del cliente a actualizar
     * @param nombreCliente Nuevo nombre del cliente
     * @param telefonoCliente Nuevo teléfono (opcional)
     * @return ResponseEntity con ClienteDTO actualizado (200 OK)
     * @throws IllegalArgumentException si el cliente no existe (400)
     */
    @Operation(
            summary = "Actualizar datos de un cliente",
            description = "Permite a Admin/Trabajador modificar nombre y teléfono de un cliente. Requiere rol ADMIN o TRABAJADOR."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Cliente actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN o TRABAJADOR"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") // RESTRICCIÓN: Solo ADMIN y TRABAJADOR
    @PutMapping("/{id}")
    public ResponseEntity<ClienteDTO> updateCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id,
            @Parameter(description = "Nuevo nombre del cliente", required = true)
            @RequestParam String nombreCliente,
            @Parameter(description = "Nuevo teléfono del cliente (9 dígitos)")
            @RequestParam(required = false) String telefonoCliente) {
        logger.info("PUT /api/clientes/{} - Actualizando cliente", id);
        ClienteDTO cliente = clienteService.updateCliente(id, nombreCliente, telefonoCliente);
        return ResponseEntity.ok(cliente);
    }

    /**
     * PUT /api/clientes/perfil - Actualizar el perfil del cliente autenticado
     *
     * Permite al cliente modificar sus propios datos personales.
     * El Firebase UID se extrae del token JWT automáticamente.
     *
     * Casos de uso:
     * - Cliente olvida la contraseña de su email original y no puede recuperarlo
     * - Cliente cambia de número de teléfono
     * - Cliente se cambia de nombre legalmente
     * - Cliente desea corregir datos ingresados incorrectamente
     *
     * Restricciones de seguridad:
     * - Requiere token JWT válido
     * - NO tiene restricción de rol (cualquier cliente autenticado puede actualizar su perfil)
     * - El cliente solo puede modificar SUS PROPIOS datos
     *
     * Flujo:
     * 1. Spring Security extrae el Firebase UID del token JWT
     * 2. Valida el request con @Valid (verifica campos obligatorios y formatos)
     * 3. Busca el cliente por Firebase UID
     * 4. Valida que el nuevo email no esté en uso (si cambió)
     * 5. Actualiza nombre, email y teléfono
     * 6. Retorna el ClienteDTO actualizado
     *
     * Validaciones en ActualizarPerfilCliente:
     * - nombreCliente: obligatorio
     * - email: obligatorio y formato válido, debe ser único
     * - telefonoCliente: opcional, debe ser 9 dígitos
     *
     * IMPORTANTE:
     * - Este endpoint NO actualiza el email en Firebase Authentication
     * - El email en Firebase debe actualizarse desde el frontend usando Firebase SDK
     * - Se recomienda: 1) Actualizar en Firebase, 2) Llamar a este endpoint
     *
     * @param request DTO con los nuevos datos del perfil
     * @param authentication Objeto de Spring Security con info del usuario autenticado
     * @return ResponseEntity con ClienteDTO actualizado (200 OK)
     * @throws IllegalArgumentException si el cliente no existe o el email está en uso (400)
     */
    @Operation(
            summary = "Actualizar perfil del cliente autenticado",
            description = "Permite al cliente modificar sus datos personales: nombre, email y teléfono. " +
                    "El Firebase UID se obtiene automáticamente del token JWT."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Perfil actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Datos inválidos o email ya en uso"
            ),
            @ApiResponse(
                    responseCode = "401",
                    description = "No autenticado - Token faltante o inválido"
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Cliente no encontrado para el usuario autenticado"
            )
    })
    /*
    @PutMapping("/perfil")
    public ResponseEntity<ClienteDTO> actualizarPerfil(
            @Valid @RequestBody ActualizarPerfilCliente request,
            @Parameter(hidden = true) org.springframework.security.core.Authentication authentication) {
        
        // Extraer el Firebase UID del token JWT (proporcionado por Spring Security)
        String firebaseUid = authentication.getName();
        logger.info("PUT /api/clientes/perfil - Actualizando perfil del cliente UID: {}", firebaseUid);
        
        // Llamar al servicio para actualizar el perfil
        ClienteDTO cliente = clienteService.actualizarPerfil(firebaseUid, request);
        return ResponseEntity.ok(cliente);
    } */

        @PutMapping("/perfil")
  public ResponseEntity<ClienteDTO> actualizarPerfil(
          @Valid @RequestBody ActualizarPerfilCliente request,
          @Parameter(hidden = true) org.springframework.security.core.Authentication authentication) {

      // Extraer el Firebase UID del CustomUserDetails (proporcionado por JwtAuthenticationFilter)
      // NOTA: authentication.getName() devuelve el EMAIL, no el UID
      // Por eso obtenemos el principal y casteamos a CustomUserDetails
      com.goldenburgers.gestionUsuario.security.CustomUserDetails userDetails = (com.goldenburgers.gestionUsuario.security.CustomUserDetails)authentication.getPrincipal();
      String firebaseUid = userDetails.getFirebaseUid();

      logger.info("PUT /api/clientes/perfil - Actualizando perfil del cliente UID: {}",firebaseUid);

      // Llamar al servicio para actualizar el perfil
      ClienteDTO cliente = clienteService.actualizarPerfil(firebaseUid, request);
      return ResponseEntity.ok(cliente);
  }

    /**
     * PUT /api/clientes/{id}/email - Actualizar email de un cliente (Admin/Trabajador)
     *
     * Permite a ADMIN o TRABAJADOR modificar el email de un cliente.
     * Útil cuando el cliente no puede acceder a su email anterior.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')") - SOLO ADMIN y TRABAJADOR
     *
     * Casos de uso:
     * - Cliente olvidó contraseña de email anterior y no puede recuperarlo
     * - Corrección administrativa de emails incorrectos
     * - Cliente solicita cambio de email por soporte
     *
     * IMPORTANTE:
     * - Este endpoint actualiza el email en la base de datos local
     * - NO actualiza el email en Firebase Authentication
     * - El email en Firebase debe actualizarse desde el frontend
     *
     * @param id ID del cliente
     * @param request DTO con el nuevo email
     * @return ResponseEntity con ClienteDTO actualizado (200 OK)
     */
    @Operation(
            summary = "Actualizar email de un cliente",
            description = "Permite a Admin/Trabajador modificar el email de un cliente. Útil cuando el cliente no puede acceder a su email anterior."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Email actualizado exitosamente",
                    content = @Content(schema = @Schema(implementation = ClienteDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Email inválido o ya en uso"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos"),
            @ApiResponse(responseCode = "404", description = "Cliente no encontrado")
    })
    @PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')")
    @PutMapping("/{id}/email")
    public ResponseEntity<ClienteDTO> actualizarEmailCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long id,
            @Valid @RequestBody ActualizarEmailRequest request) {
        logger.info("PUT /api/clientes/{}/email - Actualizando email", id);
        ClienteDTO cliente = clienteService.actualizarEmailCliente(id, request);
        return ResponseEntity.ok(cliente);
    }

    /**
     * DELETE /api/clientes/{id} - Eliminar un cliente del sistema
     *
     * Elimina permanentemente un cliente y su usuario asociado.
     *
     * Restricciones de seguridad:
     * - @PreAuthorize("hasRole('ADMIN')") - SOLO ADMINISTRADORES
     * - Los trabajadores NO pueden eliminar clientes
     * - Esta es la operación más restrictiva (solo ADMIN)
     *
     * ADVERTENCIA: Esta operación elimina:
     * - El registro del cliente
     * - Todas las direcciones asociadas (si existen)
     * - El usuario asociado (dependiendo de la configuración de cascada)
     *
     * Flujo:
     * 1. Verifica que el cliente existe
     * 2. Elimina el cliente de la BD
     * 3. Retorna 204 No Content (sin cuerpo en la respuesta)
     *
     * @param id ID del cliente a eliminar
     * @return ResponseEntity<Void> (204 No Content)
     * @throws IllegalArgumentException si el cliente no existe (400)
     */
    @Operation(
            summary = "Eliminar un cliente",
            description = "Elimina permanentemente un cliente del sistema. Solo Admin. ADVERTENCIA: Operación irreversible."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Cliente eliminado exitosamente"),
            @ApiResponse(responseCode = "400", description = "Cliente no encontrado"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "403", description = "No tiene permisos - Requiere rol ADMIN")
    })
    @PreAuthorize("hasRole('ADMIN')") // RESTRICCIÓN MÁXIMA: Solo ADMIN puede eliminar
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCliente(
            @Parameter(description = "ID del cliente a eliminar", required = true)
            @PathVariable Long id) {
        logger.info("DELETE /api/clientes/{} - Eliminando cliente", id);
        clienteService.deleteCliente(id);
        return ResponseEntity.noContent().build(); // 204 No Content (sin cuerpo)
    }

    // ==================== GESTIÓN DE DIRECCIONES DEL CLIENTE ====================

    /**
     * POST /api/clientes/direcciones - Agregar una nueva dirección a un cliente
     *
     * Permite agregar múltiples direcciones a un cliente (casa, trabajo, etc.).
     *
     * Seguridad:
     * - Requiere token JWT válido
     * - NO tiene restricción de rol (el cliente puede agregar sus propias direcciones)
     *
     * Validaciones en CreateDireccionRequest:
     * - idCliente: obligatorio (debe existir en la BD)
     * - idCiudad: obligatorio (debe existir en la BD)
     * - direccion: obligatorio (texto de la dirección)
     * - alias: opcional (ej: "Casa", "Trabajo", "Casa de mis padres")
     *
     * Flujo:
     * 1. Valida el request con @Valid
     * 2. Verifica que el cliente existe
     * 3. Verifica que la ciudad existe
     * 4. Crea la dirección asociada al cliente
     * 5. Retorna el DireccionClienteDTO creado
     *
     * @param request DTO con los datos de la dirección (idCliente, idCiudad, direccion, alias)
     * @return ResponseEntity con DireccionClienteDTO (201 Created)
     * @throws IllegalArgumentException si el cliente o ciudad no existen (400)
     */
    @Operation(
            summary = "Agregar dirección a un cliente",
            description = "Permite agregar una nueva dirección de entrega a un cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Dirección creada exitosamente",
                    content = @Content(schema = @Schema(implementation = DireccionClienteDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Cliente o ciudad no encontrados")
    })
    @PostMapping("/direcciones")
    public ResponseEntity<DireccionClienteDTO> agregarDireccion(
            @Valid @RequestBody CrearDireccionCliente request) {
        logger.info("POST /api/clientes/direcciones - Agregando dirección");
        DireccionClienteDTO direccion = clienteService.agregarDireccion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(direccion);
    }

    /**
     * GET /api/clientes/{idCliente}/direcciones - Obtener todas las direcciones de un cliente
     *
     * Retorna la lista de todas las direcciones asociadas a un cliente específico.
     *
     * Seguridad:
     * - Requiere token JWT válido
     * - NO tiene restricción de rol
     * - Útil para que el cliente vea sus direcciones al hacer un pedido
     *
     * Uso típico:
     * - En el frontend al mostrar el listado de direcciones para seleccionar en un pedido
     * - Para validar si el cliente tiene direcciones registradas
     *
     * @param idCliente ID del cliente del cual obtener las direcciones
     * @return ResponseEntity con List<DireccionClienteDTO> (200 OK)
     */
    @Operation(
            summary = "Listar direcciones de un cliente",
            description = "Obtiene todas las direcciones de entrega asociadas a un cliente específico"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Lista de direcciones obtenida exitosamente",
                    content = @Content(schema = @Schema(implementation = DireccionClienteDTO.class))
            ),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @GetMapping("/{idCliente}/direcciones")
    public ResponseEntity<List<DireccionClienteDTO>> getDireccionesByCliente(
            @Parameter(description = "ID del cliente", required = true)
            @PathVariable Long idCliente) {
        logger.info("GET /api/clientes/{}/direcciones - Obteniendo direcciones", idCliente);
        List<DireccionClienteDTO> direcciones = clienteService.getDireccionesByCliente(idCliente);
        return ResponseEntity.ok(direcciones);
    }

    /**
     * PUT /api/clientes/direcciones/{idDireccion} - Actualizar dirección existente
     *
     * Permite modificar ciudad, dirección y alias de una dirección ya registrada.
     *
     * Seguridad:
     * - Requiere token JWT válido
     * - NO tiene restricción de rol (el cliente puede actualizar sus propias direcciones)
     *
     * Casos de uso:
     * - Cliente se muda a nueva dirección
     * - Corrección de errores en dirección ingresada
     * - Cambio de ciudad
     * - Actualización del alias descriptivo
     *
     * @param idDireccion ID de la dirección a actualizar
     * @param request DTO con los nuevos datos (idCiudad, direccion, alias)
     * @return ResponseEntity con DireccionClienteDTO actualizado (200 OK)
     */
    @Operation(
            summary = "Actualizar dirección existente",
            description = "Permite modificar una dirección de entrega ya registrada"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Dirección actualizada exitosamente",
                    content = @Content(schema = @Schema(implementation = DireccionClienteDTO.class))
            ),
            @ApiResponse(responseCode = "400", description = "Datos inválidos"),
            @ApiResponse(responseCode = "401", description = "No autenticado"),
            @ApiResponse(responseCode = "404", description = "Dirección o ciudad no encontradas")
    })
    @PutMapping("/direcciones/{idDireccion}")
    public ResponseEntity<DireccionClienteDTO> actualizarDireccion(
            @Parameter(description = "ID de la dirección a actualizar", required = true)
            @PathVariable Long idDireccion,
            @Valid @RequestBody ActualizarDireccionRequest request) {
        logger.info("PUT /api/clientes/direcciones/{} - Actualizando dirección", idDireccion);
        DireccionClienteDTO direccion = clienteService.actualizarDireccion(idDireccion, request);
        return ResponseEntity.ok(direccion);
    }

    /**
     * DELETE /api/clientes/direcciones/{idDireccion} - Eliminar una dirección específica
     *
     * Elimina una dirección de entrega de un cliente.
     *
     * Seguridad:
     * - Requiere token JWT válido
     * - NO tiene restricción de rol (el cliente puede eliminar sus propias direcciones)
     *
     * NOTA: No elimina al cliente, solo la dirección específica.
     * El cliente puede tener 0 o más direcciones.
     *
     * @param idDireccion ID de la dirección a eliminar
     * @return ResponseEntity<Void> (204 No Content)
     * @throws IllegalArgumentException si la dirección no existe (400)
     */
    @Operation(
            summary = "Eliminar una dirección",
            description = "Elimina una dirección de entrega específica de un cliente"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dirección eliminada exitosamente"),
            @ApiResponse(responseCode = "400", description = "Dirección no encontrada"),
            @ApiResponse(responseCode = "401", description = "No autenticado")
    })
    @DeleteMapping("/direcciones/{idDireccion}")
    public ResponseEntity<Void> eliminarDireccion(
            @Parameter(description = "ID de la dirección a eliminar", required = true)
            @PathVariable Long idDireccion) {
        logger.info("DELETE /api/clientes/direcciones/{} - Eliminando dirección", idDireccion);
        clienteService.eliminarDireccion(idDireccion);
        return ResponseEntity.noContent().build();
    }
}
