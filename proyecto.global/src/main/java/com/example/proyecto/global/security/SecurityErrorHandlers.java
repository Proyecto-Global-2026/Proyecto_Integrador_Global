package com.example.proyecto.global.security;

import java.io.IOException;
import java.time.Instant;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.example.proyecto.global.dto.ApiErrorResponse;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import tools.jackson.databind.ObjectMapper;

/**
 * Convierte los errores de Spring Security en el mismo contrato de error que
 * GlobalExceptionHandler, para que el frontend maneje una sola forma de error.
 */
@Component
public class SecurityErrorHandlers implements AuthenticationEntryPoint, AccessDeniedHandler {

	private static final Logger log = LoggerFactory.getLogger(SecurityErrorHandlers.class);

	private final ObjectMapper objectMapper;

	public SecurityErrorHandlers(ObjectMapper objectMapper) {
		this.objectMapper = objectMapper;
	}

	@Override
	public void commence(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException authException) throws IOException {
		log.debug("Peticion no autenticada a {}", request.getRequestURI());
		escribir(request, response, HttpStatus.UNAUTHORIZED, "Unauthorized",
				"Debes iniciar sesion para acceder a este recurso");
	}

	@Override
	public void handle(HttpServletRequest request, HttpServletResponse response,
			AccessDeniedException accessDeniedException) throws IOException {
		log.debug("Acceso denegado por rol en {}", request.getRequestURI());
		escribir(request, response, HttpStatus.FORBIDDEN, "Forbidden",
				"No tienes permisos para acceder a este recurso");
	}

	private void escribir(HttpServletRequest request, HttpServletResponse response, HttpStatus status,
			String error, String message) throws IOException {		ApiErrorResponse body = new ApiErrorResponse(
				Instant.now(),
				status.value(),
				error,
				message,
				request.getRequestURI(),
				Map.of(),
				java.util.List.of());
		response.setStatus(status.value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");
		objectMapper.writeValue(response.getOutputStream(), body);
	}
}