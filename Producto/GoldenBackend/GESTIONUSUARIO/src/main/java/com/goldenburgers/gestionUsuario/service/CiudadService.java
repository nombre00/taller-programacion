package com.goldenburgers.gestionUsuario.service;

import com.goldenburgers.gestionUsuario.DTOs.CiudadDTO;
import com.goldenburgers.gestionUsuario.model.Ciudad;
import com.goldenburgers.gestionUsuario.repository.CiudadRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;


/**
 * Servicio de negocio para la gestión de Ciudades
 *
 * Servicio de SOLO LECTURA para consultar ciudades disponibles para entrega.
 * Las ciudades son datos maestros fijos en la BD (ej: Santiago, Valparaíso, etc.).
 *
 * Integraciones:
 * - CiudadRepository: Consultas de ciudades en tabla CIUDADES
 * - EntityMapper: Conversión entre entidades JPA y DTOs
 *
 * Transaccionalidad:
 * - @Transactional(readOnly = true): TODAS las operaciones son de solo lectura
 * - Optimización: readOnly = true mejora performance en consultas
 * - No permite operaciones de escritura (INSERT, UPDATE, DELETE)
 *
 * Datos maestros:
 * - Las ciudades se insertan manualmente en la BD al inicio
 * - Ciudades típicas: "Santiago", "Valparaíso", "Concepción", etc.
 * - NO se crean, actualizan o eliminan ciudades desde la aplicación
 *
 * Uso en el sistema:
 * - ClienteService.agregarDireccion(): Valida que la ciudad existe al crear direcciones
 * - Frontend: Muestra lista de ciudades disponibles en formularios
 * - DireccionCliente: Relaciona cada dirección con una ciudad
 *
 * Llamado desde:
 * - CiudadController: para procesar las peticiones HTTP
 * - ClienteService: para validar ciudades al crear direcciones
 */
@Service
@Transactional(readOnly = true) // TODAS las operaciones son de solo lectura
public class CiudadService {

    // Repository para consultas de ciudades en tabla CIUDADES
    @Autowired
    private CiudadRepository ciudadRepository;

    // Mapper para convertir entidades JPA a DTOs
    @Autowired
    private EntityMapper entityMapper;

    /**
     * Lista todas las ciudades disponibles para entrega
     *
     * Retorna todas las ciudades configuradas como datos maestros.
     * Las ciudades son fijas y se configuran al inicializar la BD.
     *
     * Integraciones:
     * - ciudadRepository.findAll(): SELECT * FROM CIUDADES
     * - EntityMapper.toDTO(): Convierte cada entidad a DTO usando streams
     *
     * Uso típico:
     * - Mostrar lista de ciudades en formulario de registro de dirección
     * - Validar ciudades disponibles para entrega
     * - Filtrar pedidos por ciudad
     *
     * Ciudades típicas retornadas:
     * - Santiago, Valparaíso, Concepción, La Serena, etc.
     * - Depende de las ciudades configuradas en la BD
     *
     * @return List<CiudadDTO> con todas las ciudades (nunca vacía, siempre hay ciudades maestras)
     */
    public List<CiudadDTO> getAllCiudades() {
        // Obtiene todas las ciudades y las convierte a DTOs usando streams
        return ciudadRepository.findAll()
                .stream()
                .map(entityMapper::toDTO) // Convierte cada Ciudad a CiudadDTO
                .collect(Collectors.toList());
    }

    /**
     * Obtiene una ciudad por su ID de base de datos
     *
     * Busca una ciudad específica por su clave primaria.
     * Útil para validar ciudades al crear direcciones.
     *
     * Integraciones:
     * - ciudadRepository.findById(): Busca en tabla CIUDADES
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Uso típico:
     * - ClienteService.agregarDireccion(): Valida que idCiudad existe
     * - Consultar detalles de una ciudad específica
     * - Validar que un ID de ciudad es válido
     *
     * @param idCiudad ID de la ciudad (clave primaria)
     * @return CiudadDTO con los datos de la ciudad
     * @throws IllegalArgumentException si la ciudad no existe (manejado como 400 por GlobalExceptionHandler)
     */
    public CiudadDTO getCiudadById(Long idCiudad) {
        // Busca la ciudad, si no existe lanza excepción
        Ciudad ciudad = ciudadRepository.findById(idCiudad)
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada con ID: " + idCiudad));
        return entityMapper.toDTO(ciudad);
    }

    /**
     * Obtiene una ciudad por su nombre
     *
     * Busca una ciudad por su nombre exacto (case-sensitive).
     * Útil para búsquedas y validaciones por nombre.
     *
     * Integraciones:
     * - ciudadRepository.findByNombreCiudad(): Busca en tabla CIUDADES por nombre
     * - EntityMapper.toDTO(): Convierte entidad a DTO
     *
     * Uso típico:
     * - Buscar ciudad por nombre en filtros
     * - Validar que un nombre de ciudad existe
     * - Autocompletado de ciudades en formularios
     *
     * IMPORTANTE:
     * - El nombre debe coincidir exactamente (case-sensitive)
     * - Nombres válidos: según ciudades configuradas en datos maestros
     *
     * @param nombreCiudad Nombre exacto de la ciudad (ej: "Santiago", "Valparaíso")
     * @return CiudadDTO con los datos de la ciudad
     * @throws IllegalArgumentException si la ciudad no existe (manejado como 400 por GlobalExceptionHandler)
     */
    public CiudadDTO getCiudadByNombre(String nombreCiudad) {
        // Busca la ciudad por nombre exacto (case-sensitive)
        Ciudad ciudad = ciudadRepository.findByNombreCiudad(nombreCiudad)
                .orElseThrow(() -> new IllegalArgumentException("Ciudad no encontrada: " + nombreCiudad));
        return entityMapper.toDTO(ciudad);
    }
}

