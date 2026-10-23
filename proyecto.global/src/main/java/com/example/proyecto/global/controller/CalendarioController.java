package com.example.proyecto.global.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.CalendarioConexionRequest;
import com.example.proyecto.global.dto.ConexionCalendarioResponse;
import com.example.proyecto.global.dto.EventoCalendarioResponse;
import com.example.proyecto.global.dto.SincronizacionCalendarioResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.calendario.CalendarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/calendario")
@Tag(name = "Calendario", description = "Sincronizacion de fechas limite de parciales con Google Calendar (S3-03)")
@SecurityRequirement(name = "bearerAuth")
public class CalendarioController {

	private final CalendarioService calendarioService;

	public CalendarioController(CalendarioService calendarioService) {
		this.calendarioService = calendarioService;
	}

	@Operation(summary = "Estado de la conexion con Google Calendar")
	@GetMapping("/conexion")
	@PreAuthorize("hasAnyRole('DOCENTE','COORDINADOR','JEFE_DE_CARRERA','ADMIN')")
	public ResponseEntity<ConexionCalendarioResponse> estadoConexion(
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		UUID usuarioId = principal.getId();
		return ResponseEntity.ok(calendarioService.estadoConexion(usuarioId));
	}

	@Operation(summary = "Vincular calendario con codigo de autorizacion de Google")
	@PostMapping("/conexion")
	@PreAuthorize("hasAnyRole('DOCENTE','COORDINADOR','JEFE_DE_CARRERA','ADMIN')")
	public ResponseEntity<ConexionCalendarioResponse> conectar(
			@AuthenticationPrincipal UsuarioPrincipal principal,
			@Valid @RequestBody CalendarioConexionRequest request) {
		UUID usuarioId = principal.getId();
		return ResponseEntity.ok(calendarioService.conectar(usuarioId, request.codigo(), request.redirectUri()));
	}

	@Operation(summary = "Desvincular calendario y eliminar eventos creados")
	@DeleteMapping("/conexion")
	@PreAuthorize("hasAnyRole('DOCENTE','COORDINADOR','JEFE_DE_CARRERA','ADMIN')")
	public ResponseEntity<Void> desconectar(@AuthenticationPrincipal UsuarioPrincipal principal) {
		UUID usuarioId = principal.getId();
		calendarioService.desconectar(usuarioId);
		return ResponseEntity.noContent().build();
	}

	@Operation(summary = "Listar eventos sincronizados con el calendario")
	@GetMapping("/eventos")
	@PreAuthorize("hasAnyRole('DOCENTE','COORDINADOR','JEFE_DE_CARRERA','ADMIN')")
	public ResponseEntity<List<EventoCalendarioResponse>> listarEventos(
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		UUID usuarioId = principal.getId();
		return ResponseEntity.ok(calendarioService.listarEventos(usuarioId));
	}

	@Operation(summary = "Sincronizar fechas limite de parciales con Google Calendar")
	@PostMapping("/sincronizar")
	@PreAuthorize("hasAnyRole('DOCENTE','COORDINADOR','JEFE_DE_CARRERA','ADMIN')")
	public ResponseEntity<SincronizacionCalendarioResponse> sincronizar(
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		UUID usuarioId = principal.getId();
		return ResponseEntity.ok(calendarioService.sincronizar(usuarioId));
	}
}
