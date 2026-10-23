package com.example.proyecto.global.repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.proyecto.global.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID>, JpaSpecificationExecutor<Usuario> {

	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<Usuario> findByProveedorAndProveedorId(String proveedor, String proveedorId);

	List<Usuario> findByRolNombre(String rolNombre);

	/**
	 * S3-07: usuarios dados de baja que superaron el plazo de conservacion.
	 */
	@Query("""
			SELECT u FROM Usuario u
			WHERE u.anonimizado = false
			AND u.activo = false
			AND u.updatedAt < :limite
			""")
	List<Usuario> findInactivosParaAnonimizar(@Param("limite") OffsetDateTime limite);
}
