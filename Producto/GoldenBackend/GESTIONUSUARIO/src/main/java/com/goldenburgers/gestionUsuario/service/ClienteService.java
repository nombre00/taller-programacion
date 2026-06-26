package com.goldenburgers.gestionUsuario.service;

import com.goldenburgers.gestionUsuario.DTOs.ActualizarDireccionRequest;
import com.goldenburgers.gestionUsuario.DTOs.ActualizarEmailRequest;
import com.goldenburgers.gestionUsuario.DTOs.ActualizarPerfilCliente;
import com.goldenburgers.gestionUsuario.DTOs.ClienteDTO;
import com.goldenburgers.gestionUsuario.DTOs.CrearDireccionCliente;
import com.goldenburgers.gestionUsuario.DTOs.DireccionClienteDTO;
import com.goldenburgers.gestionUsuario.DTOs.RegistrarCliente;
import com.goldenburgers.gestionUsuario.model.*;
import com.goldenburgers.gestionUsuario.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de negocio para la gestión de Clientes
 *
 * Capa de servicio que contiene toda la lógica de negocio relacionada con clientes.
 * Se encarga de validar, procesar y coordinar las operaciones entre los repositories.
 *
 * Integraciones:
 * - ClienteRepository: CRUD de clientes
 * - UsuarioRepository: Gestión de usuarios (tabla base para autenticación)
 * - RolRepository: Obtención del rol "Cliente"
 * - CiudadRepository: Validación de ciudades para direcciones
 * - DireccionClienteRepository: Gestión de direcciones de entrega
 * - EntityMapper: Conversión entre entidades JPA y DTOs
 *
 * Transaccionalidad: 
 * - @Transactional a nivel de clase: TODAS las operaciones son transaccionales
 * - Si ocurre un error, se hace rollback automático de todos los cambios
 * - Garantiza consistencia de datos (ej: si falla crear cliente, también revierte crear usuario)
 *
 * Llamado desde:
 * - ClienteController: para procesar las peticiones HTTP
 */

@Service
@Transactional // TODAS las operaciones de este servicio son transaccionales
public class ClienteService {
    
    // Se crea para captar logs de la clase y usar en métodos
    private static final Logger logger = LoggerFactory.getLogger(ClienteService.class);

    // Repository para operaciones CRUD de clientes en la tabla CLIENTES
    @Autowired
    private ClienteRepository clienteRepository;

    // Repository para gestionar la tabla USUARIOS (relación con Firebase Auth)
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Repository para obtener roles del sistema (Cliente, Trabajador, Admin)
    @Autowired
    private RolRepository rolRepository;

    // Repository para validar ciudades al crear direcciones
    @Autowired
    private CiudadRepository ciudadRepository;

    // Repository para gestionar direcciones de entrega de los clientes
    @Autowired
    private DireccionClienteRepository direccionClienteRepository;

    // Mapper para convertir entidades JPA a DTOs (y viceversa)
    @Autowired
    private EntityMapper entityMapper;

