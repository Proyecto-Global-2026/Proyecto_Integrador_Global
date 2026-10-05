package com.example.proyecto.global.dto;

import java.time.Instant;
import java.util.List;
import java.util.Map;

/**
 * Contrato unico de error de la API. Evita filtrar excepciones internas y
 * estandariza la respuesta para el frontend.
 */
public record ApiErrorResponse(

		Instant timestamp,

		int status,

		String error,

		String message,

		String path,

		Map<String, String> fieldErrors,

		List<String> details) {

	public ApiErrorResponse {
		fieldErrors = fieldErrors == null ? Map.of() : Map.copyOf(fieldErrors);
		details = details == null ? List.of() : List.copyOf(details);
	}
}