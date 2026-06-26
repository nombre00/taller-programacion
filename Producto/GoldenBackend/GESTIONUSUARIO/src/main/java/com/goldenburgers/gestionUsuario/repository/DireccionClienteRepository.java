package com.goldenburgers.gestionUsuario.repository;


import com.goldenburgers.gestionUsuario.model.DireccionCliente;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository para la entidad DireccionCliente - Acceso a direcciones de entrega
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla DIRECCION_CLIENTE.
 * Maneja direcciones de entrega asociadas a clientes.
 * - Un cliente puede tener 0 o más direcciones
 *
 * Características:
 * - Extiende JpaRepository<DireccionCliente, Long>: Hereda métodos CRUD estándar
 * - Long: Tipo de la clave primaria (idDireccion)
 * - Define queries personalizados para consultar direcciones por cliente y ciudad
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todas las direcciones (poco útil, mejor filtrar por cliente)
 * - findById(Long id): Busca dirección por ID
 * - save(DireccionCliente direccion): Guarda o actualiza dirección
 * - deleteById(Long id): Elimina dirección por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de direcciones (poco útil, mejor contar por cliente)
 *
 * Uso en el sistema:
 * - ClienteService: Gestiona direcciones de clientes (crear, listar, eliminar)
 * - Módulo de Pedidos: Consulta direcciones al crear pedidos
 * - Frontend: Muestra lista de direcciones para seleccionar en pedidos
 *
 * Relaciones:
 * - DireccionCliente -> Cliente (ManyToOne): Una dirección pertenece a un cliente
 * - DireccionCliente -> Ciudad (ManyToOne): Una dirección está en una ciudad
 *
 * IMPORTANTE:
 */
@Repository
public interface DireccionClienteRepository extends JpaRepository<DireccionCliente, Long> {

    /**
     * Busca todas las direcciones de un cliente específico
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT * FROM DIRECCION_CLIENTE WHERE id_cliente = ?
     *
     * Navegación de relación:
     * - "Cliente" es la relación en entidad DireccionCliente
     * - "IdCliente" es el campo en entidad Cliente
     * - Spring Data JPA genera el query automáticamente
     *
     * Uso típico:
     * - ClienteService.getDireccionesByCliente(): Obtener todas las direcciones de un cliente
     * - Mostrar lista de direcciones al hacer un pedido
     * - Gestionar direcciones del cliente en su perfil
     *
     * IMPORTANTE:
     * - Retorna lista vacía si el cliente no tiene direcciones
     * - NO valida si el cliente existe (retorna vacío si idCliente no existe)
     * - Incluye información completa de ciudad por la relación ManyToOne
     *
     * @param idCliente ID del cliente del cual obtener las direcciones
     * @return List<DireccionCliente> - Lista de direcciones (puede ser vacía)
     */
    List<DireccionCliente> findByClienteIdCliente(Long idCliente);

    /**
     * Busca direcciones de un cliente filtradas por ciudad
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT * FROM DIRECCION_CLIENTE WHERE id_cliente = ? AND id_ciudad = ?
     *
     * Navegación de relaciones:
     * - "Cliente.IdCliente" navega a través de relación Cliente
     * - "Ciudad.IdCiudad" navega a través de relación Ciudad
     * - "And" combina ambas condiciones (WHERE ... AND ...)
     *
     * Uso típico:
     * - Filtrar direcciones por zona de entrega
     * - Mostrar solo direcciones en ciudades con cobertura
     * - Validar si cliente tiene direcciones en ciudad específica
     *
     * IMPORTANTE:
     * - Retorna lista vacía si no hay coincidencias
     * - Útil para validar cobertura de entrega por ciudad
     * - Un cliente puede tener múltiples direcciones en la misma ciudad
     *
     * @param idCliente ID del cliente
     * @param idCiudad ID de la ciudad a filtrar
     * @return List<DireccionCliente> - Lista de direcciones en esa ciudad (puede ser vacía)
     */
    List<DireccionCliente> findByClienteIdClienteAndCiudadIdCiudad(Long idCliente, Long idCiudad);

    /**
     * Cuenta cuántas direcciones tiene un cliente
     *
     * Query method de Spring Data JPA que genera automáticamente:
     * SELECT COUNT(*) FROM DIRECCION_CLIENTE WHERE id_cliente = ?
     *
     * Uso típico:
     * - Validar si cliente tiene direcciones registradas
     * - Mostrar cantidad de direcciones en perfil del cliente
     * - Estadísticas de direcciones por cliente
     *
     * Ventaja sobre findByClienteIdCliente().size():
     * - Más eficiente: solo cuenta en BD, no carga todas las entidades
     * - No consume memoria cargando direcciones completas
     * - Mejor performance para verificaciones simples
     *
     * @param idCliente ID del cliente
     * @return long - Número de direcciones del cliente (0 si no tiene ninguna)
     */
    long countByClienteIdCliente(Long idCliente);
}
