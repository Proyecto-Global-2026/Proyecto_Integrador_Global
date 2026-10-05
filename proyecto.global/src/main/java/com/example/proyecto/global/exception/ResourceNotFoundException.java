package com.example.proyecto.global.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends RuntimeException {

	private final HttpStatus status;

	public ResourceNotFoundException(String message) {
		this(HttpStatus.NOT_FOUND, message);
	}

	public ResourceNotFoundException(HttpStatus status, String message) {
		super(message);
		this.status = status;
	}

	public HttpStatus getStatus() {
		return status;
	}
}