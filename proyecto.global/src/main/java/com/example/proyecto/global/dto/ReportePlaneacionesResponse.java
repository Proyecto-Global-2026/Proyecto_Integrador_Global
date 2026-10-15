package com.example.proyecto.global.dto;

/**
 * Reporte de cumplimiento de planeaciones. El porcentaje de cumplimiento se
 * calcula como planeaciones APROBADAS sobre el total.
 */
public record ReportePlaneacionesResponse(
		long total,
		long aprobadas,
		long rechazadas,
		long pendientes,
		long ajustesSolicitados,
		double porcentajeCumplimiento) {
}
