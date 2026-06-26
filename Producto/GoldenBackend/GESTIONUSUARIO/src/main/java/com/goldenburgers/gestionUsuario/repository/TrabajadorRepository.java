package com.goldenburgers.gestionUsuario.repository;

import com.goldenburgers.gestionUsuario.model.Trabajador;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad Trabajador - Acceso a datos de trabajadores
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla TRABAJADORES.
 * Maneja trabajadores que son personal con permisos para gestionar pedidos y clientes.
 * - RUT es único por trabajador (validado al crear/actualizar)
 * - Formato RUT: ej: 12.345.678-9
 * - Solo ADMIN puede crear/modificar/eliminar trabajadores
 *
 * Características:
 * - Extiende JpaRepository<Trabajador, Long>: Hereda métodos CRUD estándar
 * - Long: Tipo de la clave primaria (idTrabajador)
 * - Define queries personalizados incluyendo búsqueda por RUT
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todos los trabajadores
 * - findById(Long id): Busca trabajador por ID
 * - save(Trabajador trabajador): Guarda o actualiza trabajador
 * - deleteById(Long id): Elimina trabajador por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de trabajadores
 *
 * Uso en el sistema:
 * - TrabajadorService: Usa este repository para operaciones CRUD de trabajadores
 * - Asignación de pedidos: Consulta trabajadores disponibles
 * - Reportes: Obtiene información de trabajadores
 *
 * Relaciones:
 * - Trabajador -> Usuario (ManyToOne): Un trabajador tiene un usuario base
 */
@Repository
public interface TrabajadorRepository extends JpaRepository<Trabajador, Long> {

    /**
     * Busca un trabajador por el Firebase UID del usuario
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT t.* FROM TRABAJADORES t
     * JOIN USUARIOS u ON t.id_usuario = u.id_usuario
     * WHERE u.id_usuario = ?
     *
     * Navegación de relación:
     * - "Usuario" es la relación en entidad Trabajador
     * - "IdUsuario" es el campo en entidad Usuario
     * - Spring Data JPA genera el JOIN automáticamente
     *
     * Uso típico:
     * - TrabajadorService.getTrabajadorByUsuarioId(): Obtener datos del trabajador autenticado
     * - Buscar trabajador usando Firebase UID del token JWT
     *
     * IMPORTANTE:
     * - Útil cuando tienes el Firebase UID pero no el idTrabajador
     * - El Firebase UID viene del token de autenticación
     * - Retorna Optional.empty() si no existe trabajador para ese usuario
     *
     * @param idUsuario Firebase UID del usuario asociado al trabajador
     * @return Optional<Trabajador> - Contiene el trabajador si existe, Optional.empty() si no existe
     */
    Optional<Trabajador> findByUsuarioIdUsuario(String idUsuario);

    /**
     * Busca un trabajador por su RUT chileno
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT * FROM TRABAJADORES WHERE rut_trabajador = ?
     *
     * Uso típico:
     * - TrabajadorService.getTrabajadorByRut(): Buscar trabajador por RUT
     * - Búsquedas de trabajadores en panel de administración
     * - Validaciones de identificación legal
     *
     * IMPORTANTE:
     * - RUT es único en el sistema (validado al crear/actualizar)
     * - Formato esperado: ej: 12.345.678-9
     * - La búsqueda es exacta (debe coincidir formato completo)
     * - Retorna Optional.empty() si no existe trabajador con ese RUT
     *
     * @param rutTrabajador RUT del trabajador en formato  ej: 12.345.678-9
     * @return Optional<Trabajador> - Contiene el trabajador si existe, Optional.empty() si no existe
     */
    Optional<Trabajador> findByRutTrabajador(String rutTrabajador);

    /**
     * Verifica si existe un trabajador con el RUT especificado
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) > 0 FROM TRABAJADORES WHERE rut_trabajador = ?
     *
     * Uso típico:
     * - TrabajadorService.registerTrabajador(): Valida que RUT no exista
     * - TrabajadorService.updateTrabajador(): Valida que nuevo RUT no esté en uso
     * - Evitar duplicados de RUT al crear/actualizar trabajadores
     *
     * Ventaja sobre findByRutTrabajador():
     * - Más eficiente cuando solo necesitas verificar existencia
     * - No carga la entidad completa, solo cuenta
     *
     * @param rutTrabajador RUT del trabajador a verificar
     * @return true si existe un trabajador con ese RUT, false si no existe
     */
    boolean existsByRutTrabajador(String rutTrabajador);

    /**
     * Verifica si existe un trabajador para el Firebase UID especificado
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) > 0 FROM TRABAJADORES t
     * JOIN USUARIOS u ON t.id_usuario = u.id_usuario
     * WHERE u.id_usuario = ?
     *
     * Uso típico:
     * - Validar que un trabajador existe para un usuario
     * - Verificaciones antes de operaciones sobre trabajadores
     *
     * Ventaja sobre findByUsuarioIdUsuario():
     * - Más eficiente cuando solo necesitas verificar existencia
     * - No carga la entidad completa con todas sus relaciones, solo cuenta
     *
     * @param idUsuario Firebase UID del usuario a verificar
     * @return true si existe un trabajador para ese usuario, false si no existe
     */
    boolean existsByUsuarioIdUsuario(String idUsuario);
}
