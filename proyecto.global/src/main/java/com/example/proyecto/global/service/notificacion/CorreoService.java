package com.example.proyecto.global.service.notificacion;

import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.proyecto.global.config.NotificacionProperties;

/**
 * Cliente de envio de correos contra la API HTTP de SendGrid.
 *
 * Si la integracion no esta habilitada (o falta la API key) no se realiza la
 * llamada: solo se registra en el log, de modo que el entorno de desarrollo
 * funciona sin credenciales. Cualquier fallo de red se registra y no se
 * propaga, para no afectar la revision en curso.
 */
@Service
public class CorreoService {

	private static final Logger log = LoggerFactory.getLogger(CorreoService.class);

	private final NotificacionProperties properties;
	private final RestClient restClient;

	public CorreoService(NotificacionProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder().build();
	}

	public void enviar(String destinatarioEmail, String destinatarioNombre, String asunto, String html) {
		if (destinatarioEmail == null || destinatarioEmail.isBlank()) {
			log.warn("Notificacion omitida: el destinatario no tiene correo configurado");
			return;
		}
		if (!properties.listoParaEnviar()) {
			log.info("Notificacion omitida (correo deshabilitado o sin API key). Destinatario: {}, asunto: {}",
					destinatarioEmail, asunto);
			return;
		}

		Map<String, Object> contenido = Map.of(
				"personalizations", List.of(Map.of(
						"to", List.of(Map.of(
								"email", destinatarioEmail,
								"name", destinatarioNombre == null ? destinatarioEmail : destinatarioNombre)))),
				"from", Map.of(
						"email", properties.remitenteEmail(),
						"name", properties.remitenteNombre()),
				"subject", asunto,
				"content", List.of(Map.of(
						"type", "text/html",
						"value", html)));

		try {
			restClient.post()
					.uri(properties.urlSendgrid())
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + properties.sendgridApiKey())
					.contentType(MediaType.APPLICATION_JSON)
					.body(contenido)
					.retrieve()
					.toBodilessEntity();
			log.info("Correo enviado a {} con asunto '{}'", destinatarioEmail, asunto);
		} catch (Exception e) {
			log.error("Fallo el envio de correo a {}: {}", destinatarioEmail, e.getMessage());
		}
	}
}
