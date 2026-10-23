package com.example.proyecto.global.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.proyecto.global.domain.EventoCalendario;

public interface EventoCalendarioRepository extends JpaRepository<EventoCalendario, UUID> {

	@EntityGraph(attributePaths = { "parcial", "parcial.periodo" })
	Optional<EventoCalendario> findByUsuarioIdAndParcialId(UUID usuarioId, UUID parcialId);

	@EntityGraph(attributePaths = { "parcial", "parcial.periodo" })
	List<EventoCalendario> findByUsuarioIdOrderByFechaLimiteAsc(UUID usuarioId);

	@EntityGraph(attributePaths = { "parcial", "parcial.periodo" })
	List<EventoCalendario> findByUsuarioIdAndFechaLimiteGreaterThanEqualOrderByFechaLimiteAsc(UUID usuarioId,
			LocalDate desde);
}
