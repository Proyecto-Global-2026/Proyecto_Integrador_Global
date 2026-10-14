package com.example.proyecto.global.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record EvidenciaRequest(
		@NotBlank(message = "El curso es obligatorio")
		@Size(max = 200, message = "El curso no puede exceder 200 caracteres")
		String curso,

		@NotBlank(message = "La institucion es obligatoria")
		@Size(max = 200, message = "La institucion no puede exceder 200 caracteres")
		String institucion,

		@NotNull(message = "La fecha es obligatoria")
		@PastOrPresent(message = "La fecha no puede ser futura")
		LocalDate fecha,

		@NotNull(message = "Las horas acreditadas son obligatorias")
		@Positive(message = "Las horas acreditadas deben ser mayores a cero")
		Integer horasAcreditadas) {
}
