package com.example.proyecto.global.service;

import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Materia;
import com.example.proyecto.global.dto.MateriaCreateRequest;
import com.example.proyecto.global.dto.MateriaUpdateRequest;
import com.example.proyecto.global.exception.RegistroDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.MateriaRepository;

@Service
public class MateriaService {

	private final MateriaRepository materiaRepository;

	public MateriaService(MateriaRepository materiaRepository) {
		this.materiaRepository = materiaRepository;
	}

	@Transactional(readOnly = true)
	public Page<Materia> listar(String q, Boolean activo, Pageable pageable) {
		return materiaRepository.buscar(q, activo, pageable);
	}

	@Transactional(readOnly = true)
	public Materia buscarPorId(UUID id) {
		return materiaRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada: " + id));
	}

	@Transactional
	public Materia crear(MateriaCreateRequest request) {
		String codigoNormalizado = Materia.normalizarCodigo(request.codigo());
		if (codigoNormalizado == null || codigoNormalizado.isBlank()) {
			throw new RegistroDuplicadoException("El codigo de materia es obligatorio");
		}
		if (materiaRepository.existsByCodigoIgnoreCase(codigoNormalizado)) {
			throw new RegistroDuplicadoException("Ya existe una materia con ese codigo");
		}
		Materia m = new Materia(request.nombre().trim(), codigoNormalizado, request.descripcion());
		return materiaRepository.save(m);
	}

	@Transactional
	public Materia actualizar(UUID id, MateriaUpdateRequest request) {
		Materia m = buscarPorId(id);
		String codigoNormalizado = Materia.normalizarCodigo(request.codigo());
		if (codigoNormalizado == null || codigoNormalizado.isBlank()) {
			throw new RegistroDuplicadoException("El codigo de materia es obligatorio");
		}
		if (!codigoNormalizado.equalsIgnoreCase(m.getCodigo())
				&& materiaRepository.existsByCodigoIgnoreCase(codigoNormalizado)) {
			throw new RegistroDuplicadoException("Ya existe una materia con ese codigo");
		}
		m.actualizar(request.nombre().trim(), codigoNormalizado, request.descripcion(), request.activo());
		return m;
	}

	@Transactional
	public void desactivar(UUID id) {
		Materia m = buscarPorId(id);
		if (!m.isActivo()) {
			return;
		}
		m.setActivo(false);
	}

	@Transactional
	public Materia reactivar(UUID id) {
		Materia m = buscarPorId(id);
		m.setActivo(true);
		return m;
	}
}