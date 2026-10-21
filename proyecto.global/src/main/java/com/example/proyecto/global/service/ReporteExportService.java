package com.example.proyecto.global.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Service;

import com.example.proyecto.global.domain.Periodo;
import com.example.proyecto.global.dto.FormatoExport;
import com.example.proyecto.global.dto.ReporteAvancesResponse;
import com.example.proyecto.global.dto.ReporteAvancesResponse.CumplimientoParcial;
import com.example.proyecto.global.dto.ReporteCursosResponse;
import com.example.proyecto.global.dto.ReporteCursosResponse.CursosPorPeriodo;
import com.example.proyecto.global.dto.ReporteDescarga;
import com.example.proyecto.global.dto.ReportePlaneacionesResponse;
import com.example.proyecto.global.dto.TipoReporte;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.repository.PeriodoRepository;

@Service
public class ReporteExportService {

	private static final String CONTENT_TYPE_PDF = "application/pdf";
	private static final String CONTENT_TYPE_XLSX = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet";
	private static final DateTimeFormatter FECHA_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final ReporteService reporteService;
	private final PeriodoRepository periodoRepository;

	public ReporteExportService(ReporteService reporteService, PeriodoRepository periodoRepository) {
		this.reporteService = reporteService;
		this.periodoRepository = periodoRepository;
	}

