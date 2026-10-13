package com.example.proyecto.global.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.AvanceRequest;
import com.example.proyecto.global.dto.AvanceResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.AvanceService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/planeaciones/{planeacionId}/avances")
@Tag(name = "Avances por parcial")
public class AvanceController {

	private final AvanceService avanceService;

	public AvanceController(AvanceService avanceService) {
		this.avanceService = avanceService;
	}

	@PostMapping
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Registra el avance de una planeacion propia en su parcial. El parcial se toma de la planeacion")
	public ResponseEntity<AvanceResponse> registrar(@PathVariable UUID planeacionId,
			@Valid @RequestBody AvanceRequest request,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		AvanceResponse creado = avanceService.registrar(planeacionId, request, principal.getId());
		return ResponseEntity.created(URI.create("/api/planeaciones/" + planeacionId + "/avances/" + creado.id()))
				.body(creado);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista los avances de una planeacion. El docente propietario o los roles de revision")
	public List<AvanceResponse> listar(@PathVariable UUID planeacionId,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return avanceService.listarPorPlaneacion(planeacionId, principal.getRol(), principal.getId());
	}

	@GetMapping("/{avanceId}")
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Consulta un avance por su id")
	public AvanceResponse buscar(@PathVariable UUID planeacionId,
			@PathVariable UUID avanceId,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return avanceService.buscarPorId(avanceId, principal.getRol(), principal.getId());
	}

	@PutMapping("/{avanceId}")
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Actualiza el estado u observaciones de un avance propio")
	public AvanceResponse actualizar(@PathVariable UUID planeacionId,
			@PathVariable UUID avanceId,
			@Valid @RequestBody AvanceRequest request,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return avanceService.actualizar(avanceId, request, principal.getId());
	}

	@DeleteMapping("/{avanceId}")
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Elimina un avance propio")
	public ResponseEntity<Void> eliminar(@PathVariable UUID planeacionId,
			@PathVariable UUID avanceId,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		avanceService.eliminar(avanceId, principal.getId());
		return ResponseEntity.noContent().build();
	}
}
