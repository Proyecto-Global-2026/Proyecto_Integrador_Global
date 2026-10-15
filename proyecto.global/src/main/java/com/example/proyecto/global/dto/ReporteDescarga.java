package com.example.proyecto.global.dto;

/**
 * Resultado de exportar un reporte: nombre de archivo sugerido, tipo MIME y
 * el contenido binario listo para descargar.
 */
public record ReporteDescarga(String nombreArchivo, String contentType, byte[] contenido) {
}
