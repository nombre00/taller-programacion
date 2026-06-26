package com.goldenburgers.gestionUsuario.repository;

import com.goldenburgers.gestionUsuario.model.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad Cliente - Acceso a datos de clientes
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla CLIENTES.
 * Maneja clientes que son usuarios finales que realizan pedidos.
 * - Los queries navegan la relación con Usuario usando punto (.)
 * - Ejemplo: findByUsuarioIdUsuario -> SELECT * FROM CLIENTES c JOIN USUARIOS u WHERE u.id_usuario = ?
 *
 * Características:
 * - Extiende JpaRepository<Cliente, Long>: Hereda métodos CRUD estándar
 * - Long: Tipo de la clave primaria (idCliente)
 * - Define queries personalizados que navegan relaciones (usuario.idUsuario, usuario.email)
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todos los clientes
 * - findById(Long id): Busca cliente por ID
 * - save(Cliente cliente): Guarda o actualiza cliente
 * - deleteById(Long id): Elimina cliente por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de clientes
 *
 * Uso en el sistema:
 * - ClienteService: Usa este repository para operaciones CRUD de clientes
 * - Módulo de Pedidos: Consulta clientes al crear pedidos
 *
 * Relaciones:
 * - Cliente -> Usuario (ManyToOne): Un cliente tiene un usuario base
 * - Cliente -> DireccionCliente (OneToMany): Un cliente tiene múltiples direcciones
 */
@Repository
public interface ClienteRepository extends JpaRepository<Cliente, Long> {

    /**
     * Busca un cliente por el Firebase UID del usuario
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT c.* FROM CLIENTES c
     * JOIN USUARIOS u ON c.id_usuario = u.id_usuario
     * WHERE u.id_usuario = ?
     *
     * Navegación de relación:
     * - "Usuario" es la relación en entidad Cliente
     * - "IdUsuario" es el campo en entidad Usuario
     * - Spring Data JPA genera el JOIN automáticamente
     *
     * Uso típico:
     * - ClienteService.getClienteByUsuarioId(): Obtener datos del cliente autenticado
     * - Buscar cliente usando Firebase UID del token JWT
     *
     * IMPORTANTE:
     * - Útil cuando tienes el Firebase UID pero no el idCliente
     * - El Firebase UID viene del token de autenticación
     * - Retorna Optional.empty() si no existe cliente para ese usuario
     *
     * @param idUsuario Firebase UID del usuario asociado al cliente
     * @return Optional<Cliente> - Contiene el cliente si existe, Optional.empty() si no existe
     */
    Optional<Cliente> findByUsuarioIdUsuario(String idUsuario);

    /**
     * Busca un cliente por el email del usuario
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT c.* FROM CLIENTES c
     * JOIN USUARIOS u ON c.id_usuario = u.id_usuario
     * WHERE u.email = ?
     *
     * Navegación de relación:
     * - "Usuario" es la relación en entidad Cliente
     * - "Email" es el campo en entidad Usuario
     * - Spring Data JPA genera el JOIN automáticamente
     *
     * Uso típico:
     * - ClienteService.getClienteByEmail(): Buscar cliente por email
     * - Búsquedas de clientes en panel de administración
     *
     * IMPORTANTE:
     * - Útil cuando tienes el email pero no el idCliente
     * - Email es único en la tabla USUARIOS
     * - Retorna Optional.empty() si no existe cliente con ese email
     *
     * @param email Email del usuario asociado al cliente
     * @return Optional<Cliente> - Contiene el cliente si existe, Optional.empty() si no existe
     */
    Optional<Cliente> findByUsuarioEmail(String email);

    /**
     * Verifica si existe un cliente para el Firebase UID especificado
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) > 0 FROM CLIENTES c
     * JOIN USUARIOS u ON c.id_usuario = u.id_usuario
     * WHERE u.id_usuario = ?
     *
     * Uso típico:
     * - Validar que un cliente existe para un usuario
     * - Verificaciones antes de operaciones sobre clientes
     *
     * Ventaja sobre findByUsuarioIdUsuario():
     * - Más eficiente cuando solo necesitas verificar existencia
     * - No carga la entidad completa con todas sus relaciones, solo cuenta
     *
     * @param idUsuario Firebase UID del usuario a verificar
     * @return true si existe un cliente para ese usuario, false si no existe
     */
    boolean existsByUsuarioIdUsuario(String idUsuario);
}
