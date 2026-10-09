package com.example.proyecto.global.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.RevisionPlaneacion;

public interface RevisionPlaneacionRepository extends JpaRepository<RevisionPlaneacion, UUID> {

	@Query("""
			SELECT r FROM RevisionPlaneacion r
			JOIN FETCH r.revisadoPor
			WHERE r.planeacion.id = :planeacionId
			ORDER BY r.createdAt DESC
			""")
	List<RevisionPlaneacion> listarPorPlaneacion(@Param("planeacionId") UUID planeacionId);
}