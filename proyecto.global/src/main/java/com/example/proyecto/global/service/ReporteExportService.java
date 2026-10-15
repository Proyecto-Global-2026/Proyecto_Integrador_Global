package com.example.proyecto.global.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.proyecto.global.dto.FormatoExport;
import com.example.proyecto.global.dto.ReporteAvancesResponse;
import com.example.proyecto.global.dto.ReporteAvancesResponse.CumplimientoParcial;
import com.example.proyecto.global.dto.ReporteCursosResponse;
import com.example.proyecto.global.dto.ReporteCursosResponse.CursosPorPeriodo;
import com.example.proyecto.global.dto.ReporteDescarga;
import com.example.proyecto.global.dto.ReportePlaneacionesResponse;
import com.example.proyecto.global.dto.TipoReporte;
import com.example.proyecto.global.exception.BusinessRuleException;

/**
 * Exportacion de los reportes institucionales a PDF (PDFBox) y Excel
 * (Apache POI). Reutiliza {@link ReporteService} como fuente de datos.
 */
@Service
public class ReporteExportService {

	private static final String CONTENT_TYPE_PDF = "application/pdf";
	private static final String CONTENT_TYPE_XLSX =
			"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";

	private final ReporteService reporteService;

	public ReporteExportService(ReporteService reporteService) {
		this.reporteService = reporteService;
	}

	public ReporteDescarga exportar(TipoReporte tipo, FormatoExport formato, UUID periodoId) {
		TablaReporte tabla = switch (tipo) {
			case PLANEACIONES -> tablaPlaneaciones(periodoId);
			case AVANCES -> tablaAvances(periodoId);
			case CURSOS -> tablaCursos(periodoId);
		};

		String base = "reporte-" + tipo.name().toLowerCase(Locale.ROOT);
		try {
			if (formato == FormatoExport.PDF) {
				return new ReporteDescarga(base + ".pdf", CONTENT_TYPE_PDF, renderPdf(tabla));
			}
			return new ReporteDescarga(base + ".xlsx", CONTENT_TYPE_XLSX, renderExcel(tabla));
		} catch (IOException e) {
			throw new BusinessRuleException("No se pudo generar el reporte: " + e.getMessage());
		}
	}

	// ------------------------------------------------------------------
	// Construccion de las tablas a partir de los reportes
	// ------------------------------------------------------------------

	private TablaReporte tablaPlaneaciones(UUID periodoId) {
		ReportePlaneacionesResponse r = reporteService.cumplimientoPlaneaciones(periodoId);
		List<String> resumen = List.of(
				"Total de planeaciones: " + r.total(),
				"Porcentaje de cumplimiento: " + r.porcentajeCumplimiento() + "%");
		List<List<String>> filas = new ArrayList<>();
		filas.add(List.of("Aprobadas", String.valueOf(r.aprobadas())));
		filas.add(List.of("Rechazadas", String.valueOf(r.rechazadas())));
		filas.add(List.of("Pendientes", String.valueOf(r.pendientes())));
		filas.add(List.of("Ajustes solicitados", String.valueOf(r.ajustesSolicitados())));
		filas.add(List.of("Total", String.valueOf(r.total())));
		return new TablaReporte("Reporte de cumplimiento de planeaciones",
				List.of("Estado", "Cantidad"), filas, resumen);
	}

	private TablaReporte tablaAvances(UUID periodoId) {
		ReporteAvancesResponse r = reporteService.cumplimientoAvances(periodoId);
		List<String> resumen = List.of(
				"Total de avances: " + r.totalAvances(),
				"Porcentaje de cumplimiento global: " + r.porcentajeCumplimiento() + "%");
		List<List<String>> filas = new ArrayList<>();
		for (CumplimientoParcial p : r.parciales()) {
			filas.add(List.of(
					p.parcialNombre(),
					String.valueOf(p.total()),
					String.valueOf(p.cumplidos()),
					String.valueOf(p.parciales()),
					String.valueOf(p.noCumplidos()),
					p.porcentajeCumplimiento() + "%"));
		}
		return new TablaReporte("Reporte de cumplimiento de avances por parcial",
				List.of("Parcial", "Total", "Cumplidos", "Parciales", "No cumplidos", "Cumplimiento"),
				filas, resumen);
	}

	private TablaReporte tablaCursos(UUID periodoId) {
		ReporteCursosResponse r = reporteService.cursosPorPeriodo(periodoId);
		List<String> resumen = List.of(
				"Total de cursos acreditados: " + r.totalCursos(),
				"Total de horas acreditadas: " + r.totalHoras());
		List<List<String>> filas = new ArrayList<>();
		for (CursosPorPeriodo p : r.periodos()) {
			filas.add(List.of(
					p.periodoNombre(),
					String.valueOf(p.cursosAcreditados()),
					String.valueOf(p.horasAcreditadas())));
		}
		return new TablaReporte("Reporte de cursos acreditados por periodo",
				List.of("Periodo", "Cursos acreditados", "Horas acreditadas"), filas, resumen);
	}

	// ------------------------------------------------------------------
	// Render PDF (PDFBox)
	// ------------------------------------------------------------------