	public ReporteDescarga exportar(TipoReporte tipo, FormatoExport formato, UUID periodoId) {
		Periodo periodo = null;
		if (periodoId != null) {
			periodo = periodoRepository.findById(periodoId).orElse(null);
		}
		TablaReporte tabla = switch (tipo) {
			case PLANEACIONES -> tablaPlaneaciones(periodoId, periodo);
			case AVANCES -> tablaAvances(periodoId, periodo);
			case CURSOS -> tablaCursos(periodoId, periodo);
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

	private TablaReporte tablaPlaneaciones(UUID periodoId, Periodo periodo) {
		ReportePlaneacionesResponse r = reporteService.cumplimientoPlaneaciones(periodoId);
		List<String> resumen = new ArrayList<>();
		resumen.add("Total de planeaciones: " + r.total());
		resumen.add("Aprobadas: " + r.aprobadas());
		resumen.add("Rechazadas: " + r.rechazadas());
		resumen.add("Pendientes: " + r.pendientes());
		resumen.add("Ajustes solicitados: " + r.ajustesSolicitados());
		resumen.add("Porcentaje de cumplimiento: " + r.porcentajeCumplimiento() + "%");
		List<List<String>> filas = new ArrayList<>();
		filas.add(List.of("Aprobadas", String.valueOf(r.aprobadas())));
		filas.add(List.of("Rechazadas", String.valueOf(r.rechazadas())));
		filas.add(List.of("Pendientes", String.valueOf(r.pendientes())));
		filas.add(List.of("Ajustes solicitados", String.valueOf(r.ajustesSolicitados())));
		filas.add(List.of("Total", String.valueOf(r.total())));
		String periodoLbl = periodo != null ? periodo.getNombre() : "Todos los periodos";
		return new TablaReporte("Reporte de cumplimiento de planeaciones", periodoLbl,
				List.of("Estado", "Cantidad"), filas, resumen);
	}

	private TablaReporte tablaAvances(UUID periodoId, Periodo periodo) {
		ReporteAvancesResponse r = reporteService.cumplimientoAvances(periodoId);
		List<String> resumen = new ArrayList<>();
		resumen.add("Total de avances: " + r.totalAvances());
		resumen.add("Porcentaje de cumplimiento global: " + r.porcentajeCumplimiento() + "%");
		List<List<String>> filas = new ArrayList<>();
		for (CumplimientoParcial p : r.parciales()) {
			filas.add(List.of(
					p.parcialNombre(),
					String.valueOf(p.total()),
					String.valueOf(p.cumplidos()),
					String.valueOf(p.parciales()),
					String.valueOf(p.noCumplidos()),
					String.format(Locale.ROOT, "%.2f%%", p.porcentajeCumplimiento())));
		}
		String periodoLbl = periodo != null ? periodo.getNombre() : "Todos los periodos";
		return new TablaReporte("Reporte de cumplimiento de avances por parcial", periodoLbl,
				List.of("Parcial", "Total", "Cumplidos", "Parciales", "No cumplidos", "Cumplimiento"),
				filas, resumen);
	}

	private TablaReporte tablaCursos(UUID periodoId, Periodo periodo) {
		ReporteCursosResponse r = reporteService.cursosPorPeriodo(periodoId);
		List<String> resumen = new ArrayList<>();
		resumen.add("Total de cursos acreditados: " + r.totalCursos());
		resumen.add("Total de horas acreditadas: " + r.totalHoras());
		List<List<String>> filas = new ArrayList<>();
		for (CursosPorPeriodo p : r.periodos()) {
			filas.add(List.of(
					p.periodoNombre(),
					String.valueOf(p.cursosAcreditados()),
					String.valueOf(p.horasAcreditadas())));
		}
		String periodoLbl = periodo != null ? periodo.getNombre() : "Todos los periodos";
		return new TablaReporte("Reporte de cursos acreditados por periodo", periodoLbl,
				List.of("Periodo", "Cursos acreditados", "Horas acreditadas"), filas, resumen);
	}

	private byte[] renderPdf(TablaReporte tabla) throws IOException {
		try (PDDocument doc = new PDDocument()) {
			PDFont normal = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
			PDFont bold = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
			PdfWriter writer = new PdfWriter(doc, normal, bold);

			writer.titulo(tabla.titulo());
			writer.linea("Periodo: " + tabla.periodo(), normal, 10f);
			writer.linea("Generado: " + LocalDateTime.now().format(FECHA_FMT), normal, 9.5f);
			writer.espacio(8f);
			for (String linea : tabla.resumen()) {
				writer.linea(linea, normal, 10f);
			}
			writer.espacio(10f);
			writer.tabla(tabla.encabezados(), tabla.filas(), bold, normal);
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
		private static final float ESPACIO_LINEA = 14f;
		private static final float PADDING = 4f;

		private final PDDocument doc;
		private final PDFont normal;
		private final PDFont bold;
		private PDPage page;
		private PDPageContentStream stream;
		private float y;

		PdfWriter(PDDocument doc, PDFont normal, PDFont bold) throws IOException {
			this.doc = doc;
			this.normal = normal;
			this.bold = bold;
			nuevaPagina();
		}

		private void nuevaPagina() throws IOException {
			page = new PDPage(PDRectangle.A4);
			doc.addPage(page);
			stream = new PDPageContentStream(doc, page);
			y = ALTO - MARGEN;
		}

		void titulo(String texto) throws IOException {
			stream.setFont(bold, 14f);
			stream.beginText();
			stream.newLineAtOffset(MARGEN, y);
			stream.showText(texto);
			stream.endText();
			y -= 18f;
		}

		void linea(String texto, PDFont fuente, float tamano) throws IOException {
			if (y < MARGEN + 20f) {
				cerrarPagina();
				nuevaPagina();
			}
			stream.setFont(fuente, tamano);
			stream.beginText();
			stream.newLineAtOffset(MARGEN, y);
			stream.showText(texto);
			stream.endText();
			y -= ESPACIO_LINEA;
		}

		void espacio(float puntos) {
			y -= puntos;
		}

		void tabla(List<String> encabezados, List<List<String>> filas, PDFont fontBold, PDFont fontNormal)
				throws IOException {
			if (encabezados == null || encabezados.isEmpty()) {
				return;
			}
			int cols = encabezados.size();
			float anchoTotal = ANCHO - 2 * MARGEN;
			float[] anchos = calcularAnchos(cols, anchoTotal);
			float alturaFila = 16f;

			if (y < MARGEN + 40f) {
				cerrarPagina();
				nuevaPagina();
			}
			drawRow(encabezados, anchos, fontBold, 10f, alturaFila, true);
			y -= alturaFila;

			for (int i = 0; i < filas.size(); i++) {
				if (y < MARGEN + 20f) {
					cerrarPagina();
					nuevaPagina();
					drawRow(encabezados, anchos, fontBold, 10f, alturaFila, true);
					y -= alturaFila;
				}
				boolean alternada = i % 2 == 1;
				drawRow(filas.get(i), anchos, fontNormal, 9.5f, alturaFila, alternada);
				y -= alturaFila;
			}
		}

		private void drawRow(List<String> valores, float[] anchos, PDFont font, float size, float alturaFila,
				boolean fondoGris) throws IOException {
			float x = MARGEN;
			float yBase = y;
			float anchoTotal = ANCHO - 2 * MARGEN;

			if (fondoGris) {
				stream.setNonStrokingColor(0.95f, 0.95f, 0.95f);
				stream.addRect(x, yBase - alturaFila + 2f, anchoTotal, alturaFila);
				stream.fill();
				stream.setNonStrokingColor(0, 0, 0);
			}

			stream.setStrokingColor(0.6f, 0.6f, 0.6f);
			stream.addRect(x, yBase - alturaFila + 2f, anchoTotal, alturaFila);
			stream.stroke();
			stream.setStrokingColor(0, 0, 0);

			for (int c = 0; c < anchos.length; c++) {
				String val = "";
				if (valores != null && c < valores.size() && valores.get(c) != null) {
					val = valores.get(c);
				}
				stream.setFont(font, size);
				stream.beginText();
				stream.newLineAtOffset(x + PADDING, yBase - 4f);
				stream.showText(truncar(val, font, size, anchos[c] - 2 * PADDING));
				stream.endText();
				x += anchos[c];
			}
		}

		private float[] calcularAnchos(int cols, float totalDisponible) {
			int c = Math.max(1, cols);
			float[] anchos = new float[c];
			float w = totalDisponible / (float) c;
			Arrays.fill(anchos, w);
			return anchos;
		}

		private String truncar(String texto, PDFont font, float size, float maxAncho) throws IOException {
			if (texto == null) {
				return "";
			}
			float w = font.getStringWidth(texto) / 1000f * size;
			if (w <= maxAncho + 1f) {
				return texto;
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < texto.length(); i++) {
				String s2 = sb.toString() + "...";
				float w2 = font.getStringWidth(s2) / 1000f * size;
				if (w2 > maxAncho) {
					break;
				}
				sb.append(texto.charAt(i));
			}
			return sb.toString();
		}

		private void cerrarPagina() throws IOException {
			if (stream != null) {
				stream.close();
			}
		}

		void close() throws IOException {
			cerrarPagina();
		}
	}

	private byte[] renderExcel(TablaReporte tabla) throws IOException {
		try (XSSFWorkbook wb = new XSSFWorkbook()) {
			Sheet sheet = wb.createSheet("Reporte");
			int fila = 0;

			Font fontTitulo = wb.createFont();
			fontTitulo.setBold(true);
			fontTitulo.setFontHeightInPoints((short) 14);

			CellStyle styleTitulo = wb.createCellStyle();
			styleTitulo.setFont(fontTitulo);
			styleTitulo.setAlignment(HorizontalAlignment.LEFT);

			Row rowTitulo = sheet.createRow(fila++);
			Cell cellTitulo = rowTitulo.createCell(0);
			cellTitulo.setCellValue(tabla.titulo());
			cellTitulo.setCellStyle(styleTitulo);

			Row rowPeriodo = sheet.createRow(fila++);
			rowPeriodo.createCell(0).setCellValue("Periodo: " + tabla.periodo());

			Row rowFecha = sheet.createRow(fila++);
			rowFecha.createCell(0).setCellValue("Generado: " + LocalDateTime.now().format(FECHA_FMT));

			fila++;

			for (String linea : tabla.resumen()) {
				Row r = sheet.createRow(fila++);
				r.createCell(0).setCellValue(linea);
			}
			fila++;

			Font fontHeader = wb.createFont();
			fontHeader.setBold(true);
			fontHeader.setColor(IndexedColors.WHITE.getIndex());

			CellStyle styleHeader = wb.createCellStyle();
			styleHeader.setFont(fontHeader);
			styleHeader.setFillForegroundColor(IndexedColors.GREY_50_PERCENT.getIndex());
			styleHeader.setFillPattern(FillPatternType.SOLID_FOREGROUND);
			styleHeader.setAlignment(HorizontalAlignment.CENTER);
			styleHeader.setBorderTop(BorderStyle.THIN);
			styleHeader.setBorderBottom(BorderStyle.THIN);
			styleHeader.setBorderLeft(BorderStyle.THIN);
			styleHeader.setBorderRight(BorderStyle.THIN);

			CellStyle styleData = wb.createCellStyle();
			styleData.setBorderTop(BorderStyle.THIN);
			styleData.setBorderBottom(BorderStyle.THIN);
			styleData.setBorderLeft(BorderStyle.THIN);
			styleData.setBorderRight(BorderStyle.THIN);
			styleData.setWrapText(true);

			Row headerRow = sheet.createRow(fila++);
			for (int c = 0; c < tabla.encabezados().size(); c++) {
				Cell celda = headerRow.createCell(c);
				celda.setCellValue(tabla.encabezados().get(c));
				celda.setCellStyle(styleHeader);
			}

			for (List<String> valores : tabla.filas()) {
				Row row = sheet.createRow(fila++);
				for (int c = 0; c < valores.size(); c++) {
					Cell celda = row.createCell(c);
					celda.setCellValue(valores.get(c));
					celda.setCellStyle(styleData);
				}
			}

			for (int c = 0; c < tabla.encabezados().size(); c++) {
				sheet.autoSizeColumn(c, true);
			}
			if (tabla.encabezados().size() > 0) {
				sheet.createFreezePane(0, headerRow.getRowNum(), 0, headerRow.getRowNum());
			}

			ByteArrayOutputStream out = new ByteArrayOutputStream();
			wb.write(out);
			return out.toByteArray();
		}
	}

	private record TablaReporte(String titulo, String periodo, List<String> encabezados, List<List<String>> filas,
			List<String> resumen) {
	}
}
