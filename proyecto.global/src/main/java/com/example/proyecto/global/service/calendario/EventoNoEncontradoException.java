package com.example.proyecto.global.service.calendario;

public class EventoNoEncontradoException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public EventoNoEncontradoException(String message) {
		super(message);
	}
}
