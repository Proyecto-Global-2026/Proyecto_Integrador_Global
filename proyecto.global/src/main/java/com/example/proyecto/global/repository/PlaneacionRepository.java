package com.example.proyecto.global.repository;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.EstadoPlaneacion;
import com.example.proyecto.global.domain.Planeacion;

public interface PlaneacionRepository extends JpaRepository<Planeacion, UUID> {

	boolean existsByDocenteIdAndMateriaIdAndParcialId(UUID docenteId, UUID materiaId, UUID parcialId);

	boolean existsByDocenteIdAndMateriaIdAndParcialIdAndIdNot(UUID docenteId, UUID materiaId, UUID parcialId,
			UUID id);

	@Query("""
			SELECT pl FROM Planeacion pl
			JOIN FETCH pl.docente d
			JOIN FETCH pl.materia m
			JOIN FETCH pl.parcial p
			WHERE (:docenteId IS NULL OR d.id = :docenteId)
			  AND (:materiaId IS NULL OR m.id = :materiaId)
			  AND (:parcialId IS NULL OR p.id = :parcialId)
			  AND (:estado IS NULL OR pl.estado = :estado)
			  AND (:q IS NULL OR :q = '' OR LOWER(pl.titulo) LIKE LOWER(CONCAT('%', :q, '%')))
			""")
	Page<Planeacion> buscar(@Param("docenteId") UUID docenteId,
			@Param("materiaId") UUID materiaId,
			@Param("parcialId") UUID parcialId,
			@Param("estado") EstadoPlaneacion estado,
			@Param("q") String q,
			Pageable pageable);
}