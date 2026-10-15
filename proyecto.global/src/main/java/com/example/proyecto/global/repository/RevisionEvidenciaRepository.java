package com.example.proyecto.global.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.RevisionEvidencia;

public interface RevisionEvidenciaRepository extends JpaRepository<RevisionEvidencia, UUID> {

	@Query("""
			SELECT r FROM RevisionEvidencia r
			JOIN FETCH r.revisadoPor
			WHERE r.evidencia.id = :evidenciaId
			ORDER BY r.createdAt DESC
			""")
	List<RevisionEvidencia> listarPorEvidencia(@Param("evidenciaId") UUID evidenciaId);
}
