package com.example.proyecto.global.service;

import java.util.List;
import java.util.UUID;

import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.AvanceParcial;
import com.example.proyecto.global.domain.Planeacion;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.AvanceRequest;
import com.example.proyecto.global.dto.AvanceResponse;
import com.example.proyecto.global.exception.RegistroDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.AvanceParcialRepository;
import com.example.proyecto.global.repository.PlaneacionRepository;
import com.example.proyecto.global.repository.UsuarioRepository;
import com.example.proyecto.global.security.PoliticaAccesoDatos;

@Service
public class AvanceService {

	private final AvanceParcialRepository avanceRepository;
	private final PlaneacionRepository planeacionRepository;
	private final UsuarioRepository usuarioRepository;
	private final PoliticaAccesoDatos politica;

	public AvanceService(AvanceParcialRepository avanceRepository,
			PlaneacionRepository planeacionRepository,
			UsuarioRepository usuarioRepository,
			PoliticaAccesoDatos politica) {
		this.avanceRepository = avanceRepository;
		this.planeacionRepository = planeacionRepository;
		this.usuarioRepository = usuarioRepository;
		this.politica = politica;
	}

	/**
	 * El docente propietario registra el avance de su planeacion en el parcial.
	 * El parcial del avance es el de la planeacion.
	 */
	@Transactional
	public AvanceResponse registrar(UUID planeacionId, AvanceRequest request, UUID idSolicitante) {
		Planeacion planeacion = planeacionRepository.findById(planeacionId)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + planeacionId));
		if (!politica.puedeModificarDatosDe(idSolicitante, planeacion.getDocente().getId())) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede registrar el avance");
		}
		UUID parcialId = planeacion.getParcial().getId();
		if (avanceRepository.existsByPlaneacionIdAndParcialId(planeacionId, parcialId)) {
			throw new RegistroDuplicadoException(
					"Ya existe un avance registrado para esta planeacion en el parcial");
		}
		Usuario docente = usuarioRepository.findById(idSolicitante)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idSolicitante));

		AvanceParcial avance = new AvanceParcial(
				planeacion,
				planeacion.getParcial(),
				request.estado(),
				normalizar(request.observaciones()),
				docente);
		return AvanceResponse.from(avanceRepository.save(avance));
	}

	/**
	 * El docente propietario y los roles de revision pueden consultar los
	 * avances de una planeacion.
	 */
	@Transactional(readOnly = true)
	public List<AvanceResponse> listarPorPlaneacion(UUID planeacionId, String rolSolicitante, UUID idSolicitante) {
		Planeacion planeacion = planeacionRepository.findById(planeacionId)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + planeacionId));
		verificarAccesoLectura(planeacion, rolSolicitante, idSolicitante);
		return avanceRepository.listarPorPlaneacion(planeacionId).stream()
				.map(AvanceResponse::from)
				.toList();
	}

	@Transactional(readOnly = true)
	public AvanceResponse buscarPorId(UUID avanceId, String rolSolicitante, UUID idSolicitante) {
		AvanceParcial avance = obtener(avanceId);
		verificarAccesoLectura(avance.getPlaneacion(), rolSolicitante, idSolicitante);
		return AvanceResponse.from(avance);
	}

	/**
	 * Solo el docente propietario actualiza el avance de su planeacion.
	 */
	@Transactional
	public AvanceResponse actualizar(UUID avanceId, AvanceRequest request, UUID idSolicitante) {
		AvanceParcial avance = obtener(avanceId);
		if (!politica.puedeModificarDatosDe(idSolicitante, avance.getPlaneacion().getDocente().getId())) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede actualizar el avance");
		}
		avance.actualizar(request.estado(), normalizar(request.observaciones()));
		return AvanceResponse.from(avance);
	}

	/**
	 * Solo el docente propietario elimina el avance de su planeacion.
	 */
	@Transactional
	public void eliminar(UUID avanceId, UUID idSolicitante) {
		AvanceParcial avance = obtener(avanceId);
		if (!politica.puedeModificarDatosDe(idSolicitante, avance.getPlaneacion().getDocente().getId())) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede eliminar el avance");
		}
		avanceRepository.delete(avance);
	}

	private AvanceParcial obtener(UUID avanceId) {
		return avanceRepository.buscarPorIdConRelaciones(avanceId)
				.orElseThrow(() -> new ResourceNotFoundException("Avance no encontrado: " + avanceId));
	}

	private void verificarAccesoLectura(Planeacion planeacion, String rolSolicitante, UUID idSolicitante) {
		if (!politica.puedeConsultarDatosDe(rolSolicitante, idSolicitante, planeacion.getDocente().getId())) {
			throw new AuthorizationDeniedException("No tienes permisos para consultar los avances de esta planeacion");
		}
	}

	private String normalizar(String texto) {
		if (texto == null) {
			return null;
		}
		String limpio = texto.trim();
		return limpio.isEmpty() ? null : limpio;
	}
}
