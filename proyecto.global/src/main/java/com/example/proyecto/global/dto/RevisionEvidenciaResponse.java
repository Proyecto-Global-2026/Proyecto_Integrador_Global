package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.RevisionEvidencia;

public record RevisionEvidenciaResponse(
		UUID id,
		UUID evidenciaId,
		String estadoAnterior,
		String estadoNuevo,
		String comentario,
		UUID revisadoPorId,
		String revisadoPorNombre,
		OffsetDateTime createdAt) {

	public static RevisionEvidenciaResponse from(RevisionEvidencia r) {
		return new RevisionEvidenciaResponse(
				r.getId(),
				r.getEvidencia().getId(),
				r.getEstadoAnterior().name(),
				r.getEstadoNuevo().name(),
				r.getComentario(),
				r.getRevisadoPor().getId(),
				r.getRevisadoPor().getNombre(),
				r.getCreatedAt());
	}
}
