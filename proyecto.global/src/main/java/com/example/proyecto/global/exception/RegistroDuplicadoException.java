package com.example.proyecto.global.exception;

import org.springframework.http.HttpStatus;

public class RegistroDuplicadoException extends BusinessRuleException {
	private static final long serialVersionUID = 1L;

	public RegistroDuplicadoException(String mensaje) {
		super(HttpStatus.CONFLICT, mensaje);
	}
}