package com.example.proyecto.global.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.ArcoAccesoResponse;
import com.example.proyecto.global.dto.ArcoCancelacionRequest;
import com.example.proyecto.global.dto.ArcoOposicionRequest;
import com.example.proyecto.global.dto.ArcoRectificacionRequest;
import com.example.proyecto.global.dto.SolicitudArcoResponse;
import com.example.proyecto.global.dto.UsuarioResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.ArcoService;
import com.example.proyecto.global.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/usuarios/me/arco")
@Tag(name = "Derechos ARCO")
@SecurityRequirement(name = "bearerAuth")
public class ArcoController {

	private final ArcoService arcoService;
	private final UsuarioService usuarioService;

	public ArcoController(ArcoService arcoService, UsuarioService usuarioService) {
		this.arcoService = arcoService;
		this.usuarioService = usuarioService;
	}

	@GetMapping("/acceso")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "ARCO - Acceso: consulta los propios datos personales")
	public ArcoAccesoResponse acceso(@AuthenticationPrincipal UsuarioPrincipal principal) {
		return arcoService.acceder(principal.getId());
	}

	@PutMapping("/rectificacion")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "ARCO - Rectificacion: corrige nombre/correo del titular")
	public UsuarioResponse rectificacion(@AuthenticationPrincipal UsuarioPrincipal principal,
			@Valid @RequestBody ArcoRectificacionRequest request) {
		return UsuarioResponse.from(arcoService.rectificar(principal.getId(), request));
	}

	@PostMapping("/oposicion")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "ARCO - Oposicion: registra oposicion al tratamiento")
	public ResponseEntity<SolicitudArcoResponse> oposicion(@AuthenticationPrincipal UsuarioPrincipal principal,
			@Valid @RequestBody ArcoOposicionRequest request) {
		return ResponseEntity.ok(SolicitudArcoResponse.from(arcoService.oposicion(principal.getId(), request)));
	}

	@PostMapping("/cancelacion")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "ARCO - Cancelacion: bloquea y anonimiza los datos identificables")
	public ResponseEntity<UsuarioResponse> cancelacion(@AuthenticationPrincipal UsuarioPrincipal principal,
			@Valid @RequestBody ArcoCancelacionRequest request) {
		return ResponseEntity.ok(UsuarioResponse.from(arcoService.cancelar(principal.getId(), request)));
	}

	@GetMapping("/solicitudes")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Historial de solicitudes ARCO del titular")
	public List<SolicitudArcoResponse> solicitudes(@AuthenticationPrincipal UsuarioPrincipal principal) {
		return arcoService.listarSolicitudes(principal.getId());
	}

	@GetMapping("/perfil")
	@PreAuthorize("isAuthenticated()")
	@Operation(summary = "Perfil completo del titular para ejercicio de derechos")
	public UsuarioResponse perfil(@AuthenticationPrincipal UsuarioPrincipal principal) {
		return usuarioService.buscarPorId(principal.getId());
	}
}
