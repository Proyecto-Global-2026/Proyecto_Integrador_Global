package com.example.proyecto.global.dto;

import jakarta.validation.constraints.NotBlank;

public record ConsentimientoTerceroRequest(
		@NotBlank String tercero,
		Boolean otorgar,
		String versionAviso,
		String motivoRevocacion) {
}