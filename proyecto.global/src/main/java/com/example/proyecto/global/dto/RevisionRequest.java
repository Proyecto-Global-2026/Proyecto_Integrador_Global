package com.example.proyecto.global.dto;

import com.example.proyecto.global.domain.EstadoPlaneacion;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record RevisionRequest(
		@NotNull(message = "El estado destino es obligatorio")
		EstadoPlaneacion estado,

		@Size(max = 1000, message = "El comentario no puede exceder 1000 caracteres")
		String comentario) {
}