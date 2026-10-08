package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.Planeacion;

public record PlaneacionResponse(
		UUID id,
		UUID docenteId,
		String docenteNombre,
		UUID materiaId,
		String materiaNombre,
		String materiaCodigo,
		UUID parcialId,
		String parcialNombre,
		String titulo,
		String contenido,
		String estado,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static PlaneacionResponse from(Planeacion pl) {
		return new PlaneacionResponse(
				pl.getId(),
				pl.getDocente().getId(),
				pl.getDocente().getNombre(),
				pl.getMateria().getId(),
				pl.getMateria().getNombre(),
				pl.getMateria().getCodigo(),
				pl.getParcial().getId(),
				pl.getParcial().getNombre(),
				pl.getTitulo(),
				pl.getContenido(),
				pl.getEstado().name(),
				pl.getCreatedAt(),
				pl.getUpdatedAt());
	}
}