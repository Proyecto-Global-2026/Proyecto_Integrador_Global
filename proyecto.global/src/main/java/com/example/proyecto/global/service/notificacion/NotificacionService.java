package com.example.proyecto.global.service.notificacion;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;

import com.example.proyecto.global.domain.EstadoEvidencia;
import com.example.proyecto.global.domain.EstadoPlaneacion;
import com.example.proyecto.global.domain.Evidencia;
import com.example.proyecto.global.domain.Planeacion;
import com.example.proyecto.global.domain.Usuario;

/**
 * Genera y envia las notificaciones de cambio de estado de planeaciones y
 * evidencias usando la plantilla "templates/email/notificacion.html".
 *
 * El motor de plantillas se construye aqui mismo para no depender del
 * auto-configurado por Spring Boot.
 */
@Service
public class NotificacionService {

	private static final Logger log = LoggerFactory.getLogger(NotificacionService.class);
	private static final DateTimeFormatter FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

	private final CorreoService correoService;
	private final SpringTemplateEngine templateEngine;

	public NotificacionService(CorreoService correoService) {
		this.correoService = correoService;
		this.templateEngine = crearMotorPlantillas();
	}

	private static SpringTemplateEngine crearMotorPlantillas() {
		ClassLoaderTemplateResolver resolver = new ClassLoaderTemplateResolver();
		resolver.setPrefix("templates/email/");
		resolver.setSuffix(".html");
		resolver.setTemplateMode(TemplateMode.HTML);
		resolver.setCharacterEncoding("UTF-8");
		resolver.setCacheable(true);

		SpringTemplateEngine engine = new SpringTemplateEngine();
		engine.setTemplateResolver(resolver);
		return engine;
	}

	public void notificarRevisionPlaneacion(Planeacion planeacion, EstadoPlaneacion anterior,
			EstadoPlaneacion nuevo, String comentario, Usuario revisor) {
		String descripcion = "Planeación: " + planeacion.getTitulo()
				+ " · " + planeacion.getMateria().getNombre()
				+ " · " + planeacion.getParcial().getNombre();
		notificar(planeacion.getDocente(), "planeación", descripcion,
				anterior.name(), nuevo.name(), comentario, revisor);
	}

	public void notificarRevisionEvidencia(Evidencia evidencia, EstadoEvidencia anterior,
			EstadoEvidencia nuevo, String comentario, Usuario revisor) {
		String descripcion = "Evidencia: " + evidencia.getCurso()
				+ " · " + evidencia.getInstitucion()
				+ " · " + evidencia.getHorasAcreditadas() + " horas";
		notificar(evidencia.getUsuario(), "evidencia", descripcion,
				anterior.name(), nuevo.name(), comentario, revisor);
	}

	private void notificar(Usuario destinatario, String tipo, String descripcion,
			String estadoAnterior, String estadoNuevo, String comentario, Usuario revisor) {
		try {
			Map<String, Object> variables = new HashMap<>();
			variables.put("nombre", destinatario.getNombre());
			variables.put("tipo", tipo);
			variables.put("descripcion", descripcion);
			variables.put("estadoAnterior", etiqueta(estadoAnterior));
			variables.put("estadoNuevo", etiqueta(estadoNuevo));
			variables.put("colorEstado", color(estadoNuevo));
			variables.put("comentario",
					comentario == null || comentario.isBlank() ? null : comentario.trim());
			variables.put("revisor", revisor.getNombre());
			variables.put("fecha", OffsetDateTime.now().format(FORMATO_FECHA));

			String html = renderizar(variables);
			correoService.enviar(destinatario.getEmail(), destinatario.getNombre(),
					asunto(tipo, estadoNuevo), html);
		} catch (Exception e) {
			// La notificacion nunca debe romper la revision.
			log.error("No se pudo generar/enviar la notificacion de {} a {}: {}",
					tipo, destinatario.getEmail(), e.getMessage());
		}
	}

	String renderizar(Map<String, Object> variables) {
		Context contexto = new Context(Locale.forLanguageTag("es"));
		contexto.setVariables(variables);
		return templateEngine.process("notificacion", contexto);
	}

	private String etiqueta(String estado) {
		return switch (estado) {
			case "APROBADA" -> "Aprobada";
			case "RECHAZADA" -> "Rechazada";
			case "AJUSTES_SOLICITADOS" -> "Ajustes solicitados";
			case "PENDIENTE" -> "Pendiente";
			default -> estado;
		};
	}

	private String color(String estado) {
		return switch (estado) {
			case "APROBADA" -> "#15803d";
			case "RECHAZADA" -> "#b91c1c";
			case "AJUSTES_SOLICITADOS" -> "#b45309";
			default -> "#1f2937";
		};
	}

	private String asunto(String tipo, String estado) {
		return switch (estado) {
			case "APROBADA" -> "Tu " + tipo + " fue aprobada";
			case "RECHAZADA" -> "Tu " + tipo + " fue rechazada";
			case "AJUSTES_SOLICITADOS" -> "Tu " + tipo + " requiere ajustes";
			default -> "Actualización de tu " + tipo;
		};
	}
}
