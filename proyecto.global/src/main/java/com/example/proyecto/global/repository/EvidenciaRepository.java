package com.example.proyecto.global.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.Evidencia;
import com.example.proyecto.global.domain.EstadoEvidencia;

public interface EvidenciaRepository extends JpaRepository<Evidencia, UUID> {

	@Query("""
			SELECT e FROM Evidencia e
			JOIN FETCH e.usuario u
			WHERE (:usuarioId IS NULL OR u.id = :usuarioId)
			  AND (:estado IS NULL OR e.estado = :estado)
			  AND (:q IS NULL OR :q = '' OR LOWER(e.curso) LIKE LOWER(CONCAT('%', :q, '%'))
			       OR LOWER(e.institucion) LIKE LOWER(CONCAT('%', :q, '%')))
			""")
	Page<Evidencia> buscar(@Param("usuarioId") UUID usuarioId, @Param("estado") EstadoEvidencia estado,
			@Param("q") String q, Pageable pageable);

	@Query("""
			SELECT e FROM Evidencia e
			JOIN FETCH e.usuario u
			WHERE e.id = :id
			""")
	Optional<Evidencia> buscarPorIdConUsuario(@Param("id") UUID id);
}
