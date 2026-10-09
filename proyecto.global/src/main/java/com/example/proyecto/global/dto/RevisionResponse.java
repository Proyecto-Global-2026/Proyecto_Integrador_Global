package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.RevisionPlaneacion;

public record RevisionResponse(
		UUID id,
		UUID planeacionId,
		String estadoAnterior,
		String estadoNuevo,
		String comentario,
		UUID revisadoPorId,
		String revisadoPorNombre,
		OffsetDateTime createdAt) {

	public static RevisionResponse from(RevisionPlaneacion r) {
		return new RevisionResponse(
				r.getId(),
				r.getPlaneacion().getId(),
				r.getEstadoAnterior().name(),
				r.getEstadoNuevo().name(),
				r.getComentario(),
				r.getRevisadoPor().getId(),
				r.getRevisadoPor().getNombre(),
				r.getCreatedAt());
	}
}