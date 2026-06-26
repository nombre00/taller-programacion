package com.goldenburgers.gestionUsuario.repository;

import com.goldenburgers.gestionUsuario.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad Usuario - Acceso a datos de usuarios base
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla USUARIOS.
 * Usuario es la entidad base que se vincula con Firebase Authentication.
 * - La clave primaria es String (Firebase UID)
 * - Firebase UID debe ser único en Firebase y en BD local
 * - Email debe ser único en el sistema
 *
 * Características:
 * - Extiende JpaRepository<Usuario, String>: Hereda métodos CRUD estándar
 * - String: Tipo de la clave primaria (idUsuario = Firebase UID, NO Long!)
 * - Define queries personalizados usando Spring Data JPA method naming
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todos los usuarios
 * - findById(String id): Busca usuario por Firebase UID
 * - save(Usuario usuario): Guarda o actualiza usuario
 * - deleteById(String id): Elimina usuario por Firebase UID
 * - existsById(String id): Verifica existencia por Firebase UID
 * - count(): Cuenta total de usuarios
 *
 * Uso en el sistema:
 * - ClienteService: Crea usuarios al registrar clientes, valida unicidad
 * - TrabajadorService: Crea usuarios al registrar trabajadores, valida unicidad
 * - Spring Security: Valida autenticación y autorización
 *
 * Relaciones:
 * - Un Usuario puede ser Cliente (1:1)
 * - Un Usuario puede ser Trabajador (1:1)
 * - Un Usuario tiene un Rol (ManyToOne)
 */
@Repository
public interface UsuarioRepository extends JpaRepository<Usuario, String> {

    /**
     * ------------------------------------------------
     * Busca un usuario por su email
     * ------------------------------------------------
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT * FROM USUARIOS WHERE email = ?
     *
     * Uso típico:
     * - Buscar usuario por email en autenticación
     * - Validar que un email no esté en uso
     *
     * IMPORTANTE:
     * - La búsqueda es case-sensitive (debe coincidir exactamente)
     * - Email es único en el sistema (validado al crear usuario)
     * - Retorna Optional.empty() si no existe
     *
     * @param email Email del usuario a buscar
     * @return Optional<Usuario> - Contiene el usuario si existe, Optional.empty() si no existe
     */
    Optional<Usuario> findByEmail(String email);

    /**
     * -------------------------------------------------
     * Verifica si existe un usuario con el email especificado
     * -------------------------------------------------
     * 
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) > 0 FROM USUARIOS WHERE email = ?
     *
     * Uso típico:
     * - ClienteService.registerCliente(): Valida que email no exista
     * - TrabajadorService.registerTrabajador(): Valida que email no exista
     * - Evitar duplicados antes de crear usuario
     *
     * Ventaja sobre findByEmail():
     * - Más eficiente cuando solo necesitas verificar existencia
     * - No carga la entidad completa, solo cuenta
     *
     * @param email Email del usuario a verificar
     * @return true si existe un usuario con ese email, false si no existe
     */
    boolean existsByEmail(String email);

    /**
     * Verifica si existe un usuario con el Firebase UID especificado
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) > 0 FROM USUARIOS WHERE id_usuario = ?
     *
     * Uso típico:
     * - ClienteService.registerCliente(): Valida que Firebase UID no exista
     * - TrabajadorService.registerTrabajador(): Valida que Firebase UID no exista
     * - Evitar duplicados antes de crear usuario
     *
     * IMPORTANTE:
     * - El idUsuario es el Firebase UID obtenido de Firebase Authentication
     * - Debe ser único en el sistema (un Firebase UID = un usuario local)
     * - Esta validación evita registrar el mismo Firebase UID múltiples veces
     *
     * @param idUsuario Firebase UID del usuario a verificar
     * @return true si existe un usuario con ese Firebase UID, false si no existe
     */
    boolean existsByIdUsuario(String idUsuario);
}
