package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.ArchivoPlaneacion;

public record ArchivoResponse(
		UUID id,
		UUID planeacionId,
		String nombreOriginal,
		String contentType,
		long tamano,
		OffsetDateTime createdAt) {

	public static ArchivoResponse from(ArchivoPlaneacion a) {
		return new ArchivoResponse(
				a.getId(),
				a.getPlaneacion().getId(),
				a.getNombreOriginal(),
				a.getContentType(),
				a.getTamano(),
				a.getCreatedAt());
	}
}