	private byte[] renderPdf(TablaReporte tabla) throws IOException {
		try (PDDocument doc = new PDDocument()) {
			PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
			PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
			PdfWriter writer = new PdfWriter(doc, normal, bold);

			writer.titulo(tabla.titulo());
			for (String linea : tabla.resumen()) {
				writer.linea(linea, normal, 11f);
			}
			writer.espacio(10f);
			writer.fila(tabla.encabezados(), bold, 9.5f);
			for (List<String> fila : tabla.filas()) {
				writer.fila(fila, normal, 9.5f);
			}
			writer.close();

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			doc.save(out);
			return out.toByteArray();
		}
	}

	private static final class PdfWriter {
		private static final float MARGEN = 50f;
		private static final float ANCHO = PDRectangle.A4.getWidth();
		private static final float ALTO = PDRectangle.A4.getHeight();
		private static final float ANCHO_UTIL = ANCHO - 2 * MARGEN;

		private final PDDocument doc;
		private final PDFont normal;
		private final PDFont bold;
		private PDPageContentStream cs;
		private float y;

		private PdfWriter(PDDocument doc, PDFont normal, PDFont bold) throws IOException {
			this.doc = doc;
			this.normal = normal;
			this.bold = bold;
			nuevaPagina();
		}

		private void nuevaPagina() throws IOException {
			if (cs != null) {
				cs.close();
			}
			PDPage page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			cs = new PDPageContentStream(doc, page);
			y = ALTO - MARGEN;
		}

		private void titulo(String texto) throws IOException {
			asegurarEspacio(16f + 8f);
			dibujar(truncar(texto, ANCHO_UTIL, 16f), bold, 16f, MARGEN);
			y -= 16f + 8f;
		}

		private void linea(String texto, PDFont font, float size) throws IOException {
			asegurarEspacio(size + 4f);
			dibujar(truncar(texto, ANCHO_UTIL, size), font, size, MARGEN);
			y -= size + 4f;
		}

		private void espacio(float alto) {
			y -= alto;
		}

		private void fila(List<String> celdas, PDFont font, float size) throws IOException {
			asegurarEspacio(size + 5f);
			float anchoCol = ANCHO_UTIL / Math.max(1, celdas.size());
			for (int i = 0; i < celdas.size(); i++) {
				float x = MARGEN + i * anchoCol;
				dibujar(truncar(celdas.get(i), anchoCol, size), font, size, x);
			}
			y -= size + 5f;
		}

		private void asegurarEspacio(float necesario) throws IOException {
			if (y < MARGEN + necesario) {
				nuevaPagina();
			}
		}

		private void dibujar(String texto, PDFont font, float size, float x) throws IOException {
			cs.beginText();
			cs.setFont(font, size);
			cs.newLineAtOffset(x, y);
			cs.showText(sanitizar(texto));
			cs.endText();
		}

		private void close() throws IOException {
			if (cs != null) {
				cs.close();
			}
		}

		private static String truncar(String texto, float ancho, float size) {
			if (texto == null) {
				return "";
			}
			int maxChars = (int) (ancho / (size * 0.5f));
			if (maxChars < 4 || texto.length() <= maxChars) {
				return texto;
			}
			return texto.substring(0, maxChars - 3) + "...";
		}

		private static String sanitizar(String texto) {
			StringBuilder sb = new StringBuilder(texto.length());
			for (int i = 0; i < texto.length(); i++) {
				char c = texto.charAt(i);
				sb.append(c >= 32 && c <= 255 ? c : '?');
			}
			return sb.toString();
		}
	}

	// ------------------------------------------------------------------
	// Render Excel (Apache POI)
	// ------------------------------------------------------------------

	private byte[] renderExcel(TablaReporte tabla) throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("Reporte");

			CellStyle tituloStyle = wb.createCellStyle();
			Font tituloFont = wb.createFont();
			tituloFont.setBold(true);
			tituloFont.setFontHeightInPoints((short) 14);
			tituloStyle.setFont(tituloFont);

			CellStyle encabezadoStyle = wb.createCellStyle();
			Font encabezadoFont = wb.createFont();
			encabezadoFont.setBold(true);
			encabezadoStyle.setFont(encabezadoFont);

			int fila = 0;
			Row filaTitulo = sheet.createRow(fila++);
			Cell celdaTitulo = filaTitulo.createCell(0);
			celdaTitulo.setCellValue(tabla.titulo());
			celdaTitulo.setCellStyle(tituloStyle);

			for (String linea : tabla.resumen()) {
				sheet.createRow(fila++).createCell(0).setCellValue(linea);
			}
			fila++;

			Row filaEncabezado = sheet.createRow(fila++);
			for (int c = 0; c < tabla.encabezados().size(); c++) {
				Cell celda = filaEncabezado.createCell(c);
				celda.setCellValue(tabla.encabezados().get(c));
				celda.setCellStyle(encabezadoStyle);
			}

			for (List<String> valores : tabla.filas()) {
				Row row = sheet.createRow(fila++);
				for (int c = 0; c < valores.size(); c++) {
					row.createCell(c).setCellValue(valores.get(c));
				}
			}

			for (int c = 0; c < tabla.encabezados().size(); c++) {
				sheet.autoSizeColumn(c);
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			return out.toByteArray();
		}
	}

	private record TablaReporte(String titulo, List<String> encabezados, List<List<String>> filas,
			List<String> resumen) {
	}
}
