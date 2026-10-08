package com.example.proyecto.global.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record MateriaUpdateRequest(
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 150, message = "El nombre no puede exceder 150 caracteres")
		String nombre,

		@NotBlank(message = "El codigo es obligatorio")
		@Size(max = 50, message = "El codigo no puede exceder 50 caracteres")
		String codigo,

		@Size(max = 255, message = "La descripcion no puede exceder 255 caracteres")
		String descripcion,

		@NotNull(message = "El estado activo es obligatorio")
		Boolean activo) {
}