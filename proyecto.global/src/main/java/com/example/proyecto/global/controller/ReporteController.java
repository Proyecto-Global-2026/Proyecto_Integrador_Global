package com.example.proyecto.global.controller;

import java.util.Locale;
import java.util.UUID;

import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.FormatoExport;
import com.example.proyecto.global.dto.ReporteAvancesResponse;
import com.example.proyecto.global.dto.ReporteCursosResponse;
import com.example.proyecto.global.dto.ReporteDescarga;
import com.example.proyecto.global.dto.ReportePlaneacionesResponse;
import com.example.proyecto.global.dto.TipoReporte;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.service.ReporteExportService;
import com.example.proyecto.global.service.ReporteService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/reportes")
@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
@Tag(name = "Reportes institucionales")
public class ReporteController {

	private final ReporteService reporteService;
	private final ReporteExportService reporteExportService;

	public ReporteController(ReporteService reporteService, ReporteExportService reporteExportService) {
		this.reporteService = reporteService;
		this.reporteExportService = reporteExportService;
	}

	@GetMapping("/planeaciones")
	@Operation(summary = "Porcentaje de cumplimiento de planeaciones. Opcionalmente por periodo (ciclo)")
	public ReportePlaneacionesResponse planeaciones(@RequestParam(required = false) UUID periodoId) {
		return reporteService.cumplimientoPlaneaciones(periodoId);
	}

	@GetMapping("/avances")
	@Operation(summary = "Cumplimiento de avances por parcial. Opcionalmente por periodo (ciclo)")
	public ReporteAvancesResponse avances(@RequestParam(required = false) UUID periodoId) {
		return reporteService.cumplimientoAvances(periodoId);
	}

	@GetMapping("/cursos")
	@Operation(summary = "Cursos acreditados por periodo (evidencias APROBADAS dentro del periodo)")
	public ReporteCursosResponse cursos(@RequestParam(required = false) UUID periodoId) {
		return reporteService.cursosPorPeriodo(periodoId);
	}

	@GetMapping("/{tipo}/pdf")
	@Operation(summary = "Descarga un reporte en PDF (tipo: planeaciones, avances, cursos)")
	public ResponseEntity<byte[]> pdf(@PathVariable String tipo,
			@RequestParam(required = false) UUID periodoId) {
		return descargar(tipo, FormatoExport.PDF, periodoId);
	}

	@GetMapping("/{tipo}/excel")
	@Operation(summary = "Descarga un reporte en Excel (tipo: planeaciones, avances, cursos)")
	public ResponseEntity<byte[]> excel(@PathVariable String tipo,
			@RequestParam(required = false) UUID periodoId) {
		return descargar(tipo, FormatoExport.EXCEL, periodoId);
	}

	private ResponseEntity<byte[]> descargar(String tipo, FormatoExport formato, UUID periodoId) {
		TipoReporte tipoReporte = parsearTipo(tipo);
		ReporteDescarga descarga = reporteExportService.exportar(tipoReporte, formato, periodoId);
		String disposition = ContentDisposition.attachment()
				.filename(descarga.nombreArchivo())
				.build()
				.toString();
		return ResponseEntity.ok()
				.header(HttpHeaders.CONTENT_DISPOSITION, disposition)
				.contentType(MediaType.parseMediaType(descarga.contentType()))
				.body(descarga.contenido());
	}

	private TipoReporte parsearTipo(String tipo) {
		try {
			return TipoReporte.valueOf(tipo.toUpperCase(Locale.ROOT));
		} catch (IllegalArgumentException e) {
			throw new ResourceNotFoundException("Tipo de reporte no soportado: " + tipo);
		}
	}
}
