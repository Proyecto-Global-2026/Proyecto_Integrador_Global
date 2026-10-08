package com.example.proyecto.global.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Periodo;
import com.example.proyecto.global.dto.PeriodoCreateRequest;
import com.example.proyecto.global.dto.PeriodoUpdateRequest;
import com.example.proyecto.global.exception.FechasInvalidasException;
import com.example.proyecto.global.exception.RegistroDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.PeriodoRepository;

@Service
public class PeriodoService {

	private final PeriodoRepository periodoRepository;

	public PeriodoService(PeriodoRepository periodoRepository) {
		this.periodoRepository = periodoRepository;
	}

	@Transactional(readOnly = true)
	public Page<Periodo> listar(String q, Boolean activo, LocalDate fechaInicioDesde, LocalDate fechaFinHasta,
			Pageable pageable) {
		return periodoRepository.buscar(q, activo, fechaInicioDesde, fechaFinHasta, pageable);
	}

	@Transactional(readOnly = true)
	public Periodo buscarPorId(UUID id) {
		return periodoRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado: " + id));
	}

	@Transactional
	public Periodo crear(PeriodoCreateRequest request) {
		validarFechas(request.fechaInicio(), request.fechaFin());
		String nombre = request.nombre() == null ? "" : request.nombre().trim();
		if (nombre.isBlank()) {
			throw new RegistroDuplicadoException("El nombre del periodo es obligatorio");
		}
		if (periodoRepository.existsByNombreIgnoreCase(nombre)) {
			throw new RegistroDuplicadoException("Ya existe un periodo con ese nombre");
		}
		Periodo p = new Periodo(nombre, request.fechaInicio(), request.fechaFin());
		return periodoRepository.save(p);
	}

	@Transactional
	public Periodo actualizar(UUID id, PeriodoUpdateRequest request) {
		Periodo p = buscarPorId(id);
		validarFechas(request.fechaInicio(), request.fechaFin());
		String nombre = request.nombre() == null ? "" : request.nombre().trim();
		if (nombre.isBlank()) {
			throw new RegistroDuplicadoException("El nombre del periodo es obligatorio");
		}
		if (!nombre.equalsIgnoreCase(p.getNombre()) && periodoRepository.existsByNombreIgnoreCase(nombre)) {
			throw new RegistroDuplicadoException("Ya existe un periodo con ese nombre");
		}
		p.actualizar(nombre, request.fechaInicio(), request.fechaFin(), request.activo());
		return p;
	}

	@Transactional
	public void desactivar(UUID id) {
		Periodo p = buscarPorId(id);
		if (!p.isActivo()) {
			return;
		}
		p.setActivo(false);
	}

	@Transactional
	public Periodo reactivar(UUID id) {
		Periodo p = buscarPorId(id);
		p.setActivo(true);
		return p;
	}

	private void validarFechas(LocalDate inicio, LocalDate fin) {
		if (inicio == null || fin == null) {
			throw new FechasInvalidasException("Las fechas de inicio y fin son obligatorias");
		}
		if (fin.isBefore(inicio)) {
			throw new FechasInvalidasException("La fecha de fin no puede ser anterior a la fecha de inicio");
		}
	}
}