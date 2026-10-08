package com.example.proyecto.global.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record PeriodoCreateRequest(
		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 100, message = "El nombre no puede exceder 100 caracteres")
		String nombre,

		@NotNull(message = "La fecha de inicio es obligatoria")
		LocalDate fechaInicio,

		@NotNull(message = "La fecha de fin es obligatoria")
		LocalDate fechaFin) {
}