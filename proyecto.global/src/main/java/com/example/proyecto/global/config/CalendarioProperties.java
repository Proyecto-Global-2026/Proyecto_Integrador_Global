package com.example.proyecto.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracion de la integracion con Google Calendar (S3-03).
 */
@ConfigurationProperties(prefix = "app.calendario")
public record CalendarioProperties(

		@DefaultValue("false") boolean habilitado,

		@DefaultValue("google") String proveedor,

		/** Identificador del calendario destino. "primary" es el del usuario. */
		@DefaultValue("primary") String calendarioId,

		/** Zona horaria con la que se crean los eventos. */
		@DefaultValue("America/Mexico_City") String zonaHoraria,

		/** Minutos de anticipacion del recordatorio del evento. */
		@DefaultValue("1440") int recordatorioMinutos,

		/** Vigencia, en dias, de las fechas limite que se sincronizan. */
		@DefaultValue("180") int diasVigencia,

		/** URL de la API de Google Calendar. */
		@DefaultValue("https://www.googleapis.com/calendar/v3") String urlApi,

		/** Endpoint de tokens OAuth2 de Google. */
		@DefaultValue("https://oauth2.googleapis.com/token") String urlToken,

		@DefaultValue GoogleProps google) {

	public record GoogleProps(
			@DefaultValue("") String clientId,
			@DefaultValue("") String clientSecret,
			@DefaultValue("openid,email,https://www.googleapis.com/auth/calendar.events") String scope,
			@DefaultValue("postmessage") String redirectUri) {
	}

	/**
	 * La sincronizacion real solo se intenta cuando la integracion esta
	 * habilitada y hay credenciales OAuth2 configuradas.
	 */
	public boolean listoParaSincronizar() {
		return habilitado
				&& google() != null
				&& google().clientId() != null && !google().clientId().isBlank()
				&& google().clientSecret() != null && !google().clientSecret().isBlank();
	}
}
