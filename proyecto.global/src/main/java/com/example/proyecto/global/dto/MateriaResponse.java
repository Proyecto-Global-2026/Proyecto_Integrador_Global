package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.Materia;

public record MateriaResponse(
		UUID id,
		String nombre,
		String codigo,
		String descripcion,
		boolean activo,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static MateriaResponse from(Materia m) {
		return new MateriaResponse(
				m.getId(),
				m.getNombre(),
				m.getCodigo(),
				m.getDescripcion(),
				m.isActivo(),
				m.getCreatedAt(),
				m.getUpdatedAt());
	}
}