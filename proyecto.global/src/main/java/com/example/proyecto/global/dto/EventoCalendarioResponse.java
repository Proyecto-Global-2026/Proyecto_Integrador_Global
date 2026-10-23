package com.example.proyecto.global.dto;

import java.time.LocalDate;
import java.util.UUID;

import com.example.proyecto.global.domain.EventoCalendario;

public record EventoCalendarioResponse(
		UUID id,
		UUID parcialId,
		String parcialNombre,
		String titulo,
		LocalDate fechaLimite,
		String estado,
		String proveedor,
		String eventoId,
		String mensajeError) {

	public static EventoCalendarioResponse from(EventoCalendario ev) {
		return new EventoCalendarioResponse(
				ev.getId(),
				ev.getParcial() != null ? ev.getParcial().getId() : null,
				ev.getParcial() != null ? ev.getParcial().getNombre() : null,
				ev.getTitulo(),
				ev.getFechaLimite(),
				ev.getEstado() != null ? ev.getEstado().name() : null,
				ev.getProveedor(),
				ev.getEventoId(),
				ev.getMensajeError());
	}
}
