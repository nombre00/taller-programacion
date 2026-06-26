package com.goldenburgers.gestionturnos.repository;

import com.goldenburgers.gestionturnos.model.SemanaTipo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para la entidad SemanaTipo - Acceso a datos de semanas tipo
 *
 * Interface que extiende JpaRepository para operaciones CRUD sobre la tabla SEMANATIPO.
 * Las semanas tipo son plantillas atemporales que agrupan asignaciones de turno
 * por día de la semana. Se aplican al calendario con fechas concretas mediante
 * CalendarioSemana.
 *
 * Métodos heredados de JpaRepository:
 * - findAll(): Obtiene todas las semanas tipo
 * - findById(Long id): Busca semana tipo por ID
 * - save(SemanaTipo semana): Guarda o actualiza semana tipo
 * - deleteById(Long id): Elimina semana tipo por ID
 * - existsById(Long id): Verifica existencia por ID
 * - count(): Cuenta total de semanas tipo
 *
 * Uso en el sistema:
 * - SemanaTipoService: Operaciones CRUD de semanas tipo
 * - AsignacionTurnoService: Consulta semanas tipo al crear asignaciones
 * - CalendarioSemanaService: Consulta semanas tipo al aplicar al calendario
 */
@Repository
public interface SemanaTipoRepository extends JpaRepository<SemanaTipo, Long> {

    /**
     * Busca una semana tipo por su nombre exacto
     *
     * Query generado automáticamente:
     * SELECT * FROM SEMANATIPO WHERE nombre = ?
     *
     * Uso típico:
     * - SemanaTipoService.createSemanaTipo(): Valida que el nombre no exista
     * - SemanaTipoService.updateSemanaTipo(): Valida que el nuevo nombre no esté en uso
     *
     * @param nombre Nombre exacto de la semana tipo (ej: "Semana Verano", "Semana Normal")
     * @return Optional<SemanaTipo> - Contiene la semana tipo si existe, Optional.empty() si no
     */
    Optional<SemanaTipo> findByNombre(String nombre);

    /**
     * Verifica si existe una semana tipo con el nombre especificado
     *
     * Query generado automáticamente:
     * SELECT COUNT(*) > 0 FROM SEMANATIPO WHERE nombre = ?
     *
     * Uso típico:
     * - Validar unicidad de nombre antes de crear o actualizar semana tipo
     *
     * @param nombre Nombre de la semana tipo a verificar
     * @return true si existe una semana tipo con ese nombre, false si no existe
     */
    boolean existsByNombre(String nombre);

    /**
     * Busca todas las semanas tipo activas
     *
     * Query generado automáticamente:
     * SELECT * FROM SEMANATIPO WHERE activo = true
     *
     * Uso típico:
     * - SemanaTipoService.getSemanasActivas(): Obtener semanas disponibles para asignar
     * - Mostrar solo semanas activas en el selector del calendario
     *
     * @return List<SemanaTipo> - Lista de semanas tipo activas (puede ser vacía)
     */
    List<SemanaTipo> findByActivoTrue();
}
