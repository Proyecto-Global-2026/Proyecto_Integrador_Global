package com.example.proyecto.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuración de cifrado de datos en reposo (S3-08).
 *
 * <p>
 * Soporta cifrado de valores sensibles con Jasypt (ENC(...)) tanto en
 * application.properties como en el archivo .env. La clave maestra se
 * proporciona mediante la variable de entorno {@code JASYPT_ENCRYPTOR_PASSWORD}.
 * </p>
 */
@ConfigurationProperties(prefix = "app.cifrado")
public record CifradoProperties(

		/** Habilita las funcionalidades de cifrado definidas en S3-08. */
		@DefaultValue("true") boolean habilitado,

		/** Indica si se utiliza cifrado de columnas para datos personales. */
		@DefaultValue("true") boolean cifradoColumnasHabilitado,

		/** Algoritmo utilizado por el cifrador. */
		@DefaultValue("PBEWithHMACSHA512AndAES_256") String algoritmo,

		/** Indica si la configuración con credenciales se carga de forma cifrada. */
		@DefaultValue("true") boolean configuracionCifradaHabilitada) {
}
