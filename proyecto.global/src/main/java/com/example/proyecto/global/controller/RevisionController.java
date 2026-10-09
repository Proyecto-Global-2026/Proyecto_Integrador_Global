package com.example.proyecto.global.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.RevisionRequest;
import com.example.proyecto.global.dto.RevisionResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.RevisionService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/planeaciones/{planeacionId}/revisiones")
@Tag(name = "Revisiones de planeaciones")
public class RevisionController {

	private final RevisionService revisionService;

	public RevisionController(RevisionService revisionService) {
		this.revisionService = revisionService;
	}

	@PostMapping
	@PreAuthorize("hasRole('COORDINADOR')")
	@Operation(summary = "Aprueba, rechaza o solicita ajustes de una planeacion. Solo COORDINADOR")
	public ResponseEntity<RevisionResponse> revisar(@PathVariable UUID planeacionId,
			@Valid @RequestBody RevisionRequest request,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		RevisionResponse creada = revisionService.revisar(planeacionId, request, principal.getId());
		return ResponseEntity.created(URI.create("/api/planeaciones/" + planeacionId + "/revisiones"))
				.body(creada);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Historial de revisiones. El docente dueño o los roles de revision")
	public List<RevisionResponse> historial(@PathVariable UUID planeacionId,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return revisionService.historial(planeacionId, principal.getRol(), principal.getId());
	}
}