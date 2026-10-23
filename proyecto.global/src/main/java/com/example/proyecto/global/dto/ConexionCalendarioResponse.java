package com.example.proyecto.global.dto;

import java.time.OffsetDateTime;

public record ConexionCalendarioResponse(
		boolean conectado,
		String proveedor,
		String correoCalendario,
		String alcance,
		OffsetDateTime expiraEn,
		boolean habilitado) {

	public static ConexionCalendarioResponse desconectado(boolean habilitado, String proveedor) {
		return new ConexionCalendarioResponse(false, proveedor, null, null, null, habilitado);
	}
}
