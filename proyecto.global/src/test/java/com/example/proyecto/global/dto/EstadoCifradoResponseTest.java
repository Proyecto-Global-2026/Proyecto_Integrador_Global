package com.example.proyecto.global.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.proyecto.global.config.CifradoProperties;

class EstadoCifradoResponseTest {

	@Test
	void reflejaLaConfiguracionDeCifrado() {
		CifradoProperties properties = new CifradoProperties(
				true, true, "PBEWithHMACSHA512AndAES_256", true);

		EstadoCifradoResponse response = EstadoCifradoResponse.from(properties);

		assertTrue(response.habilitado());
		assertTrue(response.cifradoColumnasHabilitado());
		assertEquals("PBEWithHMACSHA512AndAES_256", response.algoritmo());
		assertTrue(response.configuracionCifradaHabilitada());
		assertTrue(response.detalle().contains("ENC("), "El detalle debe explicar el uso de ENC(...)");
	}

	@Test
	void reflejaElCifradoDeshabilitado() {
		CifradoProperties properties = new CifradoProperties(
				false, false, "PBEWithHMACSHA512AndAES_256", false);

		EstadoCifradoResponse response = EstadoCifradoResponse.from(properties);

		assertEquals(false, response.habilitado());
		assertEquals(false, response.cifradoColumnasHabilitado());
		assertEquals(false, response.configuracionCifradaHabilitada());
	}
}
