package com.example.proyecto.global.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.AvanceParcial;

public interface AvanceParcialRepository extends JpaRepository<AvanceParcial, UUID> {

	boolean existsByPlaneacionIdAndParcialId(UUID planeacionId, UUID parcialId);

	boolean existsByPlaneacionIdAndParcialIdAndIdNot(UUID planeacionId, UUID parcialId, UUID id);

	@Query("""
			SELECT a FROM AvanceParcial a
			JOIN FETCH a.planeacion pl
			JOIN FETCH a.parcial pa
			WHERE a.id = :id
			""")
	Optional<AvanceParcial> buscarPorIdConRelaciones(@Param("id") UUID id);

	@Query("""
			SELECT a FROM AvanceParcial a
			JOIN FETCH a.planeacion pl
			JOIN FETCH a.parcial pa
			WHERE a.planeacion.id = :planeacionId
			ORDER BY a.createdAt DESC
			""")
	List<AvanceParcial> listarPorPlaneacion(@Param("planeacionId") UUID planeacionId);
}
