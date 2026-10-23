package com.example.proyecto.global.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyecto.global.domain.ConexionCalendario;

public interface ConexionCalendarioRepository extends JpaRepository<ConexionCalendario, UUID> {

	@EntityGraph(attributePaths = "usuario")
	Optional<ConexionCalendario> findByUsuarioIdAndProveedor(UUID usuarioId, String proveedor);

	Optional<ConexionCalendario> findByUsuarioEmailAndProveedor(String email, String proveedor);

	void deleteByUsuarioIdAndProveedor(UUID usuarioId, String proveedor);
}
