package com.goldenburgers.gestionUsuario.repository;

import com.goldenburgers.gestionUsuario.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * Repository para la entidad Rol - Acceso a datos de roles del sistema
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla ROLES.
 * Los roles son datos maestros que definen permisos y accesos en el sistema.
 * - Los roles NO se crean/modifican desde la aplicación
 * - Son datos maestros insertados manualmente en la BD
 * - Solo operaciones de lectura en la aplicación
 *
 * Características:
 * - Extiende JpaRepository<Rol, Long>: Hereda métodos CRUD estándar
 * - Long: Tipo de la clave primaria (idRol)
 * - Define queries personalizados usando Spring Data JPA method naming
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todos los roles
 * - findById(Long id): Busca rol por ID
 * - save(Rol rol): Guarda o actualiza rol
 * - deleteById(Long id): Elimina rol por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de roles
 *
 * Uso en el sistema:
 * - RolService: Usa este repository para consultar roles
 * - ClienteService: Busca rol "Cliente" al registrar clientes
 * - TrabajadorService: Busca rol "Trabajador" al registrar trabajadores
 *
 * Datos maestros típicos:
 * - Cliente: Usuarios finales que realizan pedidos
 * - Trabajador: Personal que gestiona pedidos y clientes
 * - Admin: Administradores con acceso completo
 */
@Repository
public interface RolRepository extends JpaRepository<Rol, Long> {

    /**
     * Busca un rol por su nombre exacto
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT * FROM ROLES WHERE nombre_rol = ?
     *
     * Uso típico:
     * - ClienteService.registerCliente(): Busca rol "Cliente"
     * - TrabajadorService.registerTrabajador(): Busca rol "Trabajador"
     * - Validar que un rol existe por nombre
     *
     * IMPORTANTE:
     * - La búsqueda es case-sensitive (debe coincidir exactamente)
     * - Nombres válidos: "Cliente", "Trabajador", "Admin"
     * - Retorna Optional.empty() si no existe
     *
     * @param nombreRol Nombre exacto del rol (ej: "Cliente", "Trabajador", "Admin")
     * @return Optional<Rol> - Contiene el rol si existe, Optional.empty() si no existe
     */
    Optional<Rol> findByNombreRol(String nombreRol);

    /**
     * Verifica si existe un rol con el nombre especificado
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) > 0 FROM ROLES WHERE nombre_rol = ?
     *
     * Uso típico:
     * - Validar que un rol existe antes de realizar operaciones
     * - Verificaciones de integridad de datos
     *
     * Ventaja sobre findByNombreRol():
     * - Más eficiente cuando solo necesitas verificar existencia
     * - No carga la entidad completa, solo cuenta
     *
     * @param nombreRol Nombre del rol a verificar
     * @return true si existe un rol con ese nombre, false si no existe
     */
    boolean existsByNombreRol(String nombreRol);
}
