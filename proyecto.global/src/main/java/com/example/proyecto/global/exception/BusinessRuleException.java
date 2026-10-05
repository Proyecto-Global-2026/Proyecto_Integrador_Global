package com.example.proyecto.global.exception;

import org.springframework.http.HttpStatus;

/**
 * Violacion de una regla de negocio. Se traduce a 422 para diferenciarla de
 * un error de validacion de entrada (400).
 */
public class BusinessRuleException extends RuntimeException {

	private final HttpStatus status;

	public BusinessRuleException(String message) {
		this(HttpStatus.UNPROCESSABLE_ENTITY, message);
	}

	public BusinessRuleException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}