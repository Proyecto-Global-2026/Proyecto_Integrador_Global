package com.example.proyecto.global.exception;

/**
 * Las credenciales no coinciden o el correo no existe. Se traduce a 401.
 */
public class CredencialesInvalidasException extends RuntimeException {

	public CredencialesInvalidasException() {
		super("Credenciales invalidas");
	}
}