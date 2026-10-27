package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;
import java.util.UUID;

import com.example.proyecto.global.domain.SolicitudArco;

public record SolicitudArcoResponse(
		UUID id,
		UUID usuarioId,
		String tipo,
		String estado,
		String motivo,
		String detalles,
		String respuesta,
		OffsetDateTime fechaSolicitud,
		OffsetDateTime fechaAtencion) {

	public static SolicitudArcoResponse from(SolicitudArco solicitud) {
		return new SolicitudArcoResponse(
				solicitud.getId(),
				solicitud.getUsuario() == null ? null : solicitud.getUsuario().getId(),
				solicitud.getTipo() == null ? null : solicitud.getTipo().name(),
				solicitud.getEstado() == null ? null : solicitud.getEstado().name(),
				solicitud.getMotivo(),
				solicitud.getDetalles(),
				solicitud.getRespuesta(),
				solicitud.getFechaSolicitud(),
				solicitud.getFechaAtencion());
	}
}
