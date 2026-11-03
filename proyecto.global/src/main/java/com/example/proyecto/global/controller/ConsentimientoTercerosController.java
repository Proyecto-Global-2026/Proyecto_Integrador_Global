package com.example.proyecto.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.ConsentimientoTerceroRequest;
import com.example.proyecto.global.dto.ConsentimientoTerceroResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.ConsentimientoTercerosService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/consentimientos/terceros")
@Tag(name = "Consentimiento transferencias a terceros")
@SecurityRequirement(name = "bearerAuth")
public class ConsentimientoTercerosController {

	private final ConsentimientoTercerosService service;

	public ConsentimientoTercerosController(ConsentimientoTercerosService service) {
		this.service = service;
	}

	@GetMapping
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Consultar estado de consentimiento para un tercero")
	public ResponseEntity<ConsentimientoTerceroResponse> obtener(@AuthenticationPrincipal UsuarioPrincipal principal,
			@RequestParam String tercero) {
		return ResponseEntity.ok(service.obtener(principal.getId(), tercero));
	}

	@PostMapping("/otorgar")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Otorgar consentimiento para transferencia a tercero")
	public ResponseEntity<ConsentimientoTerceroResponse> otorgar(@AuthenticationPrincipal UsuarioPrincipal principal,
			@Valid @RequestBody ConsentimientoTerceroRequest request) {
		return ResponseEntity.ok(service.otorgar(principal.getId(), request));
	}

	@PostMapping("/revocar")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Revocar consentimiento para transferencia a tercero")
	public ResponseEntity<ConsentimientoTerceroResponse> revocar(@AuthenticationPrincipal UsuarioPrincipal principal,
			@Valid @RequestBody ConsentimientoTerceroRequest request) {
		return ResponseEntity.ok(service.revocar(principal.getId(), request));
	}
}