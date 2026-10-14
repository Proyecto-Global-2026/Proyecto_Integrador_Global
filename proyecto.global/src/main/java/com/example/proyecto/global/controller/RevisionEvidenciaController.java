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

import com.example.proyecto.global.dto.RevisionEvidenciaRequest;
import com.example.proyecto.global.dto.RevisionEvidenciaResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.RevisionEvidenciaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evidencias/{evidenciaId}/revisiones")
@Tag(name = "Revisiones de evidencias")
public class RevisionEvidenciaController {

	private final RevisionEvidenciaService revisionEvidenciaService;

	public RevisionEvidenciaController(RevisionEvidenciaService revisionEvidenciaService) {
		this.revisionEvidenciaService = revisionEvidenciaService;
	}

	@PostMapping
	@PreAuthorize("hasRole('COORDINADOR')")
	@Operation(summary = "Aprueba o rechaza una evidencia. Solo COORDINADOR")
	public ResponseEntity<RevisionEvidenciaResponse> revisar(@PathVariable UUID evidenciaId,
			@Valid @RequestBody RevisionEvidenciaRequest request,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		RevisionEvidenciaResponse creada = revisionEvidenciaService.revisar(evidenciaId, request,
				principal.getId());
		return ResponseEntity.created(URI.create("/api/evidencias/" + evidenciaId + "/revisiones"))
				.body(creada);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Historial de revisiones. El docente dueño o los roles de revision")
	public List<RevisionEvidenciaResponse> historial(@PathVariable UUID evidenciaId,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return revisionEvidenciaService.historial(evidenciaId, principal.getRol(), principal.getId());
	}
}
