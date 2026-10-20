package com.example.proyecto.global.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Configuracion de las notificaciones por correo (S3-01).
 *
 * La integracion se desactiva por defecto para que el entorno de desarrollo
 * funcione sin credenciales. En produccion se habilita con
 * NOTIFICACIONES_HABILITADO=true y SENDGRID_API_KEY.
 */
@ConfigurationProperties(prefix = "app.notificaciones")
public record NotificacionProperties(

		@DefaultValue("false") boolean habilitado,

		@DefaultValue("") String sendgridApiKey,

		@DefaultValue("no-reply@proyecto-global.local") String remitenteEmail,

		@DefaultValue("Proyecto Global") String remitenteNombre,

		@DefaultValue("https://api.sendgrid.com/v3/mail/send") String urlSendgrid) {

	/**
	 * Solo se intenta enviar cuando la integracion esta habilitada y hay
	 * una API key configurada.
	 */
	public boolean listoParaEnviar() {
		return habilitado && sendgridApiKey != null && !sendgridApiKey.isBlank();
	}
}
