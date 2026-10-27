package com.example.proyecto.global.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;
import org.junit.jupiter.api.Test;

/**
 * Verifica que el algoritmo configurado en S3-08 cifra y descifra valores
 * sensibles (los mismos parametros que usa {@code jasypt.encryptor.*}).
 */
class CifradoJasyptTest {

	private static final String CLAVE = "clave-de-prueba";
	private static final String ALGORITMO = "PBEWithHMACSHA512AndAES_256";

	@Test
	void cifraYDescifraUnValorSensible() {
		PooledPBEStringEncryptor cifrador = cifrador();

		String valorOriginal = "token-secreto-de-prueba";
		String valorCifrado = cifrador.encrypt(valorOriginal);

		assertNotEquals(valorOriginal, valorCifrado, "El valor cifrado no debe ser igual al original");
		assertTrue(valorCifrado.length() > 0, "El valor cifrado no debe estar vacio");
		assertEquals(valorOriginal, cifrador.decrypt(valorCifrado), "El descifrado debe recuperar el valor original");
	}

	@Test
	void dosCifradosDelMismoValorSonDistintos() {
		PooledPBEStringEncryptor cifrador = cifrador();

		String primero = cifrador.encrypt("mismo-valor");
		String segundo = cifrador.encrypt("mismo-valor");

		assertNotEquals(primero, segundo, "El IV aleatorio debe producir cifrados distintos");
		assertEquals("mismo-valor", cifrador.decrypt(primero));
		assertEquals("mismo-valor", cifrador.decrypt(segundo));
	}

	private PooledPBEStringEncryptor cifrador() {
		SimpleStringPBEConfig config = new SimpleStringPBEConfig();
		config.setPassword(CLAVE);
		config.setAlgorithm(ALGORITMO);
		config.setKeyObtentionIterations(1000);
		config.setPoolSize(1);
		config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
		config.setStringOutputType("base64");

		PooledPBEStringEncryptor cifrador = new PooledPBEStringEncryptor();
		cifrador.setConfig(config);
		return cifrador;
	}
}
