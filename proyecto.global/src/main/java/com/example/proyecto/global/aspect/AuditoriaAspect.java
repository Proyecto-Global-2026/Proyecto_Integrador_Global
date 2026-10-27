package com.example.proyecto.global.aspect;

import java.util.UUID;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.proyecto.global.domain.AuditoriaEvento;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.AuditoriaService;

import jakarta.servlet.http.HttpServletRequest;

@Aspect
@Component
public class AuditoriaAspect {

	private final AuditoriaService auditoriaService;

	public AuditoriaAspect(AuditoriaService auditoriaService) {
		this.auditoriaService = auditoriaService;
	}

	@AfterReturning(pointcut = "execution(* com.example.proyecto.global.controller..*.*(..))", returning = "result")
	public void auditarDatosPersonales(JoinPoint joinPoint, Object result) {
		try {
			HttpServletRequest request = obtenerRequest();
			UsuarioPrincipal principal = obtenerPrincipal();
			String method = request == null ? null : request.getMethod();
			String uri = request == null ? null : request.getRequestURI();

			AuditoriaEvento.Accion accion = mapAccion(method, uri);
			if (accion == null) {
				return;
			}

			String recurso = recurso(uri);
			UUID recursoId = extraerId(uri);

			auditoriaService.registrar(accion, AuditoriaEvento.Resultado.EXITOSO, recurso, recursoId,
					"Acceso/modificacion a datos personales", request, principal);
		} catch (Exception ex) {
			// Nunca bloquear el negocio por auditoria
		}
	}

	private AuditoriaEvento.Accion mapAccion(String method, String uri) {
		if (method == null) {
			return null;
		}
		if ("GET".equals(method)) {
			if (uri != null && uri.contains("/arco/acceso")) {
				return AuditoriaEvento.Accion.ACCESO;
			}
			return AuditoriaEvento.Accion.LECTURA;
		}
		if ("POST".equals(method)) {
			if (uri != null && uri.contains("/arco/oposicion")) {
				return AuditoriaEvento.Accion.OPOSICION;
			}
			if (uri != null && uri.contains("/arco/cancelacion")) {
				return AuditoriaEvento.Accion.CANCELACION;
			}
			if (uri != null && uri.contains("/revisiones")) {
				return AuditoriaEvento.Accion.ACTUALIZACION;
			}
			return AuditoriaEvento.Accion.CREACION;
		}
		if ("PUT".equals(method) || "PATCH".equals(method)) {
			if (uri != null && uri.contains("/arco/rectificacion")) {
				return AuditoriaEvento.Accion.RECTIFICACION;
			}
			return AuditoriaEvento.Accion.ACTUALIZACION;
		}
		if ("DELETE".equals(method)) {
			return AuditoriaEvento.Accion.ELIMINACION;
		}
		return null;
	}

	private String recurso(String uri) {
		if (uri == null) {
			return "DESCONOCIDO";
		}
		if (uri.contains("/api/usuarios")) {
			return "USUARIOS";
		}
		if (uri.contains("/api/planeaciones")) {
			return "PLANEACIONES";
		}
		if (uri.contains("/api/evidencias")) {
			return "EVIDENCIAS";
		}
		if (uri.contains("/api/reportes")) {
			return "REPORTES";
		}
		if (uri.contains("/api/archivos")) {
			return "ARCHIVOS";
		}
		if (uri.contains("/api/calendario")) {
			return "CALENDARIO";
		}
		if (uri.contains("/api/auditoria")) {
			return "AUDITORIA";
		}
		return "OTRO";
	}

	private UUID extraerId(String uri) {
		if (uri == null) {
			return null;
		}
		String[] parts = uri.split("/");
		for (String part : parts) {
			if (part != null && part.length() == 36 && part.contains("-")) {
				try {
					return UUID.fromString(part);
				} catch (Exception ex) {
					// ignorar
				}
			}
		}
		return null;
	}

	private HttpServletRequest obtenerRequest() {
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			return attrs.getRequest();
		} catch (Exception ex) {
			return null;
		}
	}

	private UsuarioPrincipal obtenerPrincipal() {
		try {
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
				return null;
			}
			Object principal = auth.getPrincipal();
			if (principal instanceof UsuarioPrincipal up) {
				return up;
			}
			return null;
		} catch (Exception ex) {
			return null;
		}
	}
}
