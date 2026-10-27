package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.Usuario;

public record ArcoAccesoResponse(
		UUID id,
		String nombre,
		String email,
		String rol,
		boolean activo,
		boolean anonimizado,
		String proveedor,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt,
		OffsetDateTime fechaAnonimizacion) {

	public static ArcoAccesoResponse from(Usuario usuario) {
		return new ArcoAccesoResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getEmail(),
				usuario.getRol() == null ? null : usuario.getRol().getNombre(),
				usuario.isActivo(),
				usuario.isAnonimizado(),
				usuario.getProveedor(),
				usuario.getCreatedAt(),
				usuario.getUpdatedAt(),
				usuario.getFechaAnonimizacion());
	}
}
