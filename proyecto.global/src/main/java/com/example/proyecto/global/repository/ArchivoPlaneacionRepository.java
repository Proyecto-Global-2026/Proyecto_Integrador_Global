package com.example.proyecto.global.repository;

import java.time.OffsetDateTime;
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

	/**
	 * S3-07: archivos que superaron el plazo de conservacion y aun no fueron
	 * eliminados por el proceso de retencion.
	 */
	@Query("""
			SELECT a FROM ArchivoPlaneacion a
			WHERE a.eliminadoPorRetencion = false
			AND a.createdAt < :limite
			""")
	List<ArchivoPlaneacion> findAntiguosParaEliminar(@Param("limite") OffsetDateTime limite);
}
