package com.example.proyecto.global.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.EstadoEvidencia;
import com.example.proyecto.global.domain.Evidencia;
import com.example.proyecto.global.domain.RevisionEvidencia;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.RevisionEvidenciaRequest;
import com.example.proyecto.global.dto.RevisionEvidenciaResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.EvidenciaRepository;
import com.example.proyecto.global.repository.RevisionEvidenciaRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

@Service
public class RevisionEvidenciaService {

	private static final String ROL_COORDINADOR = "COORDINADOR";
	private static final String ROL_DIRECCION = "DIRECCION";

	private final RevisionEvidenciaRepository revisionRepository;
	private final EvidenciaRepository evidenciaRepository;
	private final UsuarioRepository usuarioRepository;

	public RevisionEvidenciaService(RevisionEvidenciaRepository revisionRepository,
			EvidenciaRepository evidenciaRepository,
			UsuarioRepository usuarioRepository) {
		this.revisionRepository = revisionRepository;
		this.evidenciaRepository = evidenciaRepository;
		this.usuarioRepository = usuarioRepository;
	}

	/**
	 * El coordinador aprueba o rechaza una evidencia y queda registrado el
	 * comentario de revision.
	 */
	@Transactional
	public RevisionEvidenciaResponse revisar(UUID evidenciaId, RevisionEvidenciaRequest request, UUID revisorId) {
		Evidencia evidencia = evidenciaRepository.buscarPorIdConUsuario(evidenciaId)
				.orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada: " + evidenciaId));
		EstadoEvidencia destino = request.estado();
		validarTransicion(evidencia.getEstado(), destino, request.comentario());

		Usuario revisor = usuarioRepository.findById(revisorId)
				.orElseThrow(() -> new ResourceNotFoundException("Revisor no encontrado: " + revisorId));

		EstadoEvidencia anterior = evidencia.getEstado();
		evidencia.cambiarEstado(destino);

		RevisionEvidencia revision = new RevisionEvidencia(
				evidencia,
				anterior,
				destino,
				request.comentario() == null ? null : request.comentario().trim(),
				revisor);
		return RevisionEvidenciaResponse.from(revisionRepository.save(revision));
	}

	/**
	 * Historial de revisiones. El docente dueño y los roles de revision
	 * pueden consultarlo.
	 */
	@Transactional(readOnly = true)
	public List<RevisionEvidenciaResponse> historial(UUID evidenciaId, String rolSolicitante, UUID idSolicitante) {
		Evidencia evidencia = evidenciaRepository.buscarPorIdConUsuario(evidenciaId)
				.orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada: " + evidenciaId));
		boolean esPropietario = evidencia.getUsuario().getId().equals(idSolicitante);
		boolean esRevision = ROL_COORDINADOR.equals(rolSolicitante) || ROL_DIRECCION.equals(rolSolicitante);
		if (!esPropietario && !esRevision) {
			throw new AuthorizationDeniedException("No tienes permisos para consultar el historial de esta evidencia");
		}
		return revisionRepository.listarPorEvidencia(evidenciaId).stream()
				.map(RevisionEvidenciaResponse::from)
				.toList();
	}

	private void validarTransicion(EstadoEvidencia actual, EstadoEvidencia destino, String comentario) {
		if (destino == EstadoEvidencia.PENDIENTE) {
			throw new BusinessRuleException("El estado PENDIENTE lo asigna el sistema, no se puede asignar en revision");
		}
		if (actual == EstadoEvidencia.APROBADA || actual == EstadoEvidencia.RECHAZADA) {
			throw new BusinessRuleException(
					"La evidencia ya tiene un estado final (" + actual.name() + ") y no se puede revisar de nuevo");
		}
		if (destino == EstadoEvidencia.RECHAZADA && (comentario == null || comentario.isBlank())) {
			throw new BusinessRuleException("El comentario es obligatorio al rechazar una evidencia");
		}
	}
}
