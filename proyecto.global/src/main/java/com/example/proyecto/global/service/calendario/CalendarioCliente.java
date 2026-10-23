package com.example.proyecto.global.service.calendario;

public interface CalendarioCliente {

	TokenCalendario intercambiarCodigo(String codigo, String redirectUri);

	TokenCalendario renovarToken(String refreshToken);

	String crearEvento(String accessToken, String calendarioId, EventoGoogle evento);

	void actualizarEvento(String accessToken, String calendarioId, String eventoId, EventoGoogle evento);

	void eliminarEvento(String accessToken, String calendarioId, String eventoId);
}
