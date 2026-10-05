package com.example.proyecto.global.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyecto.global.domain.Rol;

public interface RolRepository extends JpaRepository<Rol, Short> {

	Optional<Rol> findByNombre(String nombre);
}