package com.example.proyecto.global.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.domain.EstadoPlaneacion;
import com.example.proyecto.global.dto.PaginadoResponse;
import com.example.proyecto.global.dto.PlaneacionCreateRequest;
import com.example.proyecto.global.dto.PlaneacionResponse;
import com.example.proyecto.global.dto.PlaneacionUpdateRequest;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.PlaneacionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/planeaciones")
@Tag(name = "Planeaciones")
public class PlaneacionController {

	private final PlaneacionService planeacionService;

	public PlaneacionController(PlaneacionService planeacionService) {
		this.planeacionService = planeacionService;
	}

	@PostMapping
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Registra una planeacion didactica. Queda en estado PENDIENTE")
	public ResponseEntity<PlaneacionResponse> crear(@Valid @RequestBody PlaneacionCreateRequest request,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		PlaneacionResponse creado = PlaneacionResponse.from(planeacionService.crear(request, principal.getId()));
		return ResponseEntity.created(URI.create("/api/planeaciones/" + creado.id())).body(creado);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista planeaciones. El docente solo ve las suyas; coordinacion y direccion pueden filtrar por docente, materia, parcial o estado")
	public PaginadoResponse<PlaneacionResponse> listar(
			@RequestParam(required = false) UUID docenteId,
			@RequestParam(required = false) UUID materiaId,
			@RequestParam(required = false) UUID parcialId,
			@RequestParam(required = false) EstadoPlaneacion estado,
			@RequestParam(required = false) String q,
			@AuthenticationPrincipal UsuarioPrincipal principal,
			@PageableDefault(size = 20, sort = "createdAt") Pageable pageable) {
		return PaginadoResponse.de(planeacionService
				.listar(docenteId, materiaId, parcialId, estado, q, principal.getRol(), principal.getId(), pageable)
				.map(PlaneacionResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Consulta una planeacion. El docente propietario o los roles de revision")
	public PlaneacionResponse buscar(@PathVariable UUID id,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return PlaneacionResponse
				.from(planeacionService.buscarPorId(id, principal.getRol(), principal.getId()));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Actualiza una planeacion propia mientras este en estado PENDIENTE")
	public PlaneacionResponse actualizar(@PathVariable UUID id,
			@Valid @RequestBody PlaneacionUpdateRequest request,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return PlaneacionResponse
				.from(planeacionService.actualizar(id, request, principal.getRol(), principal.getId()));
	}
}