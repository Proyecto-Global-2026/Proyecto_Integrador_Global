package com.example.proyecto.global.dto;

import java.util.List;
import java.util.UUID;

/**
 * Reporte de cursos acreditados por periodo. Solo cuenta evidencias APROBADAS
 * cuya fecha cae dentro del rango del periodo.
 */
public record ReporteCursosResponse(
		long totalCursos,
		long totalHoras,
		List<CursosPorPeriodo> periodos) {

	public record CursosPorPeriodo(
			UUID periodoId,
			String periodoNombre,
			long cursosAcreditados,
			long horasAcreditadas) {
	}
}
