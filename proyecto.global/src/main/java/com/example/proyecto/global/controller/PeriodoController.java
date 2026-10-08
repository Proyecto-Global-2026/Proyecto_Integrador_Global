package com.example.proyecto.global.controller;

import java.net.URI;
import java.time.LocalDate;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.PaginadoResponse;
import com.example.proyecto.global.dto.PeriodoCreateRequest;
import com.example.proyecto.global.dto.PeriodoResponse;
import com.example.proyecto.global.dto.PeriodoUpdateRequest;
import com.example.proyecto.global.service.PeriodoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/periodos")
@Tag(name = "Periodos")
public class PeriodoController {

	private final PeriodoService periodoService;

	public PeriodoController(PeriodoService periodoService) {
		this.periodoService = periodoService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista periodos. Filtra por texto, estado activo y rango de fechas")
	public PaginadoResponse<PeriodoResponse> listar(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Boolean activo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinHasta,
			@PageableDefault(size = 20, sort = "fechaInicio") Pageable pageable) {
		return PaginadoResponse
				.de(periodoService.listar(q, activo, fechaInicioDesde, fechaFinHasta, pageable)
						.map(PeriodoResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Consulta un periodo por id")
	public PeriodoResponse buscar(@PathVariable UUID id) {
		return PeriodoResponse.from(periodoService.buscarPorId(id));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Crea un periodo")
	public ResponseEntity<PeriodoResponse> crear(@Valid @RequestBody PeriodoCreateRequest request) {
		PeriodoResponse creado = PeriodoResponse.from(periodoService.crear(request));
		return ResponseEntity.created(URI.create("/api/periodos/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Actualiza un periodo")
	public PeriodoResponse actualizar(@PathVariable UUID id, @Valid @RequestBody PeriodoUpdateRequest request) {
		return PeriodoResponse.from(periodoService.actualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Da de baja logica un periodo")
	public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
		periodoService.desactivar(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/reactivar")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Reactiva un periodo")
	public PeriodoResponse reactivar(@PathVariable UUID id) {
		return PeriodoResponse.from(periodoService.reactivar(id));
	}
}