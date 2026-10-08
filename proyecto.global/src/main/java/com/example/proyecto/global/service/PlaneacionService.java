package com.example.proyecto.global.service;

import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.EstadoPlaneacion;
import com.example.proyecto.global.domain.Materia;
import com.example.proyecto.global.domain.Parcial;
import com.example.proyecto.global.domain.Planeacion;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.PlaneacionCreateRequest;
import com.example.proyecto.global.dto.PlaneacionUpdateRequest;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.RegistroDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.MateriaRepository;
import com.example.proyecto.global.repository.ParcialRepository;
import com.example.proyecto.global.repository.PlaneacionRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

@Service
public class PlaneacionService {

	private static final String ROL_DOCENTE = "DOCENTE";
	private static final String ROL_COORDINADOR = "COORDINADOR";
	private static final String ROL_DIRECCION = "DIRECCION";

	private final PlaneacionRepository planeacionRepository;
	private final UsuarioRepository usuarioRepository;
	private final MateriaRepository materiaRepository;
	private final ParcialRepository parcialRepository;

	public PlaneacionService(PlaneacionRepository planeacionRepository, UsuarioRepository usuarioRepository,
			MateriaRepository materiaRepository, ParcialRepository parcialRepository) {
		this.planeacionRepository = planeacionRepository;
		this.usuarioRepository = usuarioRepository;
		this.materiaRepository = materiaRepository;
		this.parcialRepository = parcialRepository;
	}

	@Transactional
	public Planeacion crear(PlaneacionCreateRequest request, UUID docenteId) {
		Usuario docente = usuarioRepository.findById(docenteId)
				.orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado: " + docenteId));
		Materia materia = materiaActiva(request.materiaId());
		Parcial parcial = parcialActivo(request.parcialId());
		rechazarSiYaExiste(docenteId, materia.getId(), parcial.getId(), null);

		Planeacion pl = new Planeacion(docente, materia, parcial,
				request.titulo().trim(), request.contenido().trim());
		return planeacionRepository.save(pl);
	}

	/**
	 * Un docente solo ve sus planeaciones. Coordinacion y direccion pueden
	 * consultar todas y filtrar por docente, materia o parcial.
	 */
	@Transactional(readOnly = true)
	public Page<Planeacion> listar(UUID docenteId, UUID materiaId, UUID parcialId, EstadoPlaneacion estado,
			String q, String rolSolicitante, UUID idSolicitante, Pageable pageable) {
		UUID filtroDocente = docenteId;
		if (ROL_DOCENTE.equals(rolSolicitante)) {
			filtroDocente = idSolicitante;
		}
		return planeacionRepository.buscar(filtroDocente, materiaId, parcialId, estado, normalizar(q), pageable);
	}

	@Transactional(readOnly = true)
	public Planeacion buscarPorId(UUID id, String rolSolicitante, UUID idSolicitante) {
		Planeacion pl = obtener(id);
		verificarAccesoLectura(pl, rolSolicitante, idSolicitante);
		return pl;
	}

	@Transactional
	public Planeacion actualizar(UUID id, PlaneacionUpdateRequest request, String rolSolicitante,
			UUID idSolicitante) {
		Planeacion pl = obtener(id);
		if (!pl.getDocente().getId().equals(idSolicitante)) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede editar su planeacion");
		}
		if (pl.getEstado() != EstadoPlaneacion.PENDIENTE) {
			throw new BusinessRuleException(
					"La planeacion solo puede editarse mientras este en estado PENDIENTE. Estado actual: "
							+ pl.getEstado().name());
		}
		Materia materia = materiaActiva(request.materiaId());
		Parcial parcial = parcialActivo(request.parcialId());
		rechazarSiYaExiste(idSolicitante, materia.getId(), parcial.getId(), id);

		pl.actualizar(materia, parcial, request.titulo().trim(), request.contenido().trim());
		return pl;
	}

	private Planeacion obtener(UUID id) {
		return planeacionRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + id));
	}

	private void verificarAccesoLectura(Planeacion pl, String rolSolicitante, UUID idSolicitante) {
		boolean esPropietario = pl.getDocente().getId().equals(idSolicitante);
		boolean esRevision = ROL_COORDINADOR.equals(rolSolicitante) || ROL_DIRECCION.equals(rolSolicitante);
		if (!esPropietario && !esRevision) {
			throw new AuthorizationDeniedException("No tienes permisos para consultar esta planeacion");
		}
	}

	private Materia materiaActiva(UUID materiaId) {
		Materia materia = materiaRepository.findById(materiaId)
				.orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + materiaId));
		if (!materia.isActivo()) {
			throw new BusinessRuleException("La materia no esta activa");
		}
		return materia;
	}

	private Parcial parcialActivo(UUID parcialId) {
		Parcial parcial = parcialRepository.findById(parcialId)
				.orElseThrow(() -> new ResourceNotFoundException("Parcial no encontrado: " + parcialId));
		if (!parcial.isActivo()) {
			throw new BusinessRuleException("El parcial no esta activo");
		}
		return parcial;
	}

	private void rechazarSiYaExiste(UUID docenteId, UUID materiaId, UUID parcialId, UUID idExcluir) {
		boolean duplicada = idExcluir == null
				? planeacionRepository.existsByDocenteIdAndMateriaIdAndParcialId(docenteId, materiaId, parcialId)
				: planeacionRepository.existsByDocenteIdAndMateriaIdAndParcialIdAndIdNot(docenteId, materiaId,
						parcialId, idExcluir);
		if (duplicada) {
			throw new RegistroDuplicadoException(
					"Ya existe una planeacion tuya para esa materia y parcial");
		}
	}

	private String normalizar(String q) {
		return q == null ? null : q.trim().toLowerCase(Locale.ROOT);
	}
}