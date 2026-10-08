package com.example.proyecto.global.service;

import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Parcial;
import com.example.proyecto.global.domain.Periodo;
import com.example.proyecto.global.dto.ParcialCreateRequest;
import com.example.proyecto.global.dto.ParcialUpdateRequest;
import com.example.proyecto.global.exception.FechasInvalidasException;
import com.example.proyecto.global.exception.RegistroDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.ParcialRepository;
import com.example.proyecto.global.repository.PeriodoRepository;

@Service
public class ParcialService {

	private final ParcialRepository parcialRepository;
	private final PeriodoRepository periodoRepository;

	public ParcialService(ParcialRepository parcialRepository, PeriodoRepository periodoRepository) {
		this.parcialRepository = parcialRepository;
		this.periodoRepository = periodoRepository;
	}

	@Transactional(readOnly = true)
	public Page<Parcial> listar(UUID periodoId, String q, Boolean activo, LocalDate fechaInicioDesde,
			LocalDate fechaFinHasta, Pageable pageable) {
		return parcialRepository.buscar(periodoId, q, activo, fechaInicioDesde, fechaFinHasta, pageable);
	}

	@Transactional(readOnly = true)
	public Parcial buscarPorId(UUID id) {
		return parcialRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("Parcial no encontrado: " + id));
	}

	@Transactional
	public Parcial crear(ParcialCreateRequest request) {
		Periodo periodo = periodoRepository.findById(request.periodoId())
				.orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado: " + request.periodoId()));
		validarFechasDentroPeriodo(periodo, request.fechaInicio(), request.fechaFin());
		String nombre = request.nombre() == null ? "" : request.nombre().trim();
		if (nombre.isBlank()) {
			throw new RegistroDuplicadoException("El nombre del parcial es obligatorio");
		}
		if (parcialRepository.existsByPeriodoIdAndNombreIgnoreCase(periodo.getId(), nombre)) {
			throw new RegistroDuplicadoException("Ya existe un parcial con ese nombre en el periodo");
		}
		Parcial pr = new Parcial(periodo, nombre, request.fechaInicio(), request.fechaFin());
		return parcialRepository.save(pr);
	}

	@Transactional
	public Parcial actualizar(UUID id, ParcialUpdateRequest request) {
		Parcial pr = buscarPorId(id);
		Periodo periodo = periodoRepository.findById(request.periodoId())
				.orElseThrow(() -> new ResourceNotFoundException("Periodo no encontrado: " + request.periodoId()));
		validarFechasDentroPeriodo(periodo, request.fechaInicio(), request.fechaFin());
		String nombre = request.nombre() == null ? "" : request.nombre().trim();
		if (nombre.isBlank()) {
			throw new RegistroDuplicadoException("El nombre del parcial es obligatorio");
		}
		boolean cambiaPeriodo = !periodo.getId().equals(pr.getPeriodo().getId());
		boolean cambiaNombre = !nombre.equalsIgnoreCase(pr.getNombre());
		if ((cambiaPeriodo || cambiaNombre)
				&& parcialRepository.existsByPeriodoIdAndNombreIgnoreCase(periodo.getId(), nombre)) {
			throw new RegistroDuplicadoException("Ya existe un parcial con ese nombre en el periodo");
		}
		pr.setPeriodo(periodo);
		pr.actualizar(nombre, request.fechaInicio(), request.fechaFin(), request.activo());
		return pr;
	}

	@Transactional
	public void desactivar(UUID id) {
		Parcial pr = buscarPorId(id);
		if (!pr.isActivo()) {
			return;
		}
		pr.setActivo(false);
	}

	@Transactional
	public Parcial reactivar(UUID id) {
		Parcial pr = buscarPorId(id);
		pr.setActivo(true);
		return pr;
	}

	private void validarFechasDentroPeriodo(Periodo periodo, LocalDate inicio, LocalDate fin) {
		if (inicio == null || fin == null) {
			throw new FechasInvalidasException("Las fechas de inicio y fin son obligatorias");
		}
		if (fin.isBefore(inicio)) {
			throw new FechasInvalidasException("La fecha de fin no puede ser anterior a la fecha de inicio");
		}
		if (periodo.getFechaInicio() != null && inicio.isBefore(periodo.getFechaInicio())) {
			throw new FechasInvalidasException("La fecha de inicio del parcial debe estar dentro del periodo");
		}
		if (periodo.getFechaFin() != null && fin.isAfter(periodo.getFechaFin())) {
			throw new FechasInvalidasException("La fecha de fin del parcial debe estar dentro del periodo");
		}
	}
}