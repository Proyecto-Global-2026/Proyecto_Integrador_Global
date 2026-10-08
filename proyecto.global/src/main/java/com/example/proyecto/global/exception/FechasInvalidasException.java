package com.example.proyecto.global.exception;

public class FechasInvalidasException extends BusinessRuleException {
	private static final long serialVersionUID = 1L;

	public FechasInvalidasException(String mensaje) {
		super(mensaje);
	}
}