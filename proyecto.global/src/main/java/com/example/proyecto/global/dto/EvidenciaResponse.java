package com.example.proyecto.global.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.Evidencia;

public record EvidenciaResponse(
		UUID id,
		UUID usuarioId,
		String usuarioNombre,
		String curso,
		String institucion,
		LocalDate fecha,
		Integer horasAcreditadas,
		boolean tieneArchivo,
		String archivoNombre,
		String archivoContentType,
		Long archivoTamano,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static EvidenciaResponse from(Evidencia e) {
		return new EvidenciaResponse(
				e.getId(),
				e.getUsuario().getId(),
				e.getUsuario().getNombre(),
				e.getCurso(),
				e.getInstitucion(),
				e.getFecha(),
				e.getHorasAcreditadas(),
				e.tieneArchivo(),
				e.getNombreOriginal(),
				e.getContentType(),
				e.getTamano(),
				e.getCreatedAt(),
				e.getUpdatedAt());
	}
}
