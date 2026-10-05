package com.example.proyecto.global.exception;

/**
 * El correo ya esta registrado. Se traduce a 409.
 */
public class EmailDuplicadoException extends RuntimeException {

	private final String email;

	public EmailDuplicadoException(String email) {
		super("El correo " + email + " ya esta registrado");
		this.email = email;
	}

	public String getEmail() {
		return email;
	}
}