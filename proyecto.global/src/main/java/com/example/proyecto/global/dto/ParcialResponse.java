package com.example.proyecto.global.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.Parcial;

public record ParcialResponse(
		UUID id,
		UUID periodoId,
		String periodoNombre,
		String nombre,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		boolean activo,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static ParcialResponse from(Parcial pr) {
		return new ParcialResponse(
				pr.getId(),
				pr.getPeriodo() != null ? pr.getPeriodo().getId() : null,
				pr.getPeriodo() != null ? pr.getPeriodo().getNombre() : null,
				pr.getNombre(),
				pr.getFechaInicio(),
				pr.getFechaFin(),
				pr.isActivo(),
				pr.getCreatedAt(),
				pr.getUpdatedAt());
	}
}