package com.example.proyecto.global.service.notificacion;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;

import com.example.proyecto.global.config.NotificacionProperties;

/**
 * Verifica que la plantilla de correo se renderiza correctamente con
 * Thymeleaf, sin levantar el contexto de Spring ni enviar correos reales.
 */
class NotificacionTemplateTest {

	private final NotificacionService service = new NotificacionService(new CorreoService(
			new NotificacionProperties(false, "", "no-reply@test.local", "Proyecto Global",
					"https://api.sendgrid.com/v3/mail/send")));

	@Test
	void renderizaNotificacionAprobadaConComentario() {
		Map<String, Object> variables = variablesBase();
		variables.put("estadoNuevo", "Aprobada");
		variables.put("colorEstado", "#15803d");
		variables.put("comentario", "Buen trabajo");

		String html = service.renderizar(variables);

		assertTrue(html.contains("Juan Pérez"));
		assertTrue(html.contains("Planeación: Álgebra"));
		assertTrue(html.contains("Aprobada"));
		assertTrue(html.contains("Buen trabajo"));
		// No deben quedar atributos de la plantilla sin procesar.
		assertFalse(html.contains("th:text"));
		assertFalse(html.contains("th:if"));
		assertFalse(html.contains("th:style"));
	}

	@Test
	void omiteElBloqueDeComentarioCuandoNoHayComentario() {
		Map<String, Object> variables = variablesBase();
		variables.put("comentario", null);

		String html = service.renderizar(variables);

		assertFalse(html.contains("Comentario del revisor"));
		assertTrue(html.contains("Juan Pérez"));
	}

	private Map<String, Object> variablesBase() {
		Map<String, Object> variables = new HashMap<>();
		variables.put("nombre", "Juan Pérez");
		variables.put("tipo", "planeación");
		variables.put("descripcion", "Planeación: Álgebra · Primer parcial");
		variables.put("estadoAnterior", "Pendiente");
		variables.put("estadoNuevo", "Rechazada");
		variables.put("colorEstado", "#b91c1c");
		variables.put("comentario", "Falta bibliografía");
		variables.put("revisor", "Coordinación");
		variables.put("fecha", "19/10/2026 10:00");
		return variables;
	}
}
