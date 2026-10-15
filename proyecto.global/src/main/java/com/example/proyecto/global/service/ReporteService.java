package com.example.proyecto.global.service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.EstadoAvance;
import com.example.proyecto.global.domain.EstadoEvidencia;
import com.example.proyecto.global.domain.EstadoPlaneacion;
import com.example.proyecto.global.domain.Evidencia;
import com.example.proyecto.global.domain.Periodo;
import com.example.proyecto.global.dto.ReporteAvancesResponse;
import com.example.proyecto.global.dto.ReporteAvancesResponse.CumplimientoParcial;
import com.example.proyecto.global.dto.ReporteCursosResponse;
import com.example.proyecto.global.dto.ReporteCursosResponse.CursosPorPeriodo;
import com.example.proyecto.global.dto.ReportePlaneacionesResponse;
import com.example.proyecto.global.repository.AvanceParcialRepository;
import com.example.proyecto.global.repository.EvidenciaRepository;
import com.example.proyecto.global.repository.PeriodoRepository;
import com.example.proyecto.global.repository.PlaneacionRepository;

/**
 * Reportes institucionales consolidados para direccion y coordinacion.
 */
@Service
public class ReporteService {

	private final PlaneacionRepository planeacionRepository;
	private final AvanceParcialRepository avanceRepository;
	private final EvidenciaRepository evidenciaRepository;
	private final PeriodoRepository periodoRepository;

	public ReporteService(PlaneacionRepository planeacionRepository,
			AvanceParcialRepository avanceRepository,
			EvidenciaRepository evidenciaRepository,
			PeriodoRepository periodoRepository) {
		this.planeacionRepository = planeacionRepository;
		this.avanceRepository = avanceRepository;
		this.evidenciaRepository = evidenciaRepository;
		this.periodoRepository = periodoRepository;
	}

	/**
	 * Porcentaje de cumplimiento de planeaciones: APROBADAS sobre el total.
	 * Opcionalmente filtrado por periodo (ciclo).
	 */
	@Transactional(readOnly = true)
	public ReportePlaneacionesResponse cumplimientoPlaneaciones(UUID periodoId) {
		long total = 0;
		long aprobadas = 0;
		long rechazadas = 0;
		long pendientes = 0;
		long ajustes = 0;
		for (Object[] fila : planeacionRepository.contarPorEstado(periodoId)) {
			EstadoPlaneacion estado = (EstadoPlaneacion) fila[0];
			long cantidad = ((Number) fila[1]).longValue();
			total += cantidad;
			switch (estado) {
				case APROBADA -> aprobadas += cantidad;
				case RECHAZADA -> rechazadas += cantidad;
				case PENDIENTE -> pendientes += cantidad;
				case AJUSTES_SOLICITADOS -> ajustes += cantidad;
			}
		}
		return new ReportePlaneacionesResponse(total, aprobadas, rechazadas, pendientes, ajustes,
				porcentaje(aprobadas, total));
	}

	/**
	 * Cumplimiento de avances agrupado por parcial. El porcentaje de cada
	 * parcial es CUMPLIDO sobre el total de avances de ese parcial.
	 */
	@Transactional(readOnly = true)
	public ReporteAvancesResponse cumplimientoAvances(UUID periodoId) {
		Map<UUID, AcumuladorParcial> acumuladores = new LinkedHashMap<>();
		for (Object[] fila : avanceRepository.contarPorParcialYEstado(periodoId)) {
			UUID parcialId = (UUID) fila[0];
			String parcialNombre = (String) fila[1];
			EstadoAvance estado = (EstadoAvance) fila[2];
			long cantidad = ((Number) fila[3]).longValue();

			AcumuladorParcial acumulador = acumuladores.computeIfAbsent(parcialId,
					id -> new AcumuladorParcial(id, parcialNombre));
			acumulador.sumar(estado, cantidad);
		}

		List<CumplimientoParcial> parciales = new ArrayList<>();
		long totalAvances = 0;
		long totalCumplidos = 0;
		for (AcumuladorParcial acumulador : acumuladores.values()) {
			totalAvances += acumulador.total;
			totalCumplidos += acumulador.cumplidos;
			parciales.add(new CumplimientoParcial(
					acumulador.parcialId,
					acumulador.parcialNombre,
					acumulador.total,
					acumulador.cumplidos,
					acumulador.parciales,
					acumulador.noCumplidos,
					porcentaje(acumulador.cumplidos, acumulador.total)));
		}
		return new ReporteAvancesResponse(totalAvances, porcentaje(totalCumplidos, totalAvances), parciales);
	}

	/**
	 * Cursos acreditados por periodo: evidencias APROBADAS cuya fecha cae
	 * dentro del rango del periodo. Opcionalmente filtrado por periodo.
	 */
	@Transactional(readOnly = true)
	public ReporteCursosResponse cursosPorPeriodo(UUID periodoId) {
		List<Evidencia> aprobadas = evidenciaRepository.findByEstado(EstadoEvidencia.APROBADA);
		List<Periodo> periodos = periodoRepository.findAll();

		List<CursosPorPeriodo> resultado = new ArrayList<>();
		long totalCursos = 0;
		long totalHoras = 0;
		for (Periodo periodo : periodos) {
			if (periodoId != null && !periodo.getId().equals(periodoId)) {
				continue;
			}
			long cursos = 0;
			long horas = 0;
			for (Evidencia evidencia : aprobadas) {
				if (dentroDelPeriodo(evidencia.getFecha(), periodo)) {
					cursos++;
					horas += evidencia.getHorasAcreditadas() == null ? 0 : evidencia.getHorasAcreditadas();
				}
			}
			resultado.add(new CursosPorPeriodo(periodo.getId(), periodo.getNombre(), cursos, horas));
			totalCursos += cursos;
			totalHoras += horas;
		}
		return new ReporteCursosResponse(totalCursos, totalHoras, resultado);
	}

	private boolean dentroDelPeriodo(LocalDate fecha, Periodo periodo) {
		if (fecha == null) {
			return false;
		}
		return !fecha.isBefore(periodo.getFechaInicio()) && !fecha.isAfter(periodo.getFechaFin());
	}

	private double porcentaje(long parte, long total) {
		if (total == 0) {
			return 0.0;
		}
		return BigDecimal.valueOf(parte * 100.0 / total)
				.setScale(2, RoundingMode.HALF_UP)
				.doubleValue();
	}

	private static final class AcumuladorParcial {
		private final UUID parcialId;
		private final String parcialNombre;
		private long total;
		private long cumplidos;
		private long parciales;
		private long noCumplidos;

		private AcumuladorParcial(UUID parcialId, String parcialNombre) {
			this.parcialId = parcialId;
			this.parcialNombre = parcialNombre;
		}

		private void sumar(EstadoAvance estado, long cantidad) {
			total += cantidad;
			switch (estado) {
				case CUMPLIDO -> cumplidos += cantidad;
				case PARCIAL -> parciales += cantidad;
				case NO_CUMPLIDO -> noCumplidos += cantidad;
			}
		}
	}
}
