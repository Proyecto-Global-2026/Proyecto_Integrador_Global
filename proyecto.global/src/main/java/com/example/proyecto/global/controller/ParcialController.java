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
import com.example.proyecto.global.dto.ParcialCreateRequest;
import com.example.proyecto.global.dto.ParcialResponse;
import com.example.proyecto.global.dto.ParcialUpdateRequest;
import com.example.proyecto.global.service.ParcialService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/parciales")
@Tag(name = "Parciales")
public class ParcialController {

	private final ParcialService parcialService;

	public ParcialController(ParcialService parcialService) {
		this.parcialService = parcialService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista parciales. Filtra por periodo, texto, estado activo y rango de fechas")
	public PaginadoResponse<ParcialResponse> listar(
			@RequestParam(required = false) UUID periodoId,
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Boolean activo,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicioDesde,
			@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFinHasta,
			@PageableDefault(size = 20, sort = "fechaInicio") Pageable pageable) {
		return PaginadoResponse
				.de(parcialService.listar(periodoId, q, activo, fechaInicioDesde, fechaFinHasta, pageable)
						.map(ParcialResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Consulta un parcial por id")
	public ParcialResponse buscar(@PathVariable UUID id) {
		return ParcialResponse.from(parcialService.buscarPorId(id));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Crea un parcial")
	public ResponseEntity<ParcialResponse> crear(@Valid @RequestBody ParcialCreateRequest request) {
		ParcialResponse creado = ParcialResponse.from(parcialService.crear(request));
		return ResponseEntity.created(URI.create("/api/parciales/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Actualiza un parcial")
	public ParcialResponse actualizar(@PathVariable UUID id, @Valid @RequestBody ParcialUpdateRequest request) {
		return ParcialResponse.from(parcialService.actualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Da de baja logica un parcial")
	public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
		parcialService.desactivar(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/reactivar")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Reactiva un parcial")
	public ParcialResponse reactivar(@PathVariable UUID id) {
		return ParcialResponse.from(parcialService.reactivar(id));
	}
}