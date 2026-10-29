package com.example.proyecto.global.service;

import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import com.example.proyecto.global.domain.ConsentimientoTerceros;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.ConsentimientoTerceroRequest;
import com.example.proyecto.global.dto.ConsentimientoTerceroResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.ConsentimientoTercerosRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;

@Service
public class ConsentimientoTercerosService {

	private final ConsentimientoTercerosRepository consentimientoRepository;
	private final UsuarioRepository usuarioRepository;

	public ConsentimientoTercerosService(ConsentimientoTercerosRepository consentimientoRepository,
			UsuarioRepository usuarioRepository) {
		this.consentimientoRepository = consentimientoRepository;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional(readOnly = true)
	public ConsentimientoTerceroResponse obtener(UUID usuarioId, String tercero) {
		ConsentimientoTerceros.Tercero t = parse(tercero);
		return consentimientoRepository.findByUsuarioIdAndTercero(usuarioId, t)
				.map(ConsentimientoTerceroResponse::from)
				.orElseGet(() -> new ConsentimientoTerceroResponse(null, usuarioId, t.name(), false, null, null,
						null, null, null, null));
	}

	@Transactional
	public ConsentimientoTerceroResponse otorgar(UUID usuarioId, ConsentimientoTerceroRequest request) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
		ConsentimientoTerceros.Tercero t = parse(request.tercero());
		if (usuario.isAnonimizado()) {
			throw new BusinessRuleException("No es posible gestionar consentimiento para una cuenta anonimizada");
		}

		ConsentimientoTerceros consentimiento = consentimientoRepository.findByUsuarioIdAndTercero(usuarioId, t)
				.orElseGet(() -> new ConsentimientoTerceros(usuario, t));
		HttpServletRequest req = obtenerRequest();
		String ip = obtenerIp(req);
		String userAgent = req == null ? null : req.getHeader("User-Agent");
		if (userAgent != null && userAgent.length() > 500) {
			userAgent = userAgent.substring(0, 500);
		}
		consentimiento.otorgar(request.versionAviso(), ip, userAgent);
		return ConsentimientoTerceroResponse.from(consentimientoRepository.save(consentimiento));
	}

	@Transactional
	public ConsentimientoTerceroResponse revocar(UUID usuarioId, ConsentimientoTerceroRequest request) {
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
		ConsentimientoTerceros.Tercero t = parse(request.tercero());
		if (usuario.isAnonimizado()) {
			throw new BusinessRuleException("No es posible gestionar consentimiento para una cuenta anonimizada");
		}

		ConsentimientoTerceros consentimiento = consentimientoRepository.findByUsuarioIdAndTercero(usuarioId, t)
				.orElseGet(() -> new ConsentimientoTerceros(usuario, t));
		consentimiento.revocar(request.motivoRevocacion());
		return ConsentimientoTerceroResponse.from(consentimientoRepository.save(consentimiento));
	}

	private ConsentimientoTerceros.Tercero parse(String tercero) {
		if (tercero == null || tercero.isBlank()) {
			throw new BusinessRuleException("tercero es obligatorio");
		}
		try {
			return ConsentimientoTerceros.Tercero.valueOf(tercero.trim().toUpperCase());
		} catch (Exception e) {
			throw new BusinessRuleException("tercero no soportado");
		}
	}

	private HttpServletRequest obtenerRequest() {
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.currentRequestAttributes();
			return attrs.getRequest();
		} catch (Exception ex) {
			return null;
		}
	}

	private String obtenerIp(HttpServletRequest request) {
		if (request == null) {
			return null;
		}
		String xff = request.getHeader("X-Forwarded-For");
		if (xff != null && !xff.isBlank()) {
			String[] parts = xff.split(",");
			if (parts.length > 0 && !parts[0].isBlank()) {
				return parts[0].trim();
			}
		}
		String realIp = request.getHeader("X-Real-IP");
		if (realIp != null && !realIp.isBlank()) {
			return realIp.trim();
		}
		return request.getRemoteAddr();
	}
}