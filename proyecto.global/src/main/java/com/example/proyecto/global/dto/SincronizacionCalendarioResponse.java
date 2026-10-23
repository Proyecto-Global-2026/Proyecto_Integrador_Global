package com.example.proyecto.global.dto;

public record SincronizacionCalendarioResponse(
		boolean conexionActiva,
		int creados,
		int actualizados,
		int omitidos,
		int errores,
		String mensaje) {
}