    /**
     * Registra un nuevo cliente en el sistema
     *
     * Este método coordina la creación de un usuario y su cliente asociado en una transacción.
     *
     * Flujo de ejecución:
     * 1. Valida que el Firebase UID no exista (usuarioRepository.existsByIdUsuario)
     * 2. Valida que el email no esté registrado (usuarioRepository.existsByEmail)
     * 3. Obtiene el rol "Cliente" desde la BD (rolRepository.findByNombreRol)
     * 4. Crea registro en tabla USUARIOS con:
     *    - idUsuario = Firebase UID
     *    - email
     *    - rol = "Cliente"
     *    - fechaCreacion = fecha actual
     * 5. Crea registro en tabla CLIENTES con:
     *    - usuario = usuario recién creado (FK)
     *    - nombreCliente
     *    - telefonoCliente
     * 6. Convierte la entidad Cliente a ClienteDTO usando EntityMapper
     *
     * Validaciones:
     * - Firebase UID único
     * - Email único
     * - Rol "Cliente" debe existir en la BD
     *
     * Transaccionalidad:
     * - Si falla algún paso, se hace rollback de Todo (usuario y cliente)
     *
     * @param request DTO con datos del cliente (idUsuario, email, nombreCliente, telefonoCliente)
     * @return ClienteDTO con los datos del cliente creado (incluye IDs generados)
     * @throws IllegalArgumentException si el usuario o email ya existe (400)
     * @throws IllegalStateException si el rol "Cliente" no existe en BD (409)
     */
    public ClienteDTO registerCliente(RegistrarCliente request) {
        logger.info("Registrando nuevo cliente con email: {}", request.getEmail());

        // VALIDACIÓN 1: Verificar que el Firebase UID no esté registrado
        if (usuarioRepository.existsByIdUsuario(request.getIdUsuario())) {
            throw new IllegalArgumentException("El usuario con ID " + request.getIdUsuario() + " ya está registrado");
        }

        // VALIDACIÓN 2: Verificar que el email no esté en uso
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email " + request.getEmail() + " ya está registrado");
        }

        // PASO 1: Obtener el rol "Cliente" de la BD (debe existir como dato maestro)
        Rol rolCliente = rolRepository.findByNombreRol("Cliente")
                .orElseThrow(() -> new IllegalStateException("Rol 'Cliente' no encontrado en la base de datos"));

        // PASO 2: Crear registro en tabla USUARIOS (vinculado a Firebase Auth)
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(request.getIdUsuario()); // Firebase UID
        usuario.setEmail(request.getEmail());
        usuario.setRol(rolCliente); // FK a tabla ROLES
        usuario.setFechaCreacion(LocalDate.now());
        usuario = usuarioRepository.save(usuario); // INSERT en USUARIOS

        // PASO 3: Crear registro en tabla CLIENTES (vinculado al usuario)
        Cliente cliente = new Cliente();
        cliente.setUsuario(usuario); // FK a tabla USUARIOS
        cliente.setNombreCliente(request.getNombreCliente());
        cliente.setTelefonoCliente(request.getTelefonoCliente());
        cliente = clienteRepository.save(cliente); // INSERT en CLIENTES

        logger.info("Cliente registrado exitosamente con ID: {}", cliente.getIdCliente());

