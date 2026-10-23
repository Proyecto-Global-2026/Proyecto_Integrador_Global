package com.example.proyecto.global.service.calendario;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import com.example.proyecto.global.config.CalendarioProperties;
import com.example.proyecto.global.exception.BusinessRuleException;

@Component
public class GoogleCalendarClient implements CalendarioCliente {

	private static final Logger log = LoggerFactory.getLogger(GoogleCalendarClient.class);

	private final CalendarioProperties properties;
	private final RestClient restClient;

	public GoogleCalendarClient(CalendarioProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder().build();
	}

	@Override
	public TokenCalendario intercambiarCodigo(String codigo, String redirectUri) {
		MultiValueMap<String, String> cuerpo = new LinkedMultiValueMap<>();
		cuerpo.add("code", codigo);
		cuerpo.add("client_id", properties.google().clientId());
		cuerpo.add("client_secret", properties.google().clientSecret());
		cuerpo.add("redirect_uri", redirectUri == null || redirectUri.isBlank()
				? properties.google().redirectUri()
				: redirectUri);
		cuerpo.add("grant_type", "authorization_code");
		return solicitarToken(cuerpo);
	}

	@Override
	public TokenCalendario renovarToken(String refreshToken) {
		MultiValueMap<String, String> cuerpo = new LinkedMultiValueMap<>();
		cuerpo.add("refresh_token", refreshToken);
		cuerpo.add("client_id", properties.google().clientId());
		cuerpo.add("client_secret", properties.google().clientSecret());
		cuerpo.add("grant_type", "refresh_token");
		return solicitarToken(cuerpo);
	}

	@SuppressWarnings("unchecked")
	private TokenCalendario solicitarToken(MultiValueMap<String, String> cuerpo) {
		try {
			Map<String, Object> respuesta = restClient.post()
					.uri(properties.urlToken())
					.contentType(MediaType.APPLICATION_FORM_URLENCODED)
					.body(cuerpo)
					.retrieve()
					.body(Map.class);

			if (respuesta == null || !respuesta.containsKey("access_token") || respuesta.get("access_token") == null) {
				throw new BusinessRuleException("Google no devolvio un token de acceso valido");
			}
			String accessToken = String.valueOf(respuesta.get("access_token"));
			Object refreshTokenObj = respuesta.get("refresh_token");
			String refreshToken = refreshTokenObj == null ? null : String.valueOf(refreshTokenObj);
			long expiresIn = 3600;
			Object expiresInObj = respuesta.get("expires_in");
			if (expiresInObj instanceof Number) {
				expiresIn = ((Number) expiresInObj).longValue();
			}
			Object scopeObj = respuesta.get("scope");
			String scope = scopeObj == null ? null : String.valueOf(scopeObj);
			Object idTokenObj = respuesta.get("id_token");
			String correo = correoDesdeIdToken(idTokenObj == null ? null : String.valueOf(idTokenObj));

			return new TokenCalendario(accessToken, refreshToken,
					OffsetDateTime.now().plusSeconds(expiresIn), scope, correo);
		} catch (BusinessRuleException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error solicitando token OAuth2 a Google: {}", e.getMessage());
			throw new BusinessRuleException("No se pudo completar la autorizacion con Google Calendar");
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	public String crearEvento(String accessToken, String calendarioId, EventoGoogle evento) {
		try {
			Map<String, Object> respuesta = restClient.post()
					.uri(properties.urlApi() + "/calendars/{calendario}/events", calendarioId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(cuerpoEvento(evento))
					.retrieve()
					.body(Map.class);
			if (respuesta == null || respuesta.get("id") == null) {
				throw new BusinessRuleException("Google Calendar no devolvio el identificador del evento");
			}
			return String.valueOf(respuesta.get("id"));
		} catch (BusinessRuleException e) {
			throw e;
		} catch (Exception e) {
			log.error("Error creando evento en Google Calendar: {}", e.getMessage());
			throw new BusinessRuleException("No se pudo crear el evento en Google Calendar");
		}
	}

	@Override
	public void actualizarEvento(String accessToken, String calendarioId, String eventoId, EventoGoogle evento) {
		try {
			restClient.patch()
					.uri(properties.urlApi() + "/calendars/{calendario}/events/{evento}", calendarioId, eventoId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.contentType(MediaType.APPLICATION_JSON)
					.body(cuerpoEvento(evento))
					.retrieve()
					.toBodilessEntity();
		} catch (Exception e) {
			if (esNoEncontrado(e)) {
				throw new EventoNoEncontradoException("El evento " + eventoId + " ya no existe en Google Calendar");
			}
			log.error("Error actualizando evento {} en Google Calendar: {}", eventoId, e.getMessage());
			throw new BusinessRuleException("No se pudo actualizar el evento en Google Calendar");
		}
	}

	@Override
	public void eliminarEvento(String accessToken, String calendarioId, String eventoId) {
		try {
			restClient.delete()
					.uri(properties.urlApi() + "/calendars/{calendario}/events/{evento}", calendarioId, eventoId)
					.header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
					.retrieve()
					.toBodilessEntity();
		} catch (Exception e) {
			if (esNoEncontrado(e)) {
				log.debug("El evento {} ya no existia en Google Calendar", eventoId);
				return;
			}
			log.error("Error eliminando evento {} en Google Calendar: {}", eventoId, e.getMessage());
			throw new BusinessRuleException("No se pudo eliminar el evento en Google Calendar");
		}
	}

	private Map<String, Object> cuerpoEvento(EventoGoogle evento) {
		LocalDate fecha = evento.fecha();
		return Map.of(
				"summary", evento.titulo(),
				"description", evento.descripcion() == null ? "" : evento.descripcion(),
				"start", Map.of("date", fecha.toString()),
				"end", Map.of("date", fecha.plusDays(1).toString()),
				"reminders", Map.of(
						"useDefault", false,
						"overrides", List.of(Map.of(
								"method", "popup",
								"minutes", evento.recordatorioMinutos()))));
	}

	private boolean esNoEncontrado(Exception e) {
		String mensaje = e.getMessage() == null ? "" : e.getMessage();
		return mensaje.contains(String.valueOf(HttpStatus.NOT_FOUND.value()))
				|| mensaje.toLowerCase().contains("not found");
	}

	private String correoDesdeIdToken(String idToken) {
		if (idToken == null || idToken.isBlank()) {
			return null;
		}
		try {
			String[] partes = idToken.split("\\.");
			if (partes.length < 2) {
				return null;
			}
			String payload = new String(Base64.getUrlDecoder().decode(partes[1]));
			int inicio = payload.indexOf("\"email\"");
			if (inicio < 0) {
				return null;
			}
			int dosPuntos = payload.indexOf(':', inicio);
			int comillaInicial = payload.indexOf('"', dosPuntos + 1);
			int comillaFinal = payload.indexOf('"', comillaInicial + 1);
			return payload.substring(comillaInicial + 1, comillaFinal);
		} catch (Exception e) {
			log.debug("No se pudo extraer el correo del id_token: {}", e.getMessage());
			return null;
		}
	}
}
