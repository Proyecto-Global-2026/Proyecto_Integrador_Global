package com.example.proyecto.global.dto;

import java.util.List;

import com.example.proyecto.global.config.RetencionProperties;

/**
 * Politica de retencion de datos personales (S3-07) expuesta para consulta y
 * auditoria.
 */
public record PoliticaRetencionResponse(
		boolean habilitado,
		String ejecucion,
		List<ReglaRetencion> reglas) {

	public record ReglaRetencion(String datoPersonal, String accion, int plazoDias, String detalle) {
	}

	public static PoliticaRetencionResponse from(RetencionProperties properties) {
		List<ReglaRetencion> reglas = List.of(
				new ReglaRetencion("Datos de usuario (nombre, correo, credenciales y proveedor externo)",
						"ANONIMIZAR", properties.diasInactividadUsuario(),
						"Se aplica a usuarios dados de baja cuyo ultimo cambio supera el plazo. La fila se conserva "
								+ "anonimizada para no romper la trazabilidad de planeaciones y revisiones."),
				new ReglaRetencion("Archivos adjuntos de planeaciones y evidencias",
						"ELIMINAR", properties.diasEliminacionArchivos(),
						"Se borra el archivo del proveedor de almacenamiento y del disco, y se marca el registro "
								+ "como eliminado por retencion."),
				new ReglaRetencion("Historial de revisiones de planeaciones",
						"ELIMINAR", properties.diasLimpiezaRevisiones(),
						"Se eliminan los registros de revision que superan el plazo maximo de conservacion."),
				new ReglaRetencion("Conexion con Google Calendar (tokens y correo)",
						"REVOCAR", properties.diasInactividadUsuario(),
						"Al anonimizar al usuario se eliminan sus tokens, eventos sincronizados y correo de calendario."));

		return new PoliticaRetencionResponse(properties.habilitado(), properties.cron(), reglas);
	}
}
