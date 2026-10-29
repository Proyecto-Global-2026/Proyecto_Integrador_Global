package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.ConsentimientoTerceros;

public record ConsentimientoTerceroResponse(
		UUID id,
		UUID usuarioId,
		String tercero,
		boolean consentimientoOtorgado,
		String versionAviso,
		OffsetDateTime fechaOtorgamiento,
		OffsetDateTime fechaRevocacion,
		String motivoRevocacion,
		OffsetDateTime createdAt,
		OffsetDateTime updatedAt) {

	public static ConsentimientoTerceroResponse from(ConsentimientoTerceros c) {
		if (c == null) {
			return null;
		}
		return new ConsentimientoTerceroResponse(
				c.getId(),
				c.getUsuario() == null ? null : c.getUsuario().getId(),
				c.getTercero() == null ? null : c.getTercero().name(),
				c.isConsentimientoOtorgado(),
				c.getVersionAviso(),
				c.getFechaOtorgamiento(),
				c.getFechaRevocacion(),
				c.getMotivoRevocacion(),
				c.getCreatedAt(),
				c.getUpdatedAt());
	}
}