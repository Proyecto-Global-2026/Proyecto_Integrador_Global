package com.example.proyecto.global.dto;

import java.util.List;
import java.util.UUID;

/**
 * Reporte de cumplimiento de avances por parcial. Cada parcial incluye el
 * desglose por estado y su porcentaje de cumplimiento (CUMPLIDO sobre total).
 */
public record ReporteAvancesResponse(
		long totalAvances,
		double porcentajeCumplimiento,
		List<CumplimientoParcial> parciales) {

	public record CumplimientoParcial(
			UUID parcialId,
			String parcialNombre,
			long total,
			long cumplidos,
			long parciales,
			long noCumplidos,
			double porcentajeCumplimiento) {
	}
}