        // PASO 4: Convertir entidad JPA a DTO para retornar al controller
        return entityMapper.toDTO(cliente);
    }

    /**
     * Obtiene un cliente por su ID de base de datos
     *
     * Busca un cliente por su clave primaria (idCliente).
     *
     * Integraciones:
     * - clienteRepository.findById(): Busca en tabla CLIENTES
     * - EntityMapper.toDTO(): Convierte entidad JPA a DTO
     *
     * @param idCliente ID del cliente (clave primaria)
     * @return ClienteDTO con los datos del cliente
     * @throws IllegalArgumentException si el cliente no existe (manejado como 400 por GlobalExceptionHandler)
     */
    public ClienteDTO getClienteById(Long idCliente) {
        // Busca el cliente, si no existe lanza excepción
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idCliente));
        return entityMapper.toDTO(cliente);
    }

    /**
     * Obtiene un cliente por su Firebase UID
     *
     * Busca un cliente a través del ID de usuario de Firebase Authentication.
     * Útil para obtener los datos del cliente autenticado actualmente.
     *
     * Integraciones:
     * - clienteRepository.findByUsuarioIdUsuario(): JOIN entre CLIENTES y USUARIOS
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Query generado (aproximado):
     * SELECT c.* FROM CLIENTES c
     * JOIN USUARIOS u ON c.id_usuario = u.id_usuario
     * WHERE u.id_usuario = ?
     *
     * @param idUsuario Firebase UID del usuario
     * @return ClienteDTO con los datos del cliente
     * @throws IllegalArgumentException si no existe cliente con ese Firebase UID
     */
    public ClienteDTO getClienteByUsuarioId(String idUsuario) {
        // Busca cliente por relación con tabla USUARIOS
        Cliente cliente = clienteRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado para usuario: " + idUsuario));
        return entityMapper.toDTO(cliente);
    }

    /**
     * Obtiene un cliente por su email
     *
     * Busca un cliente a través del email registrado en la tabla USUARIOS.
     *
     * Integraciones:
     * - clienteRepository.findByUsuarioEmail(): JOIN entre CLIENTES y USUARIOS
     *
     * Query generado (aproximado):
     * SELECT c.* FROM CLIENTES c
     * JOIN USUARIOS u ON c.id_usuario = u.id_usuario
     * WHERE u.email = ?
     *
     * @param email Email del usuario
     * @return ClienteDTO con los datos del cliente
     * @throws IllegalArgumentException si no existe cliente con ese email
     */
    public ClienteDTO getClienteByEmail(String email) {
        // Busca cliente por email en tabla USUARIOS (relación)
        Cliente cliente = clienteRepository.findByUsuarioEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con email: " + email));
        return entityMapper.toDTO(cliente);
    }

    /**
     * Lista todos los clientes registrados en el sistema
     *
     * Obtiene la lista completa de clientes sin paginación.
     * Usado por ADMIN y TRABAJADOR para ver todos los clientes.
     *
     * Integraciones:
     * - clienteRepository.findAll(): SELECT * FROM CLIENTES
     * - EntityMapper.toClienteDTOList(): Convierte lista de entidades a lista de DTOs
     *
     * NOTA: En un sistema con muchos clientes, considerar implementar paginación.
     *
     * @return List<ClienteDTO> con todos los clientes (puede ser lista vacía)
     */
    public List<ClienteDTO> getAllClientes() {
        // Obtiene todos los clientes de la BD (sin filtros)
        List<Cliente> clientes = clienteRepository.findAll();
        // Convierte lista de entidades a lista de DTOs
        return entityMapper.toClienteDTOList(clientes);
    }

    /**
     * Actualiza los datos de un cliente existente
     *
     * Permite modificar nombre y teléfono de un cliente.
     * NO modifica el usuario asociado (Firebase UID, email, rol).
     *
     * Integraciones:
     * - clienteRepository.findById(): Busca el cliente
     * - clienteRepository.save(): UPDATE en tabla CLIENTES
     * - EntityMapper.toDTO(): Convierte resultado a DTO
     *
     * Transaccionalidad:
     * - Si falla, se revierte el UPDATE
     *
     * @param idCliente ID del cliente a actualizar
     * @param nombreCliente Nuevo nombre
     * @param telefonoCliente Nuevo teléfono (puede ser null)
     * @return ClienteDTO con los datos actualizados
     * @throws IllegalArgumentException si el cliente no existe
     */
    public ClienteDTO updateCliente(Long idCliente, String nombreCliente, String telefonoCliente) {
        // Busca el cliente, si no existe lanza excepción
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idCliente));

        // Actualiza solo los campos permitidos
        cliente.setNombreCliente(nombreCliente);
        cliente.setTelefonoCliente(telefonoCliente);
        cliente = clienteRepository.save(cliente); // UPDATE en CLIENTES

        logger.info("Cliente actualizado: {}", idCliente);
        return entityMapper.toDTO(cliente);
    }

    /**
     * Agrega una nueva dirección de entrega a un cliente
     *
     * Permite al cliente tener múltiples direcciones (casa, trabajo, etc.).
     * Útil para seleccionar dirección al momento de hacer un pedido.
     *
     * Flujo de ejecución:
     * 1. Valida que el cliente existe (clienteRepository.findById)
     * 2. Valida que la ciudad existe (ciudadRepository.findById)
     * 3. Crea registro en tabla DIRECCION_CLIENTE con:
     *    - cliente = cliente encontrado (FK)
     *    - ciudad = ciudad encontrada (FK)
     *    - direccion = texto de la dirección
     *    - alias = nombre descriptivo (ej: "Casa", "Trabajo")
     * 4. Guarda la dirección en la BD
     * 5. Convierte entidad a DireccionClienteDTO usando EntityMapper
     *
     * Integraciones:
     * - clienteRepository.findById(): Busca el cliente
     * - ciudadRepository.findById(): Valida la ciudad
     * - direccionClienteRepository.save(): INSERT en DIRECCION_CLIENTE
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Validaciones:
     * - Cliente debe existir en la BD
     * - Ciudad debe existir en la BD (tabla maestra)
     * - Un cliente puede tener 0 o más direcciones
     *
     * Transaccionalidad:
     * - Si falla la validación, no se crea la dirección
     *
     * @param request DTO con idCliente, idCiudad, direccion y alias
     * @return DireccionClienteDTO con los datos de la dirección creada (incluye ID generado)
     * @throws IllegalArgumentException si el cliente o ciudad no existen (400)
     */
    public DireccionClienteDTO agregarDireccion(CrearDireccionCliente request) {
        // VALIDACIÓN 1: Verificar que el cliente existe
        Cliente cliente = clienteRepository.findById(request.getIdCliente())
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + request.getIdCliente()));

        // VALIDACIÓN 2: Verificar que la ciudad existe (dato maestro)
        Ciudad ciudad = ciudadRepository.findById(request.getIdCiudad())
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada con ID: " + request.getIdCiudad()));

        // PASO 1: Crear registro en tabla DIRECCION_CLIENTE
        DireccionCliente direccion = new DireccionCliente();
        direccion.setCliente(cliente); // FK a tabla CLIENTES
        direccion.setCiudad(ciudad); // FK a tabla CIUDADES
        direccion.setDireccion(request.getDireccion()); // Texto de la dirección
        direccion.setAlias(request.getAlias()); // Nombre descriptivo

        direccion = direccionClienteRepository.save(direccion); // INSERT en DIRECCION_CLIENTE

        logger.info("Dirección agregada al cliente {}: {}", cliente.getIdCliente(), direccion.getAlias());

        // PASO 2: Convertir entidad JPA a DTO para retornar al controller
        return entityMapper.toDTO(direccion);
    }

    /**
     * Obtiene todas las direcciones de entrega de un cliente
     *
     * Retorna la lista completa de direcciones asociadas a un cliente específico.
     * Útil para mostrar al cliente sus direcciones al hacer un pedido.
     *
     * Integraciones:
     * - direccionClienteRepository.findByClienteIdCliente(): Query por JOIN con tabla CLIENTES
     * - EntityMapper.toDireccionDTOList(): Convierte lista de entidades a lista de DTOs
     *
     * Query generado (aproximado):
     * SELECT d.* FROM DIRECCION_CLIENTE d
     * WHERE d.id_cliente = ?
     *
     * Comportamiento:
     * - Si el cliente no tiene direcciones, retorna lista vacía (no lanza excepción)
     * - No valida si el cliente existe (puede retornar lista vacía para ID inexistente)
     * - Incluye información de la ciudad asociada a cada dirección
     *
     * @param idCliente ID del cliente del cual obtener las direcciones
     * @return List<DireccionClienteDTO> con todas las direcciones del cliente (puede ser lista vacía)
     */
    public List<DireccionClienteDTO> getDireccionesByCliente(Long idCliente) {
        // Busca todas las direcciones del cliente (relación uno-a-muchos)
        List<DireccionCliente> direcciones = direccionClienteRepository.findByClienteIdCliente(idCliente);
        // Convierte lista de entidades a lista de DTOs
        return entityMapper.toDireccionDTOList(direcciones);
    }

    /**
     * Elimina una dirección de entrega específica de un cliente
     *
     * Permite al cliente eliminar una dirección que ya no utiliza.
     * NO elimina al cliente, solo la dirección específica.
     *
     * Flujo de ejecución:
     * 1. Valida que la dirección existe (direccionClienteRepository.existsById)
     * 2. Elimina el registro de la tabla DIRECCION_CLIENTE
     *
     * Integraciones:
     * - direccionClienteRepository.existsById(): Verifica existencia
     * - direccionClienteRepository.deleteById(): DELETE en DIRECCION_CLIENTE
     *
     * Validaciones:
     * - La dirección debe existir en la BD
     *
     * Transaccionalidad:
     * - Si falla, se revierte el DELETE
     * - El cliente puede quedar con 0 direcciones (es válido)
     *
     * Consideraciones:
     * - No valida si la dirección está siendo usada en pedidos activos
     * - El cliente asociado NO se elimina
     *
     * @param idDireccion ID de la dirección a eliminar (clave primaria)
     * @throws IllegalArgumentException si la dirección no existe (400)
     */
    public void eliminarDireccion(Long idDireccion) {
        // VALIDACIÓN: Verificar que la dirección existe
        if (!direccionClienteRepository.existsById(idDireccion)) {
            throw new IllegalArgumentException("Dirección no encontrada con ID: " + idDireccion);
        }
        // PASO 1: Eliminar dirección de la BD
        direccionClienteRepository.deleteById(idDireccion); // DELETE en DIRECCION_CLIENTE
        logger.info("Dirección eliminada: {}", idDireccion);
    }

    /**
     * Actualiza el perfil del cliente autenticado
     *
     * Permite al cliente modificar sus propios datos personales.
     * Este método está diseñado para ser llamado por el propio cliente
     * (no requiere rol ADMIN o TRABAJADOR).
     *
     * Casos de uso:
     * - Cliente olvida la contraseña de su email original y no puede recuperarlo
     * - Cliente cambia de número de teléfono
     * - Cliente se cambia de nombre legalmente
     * - Cliente desea corregir datos ingresados incorrectamente
     *
     * Flujo de ejecución:
     * 1. Busca el cliente por Firebase UID (extraído del token JWT)
     * 2. Valida que el nuevo email no esté en uso por otro usuario
     * 3. Actualiza el nombre y teléfono en tabla CLIENTES
     * 4. Actualiza el email en tabla USUARIOS
     * 5. Retorna el ClienteDTO actualizado
     *
     * Validaciones:
     * - El cliente debe existir
     * - El nuevo email debe ser único (no puede estar en uso por otro usuario)
     * - Si el email es el mismo que el actual, no se valida unicidad
     *
     * Campos que se actualizan:
     * - nombreCliente: en tabla CLIENTES
     * - telefonoCliente: en tabla CLIENTES
     * - email: en tabla USUARIOS
     *
     * IMPORTANTE:
     * - Este método NO actualiza el email en Firebase Authentication
     * - El email en Firebase debe actualizarse desde el frontend usando Firebase SDK
     * - Se recomienda actualizar primero en Firebase y luego llamar a este endpoint
     *
     * Transaccionalidad:
     * - Si falla alguna actualización, se hace rollback de todos los cambios
     * - Garantiza consistencia entre CLIENTES y USUARIOS
     *
     * @param idUsuario Firebase UID del cliente (extraído del token JWT)
     * @param request DTO con los nuevos datos (nombreCliente, email, telefonoCliente)
     * @return ClienteDTO con los datos actualizados
     * @throws IllegalArgumentException si el cliente no existe o el email está en uso
     */
    public ClienteDTO actualizarPerfil(String idUsuario, ActualizarPerfilCliente request) {
        logger.info("Actualizando perfil del cliente con UID: {}", idUsuario);

        // PASO 1: Buscar el cliente por Firebase UID
        Cliente cliente = clienteRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado para usuario: " + idUsuario));

        // PASO 2: Validar que el nuevo email no esté en uso por otro usuario
        // Solo valida si el email cambió
        if (!cliente.getUsuario().getEmail().equals(request.getEmail())) {
            if (usuarioRepository.existsByEmail(request.getEmail())) {
                throw new IllegalArgumentException("El email " + request.getEmail() + " ya está en uso por otro usuario");
            }
            // Actualizar email en tabla USUARIOS
            cliente.getUsuario().setEmail(request.getEmail());
            usuarioRepository.save(cliente.getUsuario());
        }

        // PASO 3: Actualizar datos en tabla CLIENTES
        cliente.setNombreCliente(request.getNombreCliente());
        cliente.setTelefonoCliente(request.getTelefonoCliente());
        cliente = clienteRepository.save(cliente);

        logger.info("Perfil actualizado exitosamente para cliente ID: {}", cliente.getIdCliente());

        // PASO 4: Retornar DTO actualizado
        return entityMapper.toDTO(cliente);
    }

    /**
     * Actualiza el email de un cliente (Admin/Trabajador)
     *
     * Permite a ADMIN o TRABAJADOR modificar el email de un cliente.
     * Útil cuando el cliente no puede acceder a su email anterior.
     *
     * Casos de uso:
     * - Cliente olvidó contraseña de email anterior y no puede recuperarlo
     * - Corrección administrativa de emails incorrectos
     * - Cliente solicita cambio de email por soporte
     *
     * Flujo de ejecución:
     * 1. Busca el cliente por ID
     * 2. Valida que el nuevo email no esté en uso por otro usuario
     * 3. Actualiza el email en tabla USUARIOS
     * 4. Retorna el ClienteDTO actualizado
     *
     * Validaciones:
     * - Cliente debe existir
     * - Nuevo email debe ser único (no puede estar en uso)
     *
     * IMPORTANTE:
     * - Este método NO actualiza el email en Firebase Authentication
     * - El email en Firebase debe actualizarse desde el frontend usando Firebase SDK
     *
     * Restricciones de seguridad:
     * - Solo ADMIN y TRABAJADOR pueden ejecutar (validado en controller)
     *
     * Transaccionalidad:
     * - Si falla, se revierte el cambio
     *
     * @param idCliente ID del cliente a actualizar
     * @param request DTO con el nuevo email
     * @return ClienteDTO con los datos actualizados
     * @throws IllegalArgumentException si el cliente no existe o el email está en uso
     */
    public ClienteDTO actualizarEmailCliente(Long idCliente, ActualizarEmailRequest request) {
        logger.info("Actualizando email del cliente ID: {}", idCliente);

        // PASO 1: Buscar el cliente
        Cliente cliente = clienteRepository.findById(idCliente)
                .orElseThrow(() -> new IllegalArgumentException("Cliente no encontrado con ID: " + idCliente));

        // PASO 2: Validar que el nuevo email no esté en uso
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email " + request.getEmail() + " ya está en uso");
        }

        // PASO 3: Actualizar email en tabla USUARIOS
        cliente.getUsuario().setEmail(request.getEmail());
        usuarioRepository.save(cliente.getUsuario());

        logger.info("Email del cliente {} actualizado a: {}", idCliente, request.getEmail());

        // PASO 4: Retornar DTO actualizado
        return entityMapper.toDTO(cliente);
    }

    /**
     * Actualiza una dirección existente de un cliente
     *
     * Permite modificar ciudad, dirección y alias de una dirección ya registrada.
     *
     * Casos de uso:
     * - Cliente se muda a nueva dirección
     * - Corrección de errores en dirección ingresada
     * - Cambio de ciudad
     * - Actualización del alias descriptivo
     *
     * Flujo de ejecución:
     * 1. Busca la dirección por ID
     * 2. Valida que la ciudad existe
     * 3. Actualiza ciudad, dirección y alias
     * 4. Guarda cambios en la BD
     * 5. Retorna DireccionClienteDTO actualizado
     *
     * Validaciones:
     * - Dirección debe existir
     * - Ciudad debe existir en la BD
     *
     * Restricciones de seguridad:
     * - Requiere token JWT válido (validado en controller)
     * - NO tiene restricción de rol (el cliente puede actualizar sus propias direcciones)
     *
     * Transaccionalidad:
     * - Si falla, se revierten todos los cambios
     *
     * @param idDireccion ID de la dirección a actualizar
     * @param request DTO con los nuevos datos (idCiudad, direccion, alias)
     * @return DireccionClienteDTO con los datos actualizados
     * @throws IllegalArgumentException si la dirección o ciudad no existen
     */
    public DireccionClienteDTO actualizarDireccion(Long idDireccion, ActualizarDireccionRequest request) {
        logger.info("Actualizando dirección ID: {}", idDireccion);

        // PASO 1: Buscar la dirección
        DireccionCliente direccion = direccionClienteRepository.findById(idDireccion)
                .orElseThrow(() -> new IllegalArgumentException("Dirección no encontrada con ID: " + idDireccion));

        // PASO 2: Validar que la ciudad existe
        Ciudad ciudad = ciudadRepository.findById(request.getIdCiudad())
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada con ID: " + request.getIdCiudad()));

        // PASO 3: Actualizar datos
        direccion.setCiudad(ciudad);
        direccion.setDireccion(request.getDireccion());
        direccion.setAlias(request.getAlias());

        // PASO 4: Guardar cambios
        direccion = direccionClienteRepository.save(direccion);

        logger.info("Dirección {} actualizada exitosamente", idDireccion);

        // PASO 5: Retornar DTO actualizado
        return entityMapper.toDTO(direccion);
    }

    /**
     * Elimina un cliente del sistema
     *
     * Operación de eliminación permanente de un cliente.
     * SOLO accesible por ADMINISTRADORES (validado en ClienteController con @PreAuthorize("hasRole('ADMIN')")).
     *
     * Flujo de ejecución:
     * 1. Valida que el cliente existe (clienteRepository.existsById)
     * 2. Elimina el registro de la tabla CLIENTES
     *
     * Integraciones:
     * - clienteRepository.existsById(): Verifica existencia
     * - clienteRepository.deleteById(): DELETE en CLIENTES
     *
     * Validaciones:
     * - El cliente debe existir en la BD
     *
     * Transaccionalidad:
     * - Si falla, se revierte el DELETE
     * - Depende de la configuración de cascada en la entidad Cliente
     *
     * IMPORTANTE - Impacto de la eliminación:
     * - Se elimina el registro del cliente en tabla CLIENTES
     * - Comportamiento con direcciones: depende de la configuración de cascada
     *   (CascadeType.ALL eliminará todas las direcciones asociadas)
     * - Comportamiento con usuario: depende de la configuración de FK
     * - ADVERTENCIA: No valida si el cliente tiene pedidos activos
     *
     * Consideraciones de seguridad:
     * - Esta operación es permanente y no se puede deshacer
     * - Solo ADMIN tiene permisos para ejecutar esta operación
     * - Los trabajadores NO pueden eliminar clientes
     * - Los clientes NO pueden eliminarse a sí mismos
     *
     * @param idCliente ID del cliente a eliminar (clave primaria)
     * @throws IllegalArgumentException si el cliente no existe (400)
     */
    public void deleteCliente(Long idCliente) {
        // VALIDACIÓN: Verificar que el cliente existe
        if (!clienteRepository.existsById(idCliente)) {
            throw new IllegalArgumentException("Cliente no encontrado con ID: " + idCliente);
        }
        // PASO 1: Eliminar cliente de la BD
        clienteRepository.deleteById(idCliente); // DELETE en CLIENTES
        logger.info("Cliente eliminado: {}", idCliente);
    }
}
