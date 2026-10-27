package com.example.proyecto.global.dto;

import java.util.List;

/**
 * Matriz de control de acceso a datos personales (S3-09) expuesta para
 * auditoria.
 */
public record MatrizRbacResponse(
		String politica,
		List<ReglaRbac> reglas) {

	public record ReglaRbac(String metodo, String ruta, List<String> roles, String descripcion) {
	}
}
