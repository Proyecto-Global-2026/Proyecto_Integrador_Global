package com.example.proyecto.global.controller;

import java.net.URI;
import java.util.UUID;

import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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

import com.example.proyecto.global.dto.MateriaCreateRequest;
import com.example.proyecto.global.dto.MateriaResponse;
import com.example.proyecto.global.dto.MateriaUpdateRequest;
import com.example.proyecto.global.dto.PaginadoResponse;
import com.example.proyecto.global.service.MateriaService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/materias")
@Tag(name = "Materias")
public class MateriaController {

	private final MateriaService materiaService;

	public MateriaController(MateriaService materiaService) {
		this.materiaService = materiaService;
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista materias. Filtra por texto y por estado activo")
	public PaginadoResponse<MateriaResponse> listar(
			@RequestParam(required = false) String q,
			@RequestParam(required = false) Boolean activo,
			@PageableDefault(size = 20, sort = "nombre") Pageable pageable) {
		return PaginadoResponse.de(materiaService.listar(q, activo, pageable).map(MateriaResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Consulta una materia por id")
	public MateriaResponse buscar(@PathVariable UUID id) {
		return MateriaResponse.from(materiaService.buscarPorId(id));
	}

	@PostMapping
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Crea una materia")
	public ResponseEntity<MateriaResponse> crear(@Valid @RequestBody MateriaCreateRequest request) {
		MateriaResponse creado = MateriaResponse.from(materiaService.crear(request));
		return ResponseEntity.created(URI.create("/api/materias/" + creado.id())).body(creado);
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Actualiza una materia")
	public MateriaResponse actualizar(@PathVariable UUID id, @Valid @RequestBody MateriaUpdateRequest request) {
		return MateriaResponse.from(materiaService.actualizar(id, request));
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Da de baja logica una materia")
	public ResponseEntity<Void> eliminar(@PathVariable UUID id) {
		materiaService.desactivar(id);
		return ResponseEntity.noContent().build();
	}

	@PostMapping("/{id}/reactivar")
	@PreAuthorize("hasAnyRole('COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Reactiva una materia")
	public MateriaResponse reactivar(@PathVariable UUID id) {
		return MateriaResponse.from(materiaService.reactivar(id));
	}
}