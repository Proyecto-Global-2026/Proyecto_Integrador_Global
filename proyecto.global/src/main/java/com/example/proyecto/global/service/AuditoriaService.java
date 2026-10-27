package com.example.proyecto.global.service;

import java.time.OffsetDateTime;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.AuditoriaEvento;
import com.example.proyecto.global.dto.AuditoriaFiltroRequest;
import com.example.proyecto.global.repository.AuditoriaEventoRepository;
import com.example.proyecto.global.security.UsuarioPrincipal;

import jakarta.persistence.criteria.Predicate;
import jakarta.servlet.http.HttpServletRequest;

@Service
public class AuditoriaService {

	private final AuditoriaEventoRepository auditoriaEventoRepository;

	public AuditoriaService(AuditoriaEventoRepository auditoriaEventoRepository) {
		this.auditoriaEventoRepository = auditoriaEventoRepository;
	}

	@Transactional(propagation = Propagation.REQUIRES_NEW)
	public void registrar(AuditoriaEvento.Accion accion, AuditoriaEvento.Resultado resultado, String recurso,
			UUID recursoId, String descripcion, HttpServletRequest request, UsuarioPrincipal principal) {
		AuditoriaEvento evento = new AuditoriaEvento();
		if (principal != null) {
			evento.setUsuarioId(principal.getId());
			evento.setRol(principal.getRol());
			evento.setUsuarioEmailMask(mask(principal.getUsername()));
		}
		evento.setAccion(accion);
		evento.setResultado(resultado);
		evento.setRecurso(recurso);
		evento.setRecursoId(recursoId);
		evento.setDescripcion(descripcion);
		evento.setMetodo(request == null ? null : request.getMethod());
		evento.setRuta(request == null ? null : request.getRequestURI());
		evento.setIp(obtenerIp(request));
		evento.setUserAgent(truncar(request == null ? null : request.getHeader("User-Agent"), 500));
		evento.setFechaEvento(OffsetDateTime.now());
		try {
			auditoriaEventoRepository.save(evento);
		} catch (Exception ex) {
			// No interrumpir el flujo por fallo de auditoria
		}
	}

	public void registrarSync(AuditoriaEvento.Accion accion, AuditoriaEvento.Resultado resultado, String recurso,
			UUID recursoId, String descripcion, HttpServletRequest request, UsuarioPrincipal principal) {
		registrar(accion, resultado, recurso, recursoId, descripcion, request, principal);
	}

	@Transactional(readOnly = true)
	public Page<AuditoriaEvento> listar(AuditoriaFiltroRequest filtro, Pageable pageable) {
		return auditoriaEventoRepository.findAll(filtros(filtro), pageable);
	}

	private Specification<AuditoriaEvento> filtros(AuditoriaFiltroRequest filtro) {
		return (root, query, builder) -> {
			Predicate condicion = builder.conjunction();
			if (filtro == null) {
				return condicion;
			}
			if (filtro.usuarioId() != null) {
				condicion = builder.and(condicion, builder.equal(root.get("usuarioId"), filtro.usuarioId()));
			}
			if (filtro.rol() != null && !filtro.rol().isBlank()) {
				condicion = builder.and(condicion, builder.equal(root.get("rol"), filtro.rol().toUpperCase()));
			}
			if (filtro.recurso() != null && !filtro.recurso().isBlank()) {
				condicion = builder.and(condicion, builder.like(builder.lower(root.get("recurso")),
						"%" + filtro.recurso().toLowerCase() + "%"));
			}
			if (filtro.accion() != null && !filtro.accion().isBlank()) {
				condicion = builder.and(condicion, builder.equal(root.get("accion"),
						AuditoriaEvento.Accion.valueOf(filtro.accion().toUpperCase())));
			}
			if (filtro.resultado() != null && !filtro.resultado().isBlank()) {
				condicion = builder.and(condicion, builder.equal(root.get("resultado"),
						AuditoriaEvento.Resultado.valueOf(filtro.resultado().toUpperCase())));
			}
			if (filtro.desde() != null) {
				condicion = builder.and(condicion, builder.greaterThanOrEqualTo(root.get("fechaEvento"), filtro.desde()));
			}
			if (filtro.hasta() != null) {
				condicion = builder.and(condicion, builder.lessThanOrEqualTo(root.get("fechaEvento"), filtro.hasta()));
			}
			return condicion;
		};
	}

	private String mask(String email) {
		if (email == null || email.isBlank()) {
			return null;
		}
		String e = email.trim().toLowerCase();
		int at = e.indexOf('@');
		if (at <= 1) {
			return "***" + (at >= 0 ? e.substring(at) : "");
		}
		return e.substring(0, 2) + "***" + e.substring(at);
	}

	private String obtenerIp(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String ip = request.getHeader("X-Forwarded-For");
		if (ip != null && !ip.isBlank()) {
			String[] parts = ip.split(",");
			return truncar(parts[0].trim(), 45);
		}
		ip = request.getHeader("X-Real-IP");
		if (ip != null && !ip.isBlank()) {
			return truncar(ip.trim(), 45);
		}
		return truncar(request.getRemoteAddr(), 45);
	}

	private String truncar(String value, int max) {
		if (value == null) {
			return null;
		}
		if (value.length() <= max) {
			return value;
		}
		return value.substring(0, max);
	}
}
