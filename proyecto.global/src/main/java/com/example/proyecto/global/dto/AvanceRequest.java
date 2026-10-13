package com.example.proyecto.global.dto;

import com.example.proyecto.global.domain.EstadoAvance;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record AvanceRequest(
		@NotNull(message = "El estado del avance es obligatorio")
		EstadoAvance estado,

		@Size(max = 1000, message = "Las observaciones no pueden exceder 1000 caracteres")
		String observaciones) {
}
