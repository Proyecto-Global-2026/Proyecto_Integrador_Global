package com.example.proyecto.global.service.calendario;

import java.time.OffsetDateTime;

public record TokenCalendario(
		String accessToken,
		String refreshToken,
		OffsetDateTime expiraEn,
		String alcance,
		String correo) {
}
