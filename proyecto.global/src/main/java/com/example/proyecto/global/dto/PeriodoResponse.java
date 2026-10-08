package com.example.proyecto.global.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.Periodo;

public record PeriodoResponse(
		UUID id,
		String nombre,
		LocalDate fechaInicio,
		LocalDate fechaFin,
		boolean activo,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static PeriodoResponse from(Periodo p) {
		return new PeriodoResponse(
				p.getId(),
				p.getNombre(),
				p.getFechaInicio(),
				p.getFechaFin(),
				p.isActivo(),
				p.getCreatedAt(),
				p.getUpdatedAt());
	}
}