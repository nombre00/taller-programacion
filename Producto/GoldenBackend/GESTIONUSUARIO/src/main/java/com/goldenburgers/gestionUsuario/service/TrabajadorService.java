package com.goldenburgers.gestionUsuario.service;

import com.goldenburgers.gestionUsuario.DTOs.ActualizarEmailRequest;
import com.goldenburgers.gestionUsuario.DTOs.ActualizarRolRequest;
import com.goldenburgers.gestionUsuario.DTOs.RegistrarTrabajador;
import com.goldenburgers.gestionUsuario.DTOs.TrabajadorDTO;
import com.goldenburgers.gestionUsuario.model.Rol;
import com.goldenburgers.gestionUsuario.model.Trabajador;
import com.goldenburgers.gestionUsuario.model.Usuario;
import com.goldenburgers.gestionUsuario.repository.RolRepository;
import com.goldenburgers.gestionUsuario.repository.TrabajadorRepository;
import com.goldenburgers.gestionUsuario.repository.UsuarioRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * Servicio de negocio para la gestión de Trabajadores
 *
 * Capa de servicio que contiene toda la lógica de negocio relacionada con trabajadores.
 * Los trabajadores son usuarios con permisos privilegiados para gestionar pedidos y clientes.
 *
 * Integraciones:
 * - TrabajadorRepository: CRUD de trabajadores
 * - UsuarioRepository: Gestión de usuarios (tabla base para autenticación)
 * - RolRepository: Obtención del rol "Trabajador"
 * - EntityMapper: Conversión entre entidades JPA y DTOs
 *
 * Transaccionalidad:
 * - @Transactional a nivel de clase: TODAS las operaciones son transaccionales
 * - Si ocurre un error, se hace rollback automático de todos los cambios
 * - Garantiza consistencia de datos (ej: si falla crear trabajador, también revierte crear usuario)
 *
 * Validaciones especiales:
 * - RUT: Debe tener formato chileno (ej: 12.345.678-9)
 * - RUT único: No pueden existir dos trabajadores con el mismo RUT
 * - Firebase UID único: No pueden existir dos usuarios con el mismo idUsuario
 * - Email único: No pueden existir dos usuarios con el mismo email
 *
 * Restricciones de seguridad (aplicadas en TrabajadorController):
 * - CREAR trabajador: Solo ADMIN (@PreAuthorize("hasRole('ADMIN')"))
 * - LEER trabajadores: ADMIN y TRABAJADOR (@PreAuthorize("hasAnyRole('ADMIN', 'TRABAJADOR')"))
 * - ACTUALIZAR trabajador: Solo ADMIN (@PreAuthorize("hasRole('ADMIN')"))
 * - ELIMINAR trabajador: Solo ADMIN (@PreAuthorize("hasRole('ADMIN')"))
 *
 * Llamado desde:
 * - TrabajadorController: para procesar las peticiones HTTP
 */
@Service
@Transactional // TODAS las operaciones de este servicio son transaccionales
public class TrabajadorService {

    private static final Logger logger = LoggerFactory.getLogger(TrabajadorService.class);

    // Repository para operaciones CRUD de trabajadores en la tabla TRABAJADORES
    @Autowired
    private TrabajadorRepository trabajadorRepository;

    // Repository para gestionar la tabla USUARIOS (relación con Firebase Auth)
    @Autowired
    private UsuarioRepository usuarioRepository;

    // Repository para obtener roles del sistema (Cliente, Trabajador, Admin)
    @Autowired
    private RolRepository rolRepository;

    // Mapper para convertir entidades JPA a DTOs (y viceversa)
    @Autowired
    private EntityMapper entityMapper;

