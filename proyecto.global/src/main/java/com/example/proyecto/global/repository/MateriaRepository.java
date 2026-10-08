package com.example.proyecto.global.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.Materia;

public interface MateriaRepository extends JpaRepository<Materia, UUID> {

	boolean existsByCodigoIgnoreCase(String codigo);

	Optional<Materia> findByCodigoIgnoreCase(String codigo);

	@Query("""
			SELECT m FROM Materia m
			WHERE (:activo IS NULL OR m.activo = :activo)
			  AND (:q IS NULL OR :q = '' OR
			       LOWER(m.nombre) LIKE LOWER(CONCAT('%', :q, '%')) OR
			       LOWER(m.codigo) LIKE LOWER(CONCAT('%', :q, '%')))
			""")
	Page<Materia> buscar(@Param("q") String q, @Param("activo") Boolean activo, Pageable pageable);
}