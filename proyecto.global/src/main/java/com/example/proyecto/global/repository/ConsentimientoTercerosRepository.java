package com.example.proyecto.global.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyecto.global.domain.ConsentimientoTerceros;

public interface ConsentimientoTercerosRepository extends JpaRepository<ConsentimientoTerceros, UUID> {

	Optional<ConsentimientoTerceros> findByUsuarioIdAndTercero(UUID usuarioId, ConsentimientoTerceros.Tercero tercero);
}