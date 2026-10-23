package com.example.proyecto.global.service.calendario;

import java.time.LocalDate;

public record EventoGoogle(
		String titulo,
		String descripcion,
		LocalDate fecha,
		String zonaHoraria,
		int recordatorioMinutos) {
}
