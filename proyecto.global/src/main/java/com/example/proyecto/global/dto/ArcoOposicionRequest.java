package com.example.proyecto.global.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record ArcoOposicionRequest(

		@NotBlank(message = "El motivo es obligatorio")
		@Size(max = 500, message = "El motivo no puede superar 500 caracteres")
		String motivo,

		@Size(max = 2000, message = "Los detalles no pueden superar 2000 caracteres")
		String detalles) {
}
