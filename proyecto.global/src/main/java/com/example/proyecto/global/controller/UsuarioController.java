package com.example.proyecto.global.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.PaginadoResponse;
import com.example.proyecto.global.dto.RolResponse;
import com.example.proyecto.global.dto.UsuarioCreateRequest;
import com.example.proyecto.global.dto.UsuarioResponse;
import com.example.proyecto.global.dto.UsuarioUpdateRequest;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.UsuarioService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

/**
 * Gestion de usuarios. DIRECCION actua como administrador del sistema;
 * COORDINADOR y DIRECCION pueden consultar.
 */
@RestController
@RequestMapping("/api")
@Tag(name = "Usuarios y roles")
public class UsuarioController {

	private final UsuarioService usuarioService;

	public UsuarioController(UsuarioService usuarioService) {
		this.usuarioService = usuarioService;
	}

	@GetMapping("/usuarios")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista usuarios. Filtra por rol y por estado activo")
	public PaginadoResponse<UsuarioResponse> listar(
			@RequestParam(required = false) String rol,
			@RequestParam(required = false) Boolean activo,
			@PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
		return PaginadoResponse.de(usuarioService.listar(rol, activo, pageable));
	}

	@GetMapping("/usuarios/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Consulta un usuario por id")
	public UsuarioResponse buscar(@PathVariable UUID id) {
		return usuarioService.buscarPorId(id);
	}

	@PostMapping("/usuarios")
	@PreAuthorize("hasRole('DIRECCION')")
	@Operation(summary = "Crea un usuario con el rol indicado")
	public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest request) {
		UsuarioResponse creado = usuarioService.crear(request);
		return ResponseEntity.created(URI.create("/api/usuarios/" + creado.id())).body(creado);
	}

	@PutMapping("/usuarios/{id}")
	@PreAuthorize("hasRole('DIRECCION')")
	@Operation(summary = "Actualiza nombre, correo y rol de un usuario")
	public UsuarioResponse actualizar(@PathVariable UUID id, @Valid @RequestBody UsuarioUpdateRequest request) {
		return usuarioService.actualizar(id, request);
	}

	@DeleteMapping("/usuarios/{id}")
	@PreAuthorize("hasRole('DIRECCION')")
	@Operation(summary = "Da de baja logica a un usuario")
	public ResponseEntity<Void> eliminar(@PathVariable UUID id,
			@AuthenticationPrincipal UsuarioPrincipal solicitante) {
		usuarioService.desactivar(id, solicitante.getId());
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/usuarios/{id}/reactivar")
	@PreAuthorize("hasRole('DIRECCION')")
	@Operation(summary = "Reactiva un usuario dado de baja")
	public UsuarioResponse reactivar(@PathVariable UUID id) {
		return usuarioService.reactivar(id);
	}

	@GetMapping("/roles")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista los roles disponibles")
	public List<RolResponse> listarRoles() {
		return usuarioService.listarRoles().stream().map(RolResponse::from).toList();
	}
}