package com.example.proyecto.global.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.ArchivoPlaneacion;

public interface ArchivoPlaneacionRepository extends JpaRepository<ArchivoPlaneacion, UUID> {

	List<ArchivoPlaneacion> findByPlaneacionIdOrderByCreatedAtDesc(UUID planeacionId);

	@Query("""
			SELECT a FROM ArchivoPlaneacion a
			JOIN FETCH a.planeacion pl
			WHERE a.id = :id
			""")
	Optional<ArchivoPlaneacion> buscarPorIdConPlaneacion(@Param("id") UUID id);

	@Query("""
			SELECT a FROM ArchivoPlaneacion a
			JOIN FETCH a.planeacion pl
			WHERE a.planeacion.id = :planeacionId
			ORDER BY a.createdAt DESC
			""")
	List<ArchivoPlaneacion> listarPorPlaneacionConPlaneacion(@Param("planeacionId") UUID planeacionId);

	void deleteByPlaneacionId(UUID planeacionId);
}