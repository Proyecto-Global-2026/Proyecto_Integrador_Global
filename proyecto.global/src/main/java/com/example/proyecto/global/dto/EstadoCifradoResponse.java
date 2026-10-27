package com.example.proyecto.global.dto;

import com.example.proyecto.global.config.CifradoProperties;

public record EstadoCifradoResponse(
		boolean habilitado,
		boolean cifradoColumnasHabilitado,
		String algoritmo,
		boolean configuracionCifradaHabilitada,
		String detalle) {

	public static EstadoCifradoResponse from(CifradoProperties properties) {
		return new EstadoCifradoResponse(
				properties.habilitado(),
				properties.cifradoColumnasHabilitado(),
				properties.algoritmo(),
				properties.configuracionCifradaHabilitada(),
				"Los valores sensibles en application.properties/.env pueden cifrarse con ENC(...). "
						+ "Los tokens OAuth2, credenciales de terceros y datos personales se tratan con cifrado en reposo.");
	}
}
