package com.example.proyecto.global.service;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.List;
import java.util.UUID;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.SolicitudArco;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.ArcoAccesoResponse;
import com.example.proyecto.global.dto.ArcoCancelacionRequest;
import com.example.proyecto.global.dto.ArcoOposicionRequest;
import com.example.proyecto.global.dto.ArcoRectificacionRequest;
import com.example.proyecto.global.dto.SolicitudArcoResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.EmailDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.SolicitudArcoRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

@Service
public class ArcoService {

	private final UsuarioRepository usuarioRepository;
	private final SolicitudArcoRepository solicitudArcoRepository;
	private final PasswordEncoder passwordEncoder;
	private final SecureRandom secureRandom = new SecureRandom();

	public ArcoService(UsuarioRepository usuarioRepository, SolicitudArcoRepository solicitudArcoRepository,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.solicitudArcoRepository = solicitudArcoRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional(readOnly = true)
	public ArcoAccesoResponse acceder(UUID usuarioId) {
		Usuario usuario = obtenerUsuario(usuarioId);
		return ArcoAccesoResponse.from(usuario);
	}

	@Transactional
	public Usuario rectificar(UUID usuarioId, ArcoRectificacionRequest request) {
		Usuario usuario = obtenerUsuario(usuarioId);
		if (usuario.isAnonimizado()) {
			throw new BusinessRuleException("No es posible rectificar una cuenta anonimizada");
		}

		String email = Usuario.normalizarEmail(request.email());
		usuarioRepository.findByEmail(email).ifPresent(existente -> {
			if (!existente.getId().equals(usuarioId)) {
				throw new EmailDuplicadoException(email);
			}
		});

		usuario.actualizar(request.nombre().trim(), email, usuario.getRol());
		usuarioRepository.save(usuario);

		solicitudArcoRepository.save(new SolicitudArco(usuario, SolicitudArco.Tipo.RECTIFICACION,
				"Rectificacion de datos personales",
				"Actualizacion de nombre/correo solicitada por el titular"));
		return usuario;
	}

	@Transactional
	public SolicitudArco oposicion(UUID usuarioId, ArcoOposicionRequest request) {
		Usuario usuario = obtenerUsuario(usuarioId);
		if (usuario.isAnonimizado()) {
			throw new BusinessRuleException("No es posible registrar oposicion para una cuenta anonimizada");
		}

		SolicitudArco solicitud = new SolicitudArco(usuario, SolicitudArco.Tipo.OPOSICION, request.motivo(),
				request.detalles());
		return solicitudArcoRepository.save(solicitud);
	}

	@Transactional
	public Usuario cancelar(UUID usuarioId, ArcoCancelacionRequest request) {
		Usuario usuario = obtenerUsuario(usuarioId);
		if (usuario.isAnonimizado()) {
			throw new BusinessRuleException("La cuenta ya se encuentra anonimizada");
		}

		String nombreAnonimizado = "Usuario ARCO " + usuarioId.toString().substring(0, 8);
		String emailAnonimizado = "arco-" + usuarioId + "@anonimizado.local";
		byte[] random = new byte[32];
		secureRandom.nextBytes(random);
		String passwordHash = passwordEncoder.encode(Base64.getUrlEncoder().withoutPadding().encodeToString(random));

		usuario.anonimizar(nombreAnonimizado, emailAnonimizado, passwordHash, request.motivo());
		usuarioRepository.save(usuario);

		solicitudArcoRepository.save(new SolicitudArco(usuario, SolicitudArco.Tipo.CANCELACION, request.motivo(),
				"Cancelacion/anonimizacion solicitada por el titular. Se bloquea el acceso y se eliminan datos identificables manteniendo integridad referencial."));
		return usuario;
	}

	@Transactional(readOnly = true)
	public List<SolicitudArcoResponse> listarSolicitudes(UUID usuarioId) {
		return solicitudArcoRepository.findByUsuarioIdOrderByFechaSolicitudDesc(usuarioId).stream()
				.map(SolicitudArcoResponse::from).toList();
	}

	private Usuario obtenerUsuario(UUID id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No existe the usuario con id " + id));
	}
}
