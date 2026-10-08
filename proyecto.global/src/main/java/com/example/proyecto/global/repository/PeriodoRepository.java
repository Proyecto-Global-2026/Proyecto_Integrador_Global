package com.example.proyecto.global.repository;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.Periodo;

public interface PeriodoRepository extends JpaRepository<Periodo, UUID> {

	boolean existsByNombreIgnoreCase(String nombre);

	Optional<Periodo> findByNombreIgnoreCase(String nombre);

	@Query("""
			SELECT p FROM Periodo p
			WHERE (:activo IS NULL OR p.activo = :activo)
			  AND (:q IS NULL OR :q = '' OR LOWER(p.nombre) LIKE LOWER(CONCAT('%', :q, '%')))
			  AND (:fechaInicioDesde IS NULL OR p.fechaFin >= :fechaInicioDesde)
			  AND (:fechaFinHasta IS NULL OR p.fechaInicio <= :fechaFinHasta)
			""")
	Page<Periodo> buscar(@Param("q") String q,
			@Param("activo") Boolean activo,
			@Param("fechaInicioDesde") LocalDate fechaInicioDesde,
			@Param("fechaFinHasta") LocalDate fechaFinHasta,
			Pageable pageable);
}