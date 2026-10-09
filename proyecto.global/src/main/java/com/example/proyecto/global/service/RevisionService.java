package com.example.proyecto.global.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.EstadoPlaneacion;
import com.example.proyecto.global.domain.Planeacion;
import com.example.proyecto.global.domain.RevisionPlaneacion;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.RevisionRequest;
import com.example.proyecto.global.dto.RevisionResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.PlaneacionRepository;
import com.example.proyecto.global.repository.RevisionPlaneacionRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

@Service
public class RevisionService {

	private final RevisionPlaneacionRepository revisionRepository;
	private final PlaneacionRepository planeacionRepository;
	private final UsuarioRepository usuarioRepository;

	public RevisionService(RevisionPlaneacionRepository revisionRepository,
			PlaneacionRepository planeacionRepository,
			UsuarioRepository usuarioRepository) {
		this.revisionRepository = revisionRepository;
		this.planeacionRepository = planeacionRepository;
		this.usuarioRepository = usuarioRepository;
	}

	/**
	 * El coordinador cambia el estado de una planeacion y queda registrado
	 * el comentario de revision.
	 */
	@Transactional
	public RevisionResponse revisar(UUID planeacionId, RevisionRequest request, UUID coordinadorId) {
		Planeacion planeacion = planeacionRepository.findById(planeacionId)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + planeacionId));
		EstadoPlaneacion destino = request.estado();
		validarTransicion(planeacion.getEstado(), destino, request.comentario());

		Usuario coordinador = usuarioRepository.findById(coordinadorId)
				.orElseThrow(() -> new ResourceNotFoundException("Coordinador no encontrado: " + coordinadorId));

		EstadoPlaneacion anterior = planeacion.getEstado();
		planeacion.cambiarEstado(destino);

		RevisionPlaneacion revision = new RevisionPlaneacion(
				planeacion,
				anterior,
				destino,
				request.comentario() == null ? null : request.comentario().trim(),
				coordinador);
		return RevisionResponse.from(revisionRepository.save(revision));
	}

	/**
	 * Historial de revisiones. El docente dueño y los roles de revision
	 * pueden consultarlo.
	 */
	@Transactional(readOnly = true)
	public List<RevisionResponse> historial(UUID planeacionId, String rolSolicitante, UUID idSolicitante) {
		Planeacion planeacion = planeacionRepository.findById(planeacionId)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + planeacionId));
		boolean esPropietario = planeacion.getDocente().getId().equals(idSolicitante);
		boolean esRevision = "COORDINADOR".equals(rolSolicitante) || "DIRECCION".equals(rolSolicitante);
		if (!esPropietario && !esRevision) {
			throw new AuthorizationDeniedException("No tienes permisos para consultar el historial de esta planeacion");
		}
		return revisionRepository.listarPorPlaneacion(planeacionId).stream()
				.map(RevisionResponse::from)
				.toList();
	}

	private void validarTransicion(EstadoPlaneacion actual, EstadoPlaneacion destino, String comentario) {
		if (destino == EstadoPlaneacion.PENDIENTE) {
			throw new BusinessRuleException("El estado PENDIENTE lo asigna el sistema, no se puede asignar en revision");
		}
		if (actual == EstadoPlaneacion.APROBADA || actual == EstadoPlaneacion.RECHAZADA) {
			throw new BusinessRuleException(
					"La planeacion ya tiene un estado final (" + actual.name() + ") y no se puede revisar de nuevo");
		}
		if ((destino == EstadoPlaneacion.RECHAZADA || destino == EstadoPlaneacion.AJUSTES_SOLICITADOS)
				&& (comentario == null || comentario.isBlank())) {
			throw new BusinessRuleException("El comentario es obligatorio al rechazar o solicitar ajustes");
		}
	}
}