package com.example.proyecto.global.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyecto.global.domain.Usuario;

public interface UsuarioRepository extends JpaRepository<Usuario, UUID> {

	Optional<Usuario> findByEmail(String email);

	boolean existsByEmail(String email);

	Optional<Usuario> findByProveedorAndProveedorId(String proveedor, String proveedorId);
}