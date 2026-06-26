package com.goldenburgers.gestionUsuario.DTOs;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO de respuesta para Cliente
 *
 * DTO (Data Transfer Object) utilizado para retornar información completa de un cliente.
 * Combina datos del usuario base con información específica del cliente y sus direcciones.
 *
 * Uso en el sistema:
 * - Retornado por ClienteController en todas las operaciones CRUD
 * - Retornado al registrar nuevo cliente (POST /api/clientes)
 * - Retornado al consultar clientes (GET /api/clientes, GET /api/clientes/{id})
 * - Usado en módulo de pedidos para información del cliente
 *
 * Estructura de datos:
 * - Incluye información base del usuario (UsuarioDTO): Firebase UID, email, rol, fecha creación
 * - Incluye información específica del cliente: nombre, teléfono
 * - Incluye lista de direcciones de entrega (DireccionClienteDTO)
 *
 * Conversión desde entidad:
 * - EntityMapper.toDTO(Cliente): Convierte entidad JPA a este DTO
 * - Incluye conversiones anidadas:
 *   - Usuario -> UsuarioDTO (con Rol -> RolDTO)
 *   - List<DireccionCliente> -> List<DireccionClienteDTO> (con Ciudad -> CiudadDTO)
 *
 * Integración con:
 * - ClienteService: Retorna este DTO en todas sus operaciones
 * - ClienteController: Retorna este DTO en respuestas HTTP
 * - Módulo de Pedidos: Usa este DTO para información del cliente
 *
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClienteDTO {

    /**
     * ID único del cliente (clave primaria)
     *
     * Descripción:
     * - Identificador único del cliente en tabla CLIENTES
     * - Generado automáticamente por la BD (secuencia)
     * - Usado para operaciones CRUD sobre clientes
     * - Usado como FK en tabla DIRECCION_CLIENTE
     * - Usado en pedidos para identificar al cliente
     *
     * Ejemplo: 1L, 25L, 100L
     */
    private Long idCliente;

    /**
     * Información base del usuario (DTO anidado)
     *
     * Descripción:
     * - Contiene datos comunes a todos los usuarios
     * - Incluye: Firebase UID, email, rol, fecha creación
     * - Convertido desde entidad Usuario mediante EntityMapper
     * - El rol siempre será "Cliente" para este DTO
     *
     * Estructura:
     * {
     *   idUsuario: "abc123xyz456",
     *   email: "cliente@ejemplo.com",
     *   rol: {idRol: 1, nombreRol: "Cliente"},
     *   fechaCreacion: "2025-01-15"
     * }
     */
    private UsuarioDTO usuario;

    /**
     * Nombre completo del cliente
     *
     * Descripción:
     * - Nombre del cliente para identificación
     * - Se muestra en pedidos, facturas, reportes
     * - Campo obligatorio al registrar cliente
     * - Se almacena en tabla CLIENTES
     *
     * Ejemplo: "Juan Pérez González"
     */
    private String nombreCliente;

    /**
     * Número de teléfono del cliente (opcional)
     *
     * Descripción:
     * - Teléfono de contacto del cliente
     * - Usado para comunicación sobre pedidos
     * - Formato chileno: 9 dígitos sin código de país
     * - Puede ser null si no se proporcionó al registrar
     *
     * Formato: "912345678" (9 dígitos)
     * Ejemplo: "912345678", "987654321"
     */
    private String telefonoCliente;

    /**
     * Lista de direcciones de entrega del cliente
     *
     * Descripción:
     * - Todas las direcciones asociadas al cliente
     * - Convertidas desde List<DireccionCliente> mediante EntityMapper
     * - Cada dirección incluye información completa de ciudad
     * - Un cliente puede tener 0 o más direcciones
     * - Se usa al hacer pedidos para seleccionar dirección de entrega
     *
     * Estructura de cada elemento:
     * {
     *   idDireccion: 1,
     *   idCliente: 1,
     *   ciudad: {idCiudad: 1, nombreCiudad: "Santiago"},
     *   direccion: "Av. Principal 123",
     *   alias: "Casa"
     * }
     */
    private List<DireccionClienteDTO> direcciones;
}
