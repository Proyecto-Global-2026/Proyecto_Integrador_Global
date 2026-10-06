package com.example.proyecto.global.dto;

import java.util.List;

import org.springframework.data.domain.Page;

/**
 * Envoltura de paginacion. Se usa en lugar de Page para que el contrato JSON
 * de la API no dependa de la estructura de Spring Data.
 */
public record PaginadoResponse<T>(

		List<T> content,

		int pagina,

		int tamanoPagina,

		long totalElementos,

		int totalPaginas) {

	public static <T> PaginadoResponse<T> de(Page<T> page) {
		return new PaginadoResponse<>(
				page.getContent(),
				page.getNumber(),
				page.getSize(),
				page.getTotalElements(),
				page.getTotalPages());
	}
}