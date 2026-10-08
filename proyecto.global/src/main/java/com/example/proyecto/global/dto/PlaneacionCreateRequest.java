package com.example.proyecto.global.dto;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PlaneacionCreateRequest(
		@NotNull(message = "La materia es obligatoria")
		UUID materiaId,

		@NotNull(message = "El parcial es obligatorio")
		UUID parcialId,

		@NotBlank(message = "El titulo es obligatorio")
		@Size(max = 200, message = "El titulo no puede exceder 200 caracteres")
		String titulo,

		@NotBlank(message = "El contenido es obligatorio")
		String contenido) {
}