    /**
     * Registra un nuevo trabajador en el sistema
     *
     * Este método coordina la creación de un usuario y su trabajador asociado en una transacción.
     * SOLO accesible por ADMINISTRADORES (validado en TrabajadorController con @PreAuthorize("hasRole('ADMIN')")).
     *
     * Flujo de ejecución:
     * 1. Valida que el Firebase UID no exista (usuarioRepository.existsByIdUsuario)
     * 2. Valida que el email no esté registrado (usuarioRepository.existsByEmail)
     * 3. Valida que el RUT no exista (trabajadorRepository.existsByRutTrabajador)
     * 4. Obtiene el rol "Trabajador" desde la BD (rolRepository.findByNombreRol)
     * 5. Crea registro en tabla USUARIOS con:
     *    - idUsuario = Firebase UID
     *    - email
     *    - rol = "Trabajador"
     *    - fechaCreacion = fecha actual
     * 6. Crea registro en tabla TRABAJADORES con:
     *    - usuario = usuario recién creado (FK)
     *    - nombreTrabajador
     *    - rutTrabajador (formato rut chileno)
     * 7. Convierte la entidad Trabajador a TrabajadorDTO usando EntityMapper
     *
     * Validaciones:
     * - Firebase UID único (no puede estar registrado)
     * - Email único (no puede estar en uso)
     * - RUT único (no puede estar registrado por otro trabajador)
     * - RUT formato (ej: 12.345.678-9) - validado en RegisterTrabajadorRequest
     * - Rol "Trabajador" debe existir en la BD
     *
     * Transaccionalidad:
     * - Si falla algún paso, se hace rollback de Todo (usuario y trabajador)
     * - Garantiza que no queden registros huérfanos
     *
     * Restricciones de seguridad:
     * - Solo ADMIN puede crear trabajadores (validado en controller)
     * - Los trabajadores NO pueden crear otros trabajadores
     * - Los clientes NO tienen acceso
     *
     * @param request DTO con datos del trabajador (idUsuario, email, nombreTrabajador, rutTrabajador)
     * @return TrabajadorDTO con los datos del trabajador creado (incluye IDs generados)
     * @throws IllegalArgumentException si el usuario, email o RUT ya existe (400)
     * @throws IllegalStateException si el rol "Trabajador" no existe en BD (409)
     */
    public TrabajadorDTO registerTrabajador(RegistrarTrabajador request) {
        logger.info("Registrando nuevo trabajador con email: {}", request.getEmail());

        // VALIDACIÓN 1: Verificar que el Firebase UID no esté registrado
        if (usuarioRepository.existsByIdUsuario(request.getIdUsuario())) {
            throw new IllegalArgumentException("El usuario con ID " + request.getIdUsuario() + " ya está registrado");
        }

        // VALIDACIÓN 2: Verificar que el email no esté en uso
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email " + request.getEmail() + " ya está registrado");
        }

        // VALIDACIÓN 3: Verificar que el RUT no exista (único para trabajadores)
        if (trabajadorRepository.existsByRutTrabajador(request.getRutTrabajador())) {
            throw new IllegalArgumentException("El RUT " + request.getRutTrabajador() + " ya está registrado");
        }

        // PASO 1: Obtener el rol "Trabajador" de la BD (debe existir como dato maestro)
        Rol rolTrabajador = rolRepository.findByNombreRol("Trabajador")
                .orElseThrow(() -> new IllegalStateException("Rol 'Trabajador' no encontrado en la base de datos"));

        // PASO 2: Crear registro en tabla USUARIOS (vinculado a Firebase Auth)
        Usuario usuario = new Usuario();
        usuario.setIdUsuario(request.getIdUsuario()); // Firebase UID
        usuario.setEmail(request.getEmail());
        usuario.setRol(rolTrabajador); // FK a tabla ROLES
        usuario.setFechaCreacion(LocalDate.now());
        usuario = usuarioRepository.save(usuario); // INSERT en USUARIOS

