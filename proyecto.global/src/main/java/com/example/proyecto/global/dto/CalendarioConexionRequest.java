package com.example.proyecto.global.dto;

import jakarta.validation.constraints.NotBlank;

public record CalendarioConexionRequest(
		@NotBlank(message = "El codigo de autorizacion es obligatorio")
		String codigo,

		String redirectUri) {
}
