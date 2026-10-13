package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.AvanceParcial;

public record AvanceResponse(
		UUID id,
		UUID planeacionId,
		String planeacionTitulo,
		UUID parcialId,
		String parcialNombre,
		String estado,
		String observaciones,
		UUID registradoPorId,
		String registradoPorNombre,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static AvanceResponse from(AvanceParcial a) {
		return new AvanceResponse(
				a.getId(),
				a.getPlaneacion().getId(),
				a.getPlaneacion().getTitulo(),
				a.getParcial().getId(),
				a.getParcial().getNombre(),
				a.getEstado().name(),
				a.getObservaciones(),
				a.getRegistradoPor().getId(),
				a.getRegistradoPor().getNombre(),
				a.getCreatedAt(),
				a.getUpdatedAt());
	}
}