        // PASO 3: Crear registro en tabla TRABAJADORES (vinculado al usuario)
        Trabajador trabajador = new Trabajador();
        trabajador.setUsuario(usuario); // FK a tabla USUARIOS
        trabajador.setNombreTrabajador(request.getNombreTrabajador());
        trabajador.setRutTrabajador(request.getRutTrabajador()); // Formato RUT chileno
        trabajador = trabajadorRepository.save(trabajador); // INSERT en TRABAJADORES

        logger.info("Trabajador registrado exitosamente con ID: {}", trabajador.getIdTrabajador());

        // PASO 4: Convertir entidad JPA a DTO para retornar al controller
        return entityMapper.toDTO(trabajador);
    }

    /**
     * Obtiene un trabajador por su ID de base de datos
     *
     * Busca un trabajador por su clave primaria (idTrabajador).
     *
     * Integraciones:
     * - trabajadorRepository.findById(): Busca en tabla TRABAJADORES
     * - EntityMapper.toDTO(): Convierte entidad JPA a DTO
     *
     * Restricciones de seguridad:
     * - ADMIN y TRABAJADOR pueden acceder (validado en controller)
     *
     * @param idTrabajador ID del trabajador (clave primaria)
     * @return TrabajadorDTO con los datos del trabajador
     * @throws IllegalArgumentException si el trabajador no existe (manejado como 400 por GlobalExceptionHandler)
     */
    public TrabajadorDTO getTrabajadorById(Long idTrabajador) {
        // Busca el trabajador, si no existe lanza excepción
        Trabajador trabajador = trabajadorRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));
        return entityMapper.toDTO(trabajador);
    }

    /**
     * Obtiene un trabajador por su Firebase UID
     *
     * Busca un trabajador a través del ID de usuario de Firebase Authentication.
     * Útil para obtener los datos del trabajador autenticado actualmente.
     *
     * Integraciones:
     * - trabajadorRepository.findByUsuarioIdUsuario(): JOIN entre TRABAJADORES y USUARIOS
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Query generado (aproximado):
     * SELECT t.* FROM TRABAJADORES t
     * JOIN USUARIOS u ON t.id_usuario = u.id_usuario
     * WHERE u.id_usuario = ?
     *
     * Restricciones de seguridad:
     * - ADMIN y TRABAJADOR pueden acceder (validado en controller)
     *
     * Uso típico:
     * - Al iniciar sesión, obtener datos del trabajador con su Firebase UID
     * - Verificar información del usuario autenticado
     *
     * @param idUsuario Firebase UID del usuario
     * @return TrabajadorDTO con los datos del trabajador
     * @throws IllegalArgumentException si no existe trabajador con ese Firebase UID
     */
    public TrabajadorDTO getTrabajadorByUsuarioId(String idUsuario) {
        // Busca trabajador por relación con tabla USUARIOS
        Trabajador trabajador = trabajadorRepository.findByUsuarioIdUsuario(idUsuario)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado para usuario: " + idUsuario));
        return entityMapper.toDTO(trabajador);
    }

    /**
     * Obtiene un trabajador por su RUT
     *
     * Busca un trabajador por su RUT chileno (identificador único nacional).
     *
     * Integraciones:
     * - trabajadorRepository.findByRutTrabajador(): Busca en tabla TRABAJADORES
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Formato de RUT esperado: (ej: 12.345.678-9)
     * El RUT es único por trabajador (validado en registro y actualización).
     *
     * Restricciones de seguridad:
     * - ADMIN y TRABAJADOR pueden acceder (validado en controller)
     *
     * Uso típico:
     * - Búsqueda de trabajadores por identificación nacional
     * - Validaciones administrativas
     *
     * @param rutTrabajador RUT del trabajador 
     * @return TrabajadorDTO con los datos del trabajador
     * @throws IllegalArgumentException si no existe trabajador con ese RUT
     */
    public TrabajadorDTO getTrabajadorByRut(String rutTrabajador) {
        // Busca trabajador por RUT (identificador único)
        Trabajador trabajador = trabajadorRepository.findByRutTrabajador(rutTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con RUT: " + rutTrabajador));
        return entityMapper.toDTO(trabajador);
    }

    /**
     * Lista todos los trabajadores registrados en el sistema
     *
     * Obtiene la lista completa de trabajadores sin paginación.
     * Usado por ADMIN y TRABAJADOR para ver todos los trabajadores del sistema.
     *
     * Integraciones:
     * - trabajadorRepository.findAll(): SELECT * FROM TRABAJADORES
     * - EntityMapper.toTrabajadorDTOList(): Convierte lista de entidades a lista de DTOs
     *
     * Restricciones de seguridad:
     * - ADMIN y TRABAJADOR pueden acceder (validado en controller)
     * - Los clientes NO pueden ver la lista de trabajadores
     *
     * Uso típico:
     * - Panel de administración para gestionar trabajadores
     * - Asignación de pedidos a trabajadores
     * - Coordinación entre trabajadores
     *
     * NOTA: En un sistema con muchos trabajadores, considerar implementar paginación.
     *
     * @return List<TrabajadorDTO> con todos los trabajadores (puede ser lista vacía)
     */
    public List<TrabajadorDTO> getAllTrabajadores() {
        // Obtiene todos los trabajadores de la BD (sin filtros)
        List<Trabajador> trabajadores = trabajadorRepository.findAll();
        // Convierte lista de entidades a lista de DTOs
        return entityMapper.toTrabajadorDTOList(trabajadores);
    }

    /**
     * Actualiza los datos de un trabajador existente
     *
     * Permite modificar nombre y RUT de un trabajador.
     * NO modifica el usuario asociado (Firebase UID, email, rol).
     * SOLO accesible por ADMINISTRADORES (validado en TrabajadorController con @PreAuthorize("hasRole('ADMIN')")).
     *
     * Flujo de ejecución:
     * 1. Busca el trabajador por ID (trabajadorRepository.findById)
     * 2. Si el RUT cambió, valida que el nuevo RUT no esté en uso (trabajadorRepository.existsByRutTrabajador)
     * 3. Actualiza nombre y RUT
     * 4. Guarda cambios en la BD (trabajadorRepository.save)
     * 5. Convierte resultado a TrabajadorDTO (EntityMapper.toDTO)
     *
     * Integraciones:
     * - trabajadorRepository.findById(): Busca el trabajador
     * - trabajadorRepository.existsByRutTrabajador(): Valida RUT único
     * - trabajadorRepository.save(): UPDATE en tabla TRABAJADORES
     * - EntityMapper.toDTO(): Convierte resultado a DTO
     *
     * Validaciones:
     * - Trabajador debe existir
     * - Si se cambia el RUT, el nuevo RUT no puede estar en uso por otro trabajador
     * - Si el RUT es el mismo, no valida duplicados (permite mantener el RUT actual)
     * - RUT formato (ej: 12.345.678-9) - validado en controller al recibir datos
     *
     * Transaccionalidad:
     * - Si falla, se revierte el UPDATE
     *
     * Restricciones de seguridad:
     * - Solo ADMIN puede actualizar trabajadores (validado en controller)
     * - Los trabajadores NO pueden modificar sus propios datos ni los de otros
     *
     * @param idTrabajador ID del trabajador a actualizar
     * @param nombreTrabajador Nuevo nombre
     * @param rutTrabajador Nuevo RUT 
     * @return TrabajadorDTO con los datos actualizados
     * @throws IllegalArgumentException si el trabajador no existe o el RUT está duplicado
     */
    public TrabajadorDTO updateTrabajador(Long idTrabajador, String nombreTrabajador, String rutTrabajador) {
        // PASO 1: Busca el trabajador, si no existe lanza excepción
        Trabajador trabajador = trabajadorRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));

        // VALIDACIÓN: Si el RUT cambió, verificar que no esté en uso por otro trabajador
        if (!trabajador.getRutTrabajador().equals(rutTrabajador) &&
                trabajadorRepository.existsByRutTrabajador(rutTrabajador)) {
            throw new IllegalArgumentException("El RUT " + rutTrabajador + " ya está registrado");
        }

        // PASO 2: Actualizar solo los campos permitidos
        trabajador.setNombreTrabajador(nombreTrabajador);
        trabajador.setRutTrabajador(rutTrabajador);
        trabajador = trabajadorRepository.save(trabajador); // UPDATE en TRABAJADORES

        logger.info("Trabajador actualizado: {}", idTrabajador);
        return entityMapper.toDTO(trabajador);
    }

    /**
     * Actualiza el email de un trabajador (Solo Admin)
     *
     * Permite a ADMIN modificar el email de un trabajador.
     * Útil para correcciones administrativas o cuando el trabajador no puede acceder a su email.
     *
     * Casos de uso:
     * - Trabajador olvidó contraseña de email anterior
     * - Corrección administrativa de emails incorrectos
     * - Actualización de emails corporativos
     *
     * Flujo de ejecución:
     * 1. Busca el trabajador por ID
     * 2. Valida que el nuevo email no esté en uso por otro usuario
     * 3. Actualiza el email en tabla USUARIOS
     * 4. Retorna el TrabajadorDTO actualizado
     *
     * Validaciones:
     * - Trabajador debe existir
     * - Nuevo email debe ser único (no puede estar en uso)
     *
     * IMPORTANTE:
     * - Este método NO actualiza el email en Firebase Authentication
     * - El email en Firebase debe actualizarse desde el frontend usando Firebase SDK
     *
     * Restricciones de seguridad:
     * - Solo ADMIN puede ejecutar (validado en controller)
     *
     * Transaccionalidad:
     * - Si falla, se revierte el cambio
     *
     * @param idTrabajador ID del trabajador a actualizar
     * @param request DTO con el nuevo email
     * @return TrabajadorDTO con los datos actualizados
     * @throws IllegalArgumentException si el trabajador no existe o el email está en uso
     */
    public TrabajadorDTO actualizarEmailTrabajador(Long idTrabajador, ActualizarEmailRequest request) {
        logger.info("Actualizando email del trabajador ID: {}", idTrabajador);

        // PASO 1: Buscar el trabajador
        Trabajador trabajador = trabajadorRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));

        // PASO 2: Validar que el nuevo email no esté en uso
        if (usuarioRepository.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException("El email " + request.getEmail() + " ya está en uso");
        }

        // PASO 3: Actualizar email en tabla USUARIOS
        trabajador.getUsuario().setEmail(request.getEmail());
        usuarioRepository.save(trabajador.getUsuario());

        logger.info("Email del trabajador {} actualizado a: {}", idTrabajador, request.getEmail());

        // PASO 4: Retornar DTO actualizado
        return entityMapper.toDTO(trabajador);
    }

    /**
     * Cambia el rol de un trabajador (Solo Admin)
     *
     * Permite a ADMIN promover un trabajador a ADMIN o degradar un ADMIN a trabajador.
     *
     * Casos de uso:
     * - Promover trabajador experimentado a administrador
     * - Degradar administrador que ya no requiere permisos elevados
     * - Ajustes organizacionales en permisos
     *
     * Flujo de ejecución:
     * 1. Busca el trabajador por ID
     * 2. Valida que el nuevo rol existe
     * 3. Valida que el rol sea Trabajador (2) o Admin (3)
     * 4. Actualiza el rol en tabla USUARIOS
     * 5. Retorna el TrabajadorDTO actualizado
     *
     * Validaciones:
     * - Trabajador debe existir
     * - Rol debe existir en la BD
     * - Rol debe ser Trabajador (2) o Admin (3), NO Cliente (1)
     *
     * Restricciones:
     * - NO se puede cambiar a rol Cliente (requiere crear cliente por separado)
     * - Solo ADMIN puede ejecutar (validado en controller)
     *
     * Transaccionalidad:
     * - Si falla, se revierte el cambio
     *
     * @param idTrabajador ID del trabajador a actualizar
     * @param request DTO con el ID del nuevo rol
     * @return TrabajadorDTO con los datos actualizados
     * @throws IllegalArgumentException si el trabajador, rol no existe o rol es inválido
     */
    public TrabajadorDTO cambiarRolTrabajador(Long idTrabajador, ActualizarRolRequest request) {
        logger.info("Cambiando rol del trabajador ID: {} a rol ID: {}", idTrabajador, request.getIdRol());

        // PASO 1: Buscar el trabajador
        Trabajador trabajador = trabajadorRepository.findById(idTrabajador)
                .orElseThrow(() -> new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador));

        // PASO 2: Buscar el nuevo rol
        Rol nuevoRol = rolRepository.findById(request.getIdRol())
                .orElseThrow(() -> new IllegalArgumentException("Rol no encontrado con ID: " + request.getIdRol()));

        // VALIDACIÓN: Solo permitir roles Trabajador o Admin
        if (!nuevoRol.getNombreRol().equals("Trabajador") && !nuevoRol.getNombreRol().equals("Admin")) {
            throw new IllegalArgumentException("Solo se puede cambiar a rol Trabajador o Admin. No se permite rol Cliente.");
        }

        // PASO 3: Actualizar rol en tabla USUARIOS
        trabajador.getUsuario().setRol(nuevoRol);
        usuarioRepository.save(trabajador.getUsuario());

        logger.info("Rol del trabajador {} actualizado a: {}", idTrabajador, nuevoRol.getNombreRol());

        // PASO 4: Retornar DTO actualizado
        return entityMapper.toDTO(trabajador);
    }

    /**
     * Elimina un trabajador del sistema
     *
     * Operación de eliminación permanente de un trabajador.
     * SOLO accesible por ADMINISTRADORES (validado en TrabajadorController con @PreAuthorize("hasRole('ADMIN')")).
     *
     * Flujo de ejecución:
     * 1. Valida que el trabajador existe (trabajadorRepository.existsById)
     * 2. Elimina el registro de la tabla TRABAJADORES
     *
     * Integraciones:
     * - trabajadorRepository.existsById(): Verifica existencia
     * - trabajadorRepository.deleteById(): DELETE en TRABAJADORES
     *
     * Validaciones:
     * - El trabajador debe existir en la BD
     *
     * Transaccionalidad:
     * - Si falla, se revierte el DELETE
     * - Depende de la configuración de cascada en la entidad Trabajador
     *
     * IMPORTANTE - Impacto de la eliminación:
     * - Se elimina el registro del trabajador en tabla TRABAJADORES
     * - Comportamiento con usuario: depende de la configuración de FK
     * - ADVERTENCIA: No valida si el trabajador tiene pedidos asignados
     *
     * Consideraciones de seguridad:
     * - Esta operación es permanente y no se puede deshacer
     * - Solo ADMIN tiene permisos para ejecutar esta operación
     * - Los trabajadores NO pueden eliminar cuentas (ni la suya ni la de otros)
     * - Esta es la operación más sensible (solo ADMIN)
     *
     * @param idTrabajador ID del trabajador a eliminar (clave primaria)
     * @throws IllegalArgumentException si el trabajador no existe (400)
     */
    public void deleteTrabajador(Long idTrabajador) {
        // VALIDACIÓN: Verificar que el trabajador existe
        if (!trabajadorRepository.existsById(idTrabajador)) {
            throw new IllegalArgumentException("Trabajador no encontrado con ID: " + idTrabajador);
        }
        // PASO 1: Eliminar trabajador de la BD
        trabajadorRepository.deleteById(idTrabajador); // DELETE en TRABAJADORES
        logger.info("Trabajador eliminado: {}", idTrabajador);
    }
}
