package com.example.proyecto.global.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.AuthResponse;
import com.example.proyecto.global.dto.LoginRequest;
import com.example.proyecto.global.dto.RegistroRequest;
import com.example.proyecto.global.dto.UsuarioResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.AuthService;
import com.example.proyecto.global.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "Autenticacion")
public class AuthController {

	private final AuthService authService;
	private final UsuarioService usuarioService;

	public AuthController(AuthService authService, UsuarioService usuarioService) {
		this.authService = authService;
		this.usuarioService = usuarioService;
	}

	@PostMapping("/registro")
	@Operation(summary = "Registra un nuevo usuario con rol DOCENTE")
	public ResponseEntity<UsuarioResponse> registrar(@Valid @RequestBody RegistroRequest request) {
		return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.registrar(request));
	}

	@PostMapping("/login")
	@Operation(summary = "Autentica un usuario y devuelve un JWT")
	public AuthResponse login(@Valid @RequestBody LoginRequest request) {
		return authService.login(request);
	}

	@GetMapping("/me")
	@Operation(summary = "Devuelve el usuario autenticado")
	public UsuarioResponse me(@AuthenticationPrincipal UsuarioPrincipal principal) {
		return authService.perfil(principal);
	}

	@GetMapping("/usuarios")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista usuarios. SoloCOORDINADOR y DIRECCION")
	public java.util.List<UsuarioResponse> listar() {
		return authService.listarUsuarios();
	}
}