package com.example.proyecto.global.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.Parcial;

public interface ParcialRepository extends JpaRepository<Parcial, UUID> {

	boolean existsByPeriodoIdAndNombreIgnoreCase(UUID periodoId, String nombre);

	Optional<Parcial> findByPeriodoIdAndNombreIgnoreCase(UUID periodoId, String nombre);

	List<Parcial> findByPeriodoId(UUID periodoId);

	@Override
	@EntityGraph(attributePaths = "periodo")
	Optional<Parcial> findById(UUID id);

	@EntityGraph(attributePaths = "periodo")
	@Query("""
			SELECT pr FROM Parcial pr
			WHERE (:periodoId IS NULL OR pr.periodo.id = :periodoId)
			  AND (:activo IS NULL OR pr.activo = :activo)
			  AND (:q IS NULL OR :q = '' OR LOWER(pr.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
			  AND (:fechaInicioDesde IS NULL OR pr.fechaFin >= :fechaInicioDesde)
			  AND (:fechaFinHasta IS NULL OR pr.fechaInicio <= :fechaFinHasta)
			""")
	Page<Parcial> buscar(@Param("periodoId") UUID periodoId,
			@Param("q") String q,
			@Param("activo") Boolean activo,
			@Param("fechaInicioDesde") LocalDate fechaInicioDesde,
			@Param("fechaFinHasta") LocalDate fechaFinHasta,
			Pageable pageable);
}