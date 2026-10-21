package com.example.proyecto.global.controller;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.proyecto.global.dto.EvidenciaRequest;
import com.example.proyecto.global.dto.EvidenciaResponse;
import com.example.proyecto.global.dto.PaginadoResponse;
import com.example.proyecto.global.domain.EstadoEvidencia;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.EvidenciaService;
import com.example.proyecto.global.config.StorageProperties;
import com.example.proyecto.global.storage.StorageService;
import java.time.Duration;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/evidencias")
@Tag(name = "Evidencias de capacitacion")
public class EvidenciaController {

	private final EvidenciaService evidenciaService;
	private final StorageService storageService;
	private final StorageProperties properties;
	private final com.example.proyecto.global.repository.EvidenciaRepository evidenciaRepository;

	public EvidenciaController(EvidenciaService evidenciaService, StorageService storageService,
			StorageProperties properties, com.example.proyecto.global.repository.EvidenciaRepository evidenciaRepository) {
		this.evidenciaService = evidenciaService;
		this.storageService = storageService;
		this.properties = properties;
		this.evidenciaRepository = evidenciaRepository;
	}

	@PostMapping
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Registra una evidencia de capacitacion (multipart, archivo adjunto opcional)")
	public ResponseEntity<EvidenciaResponse> crear(@Valid @ModelAttribute EvidenciaRequest request,
			@RequestParam(value = "archivo", required = false) MultipartFile archivo,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		EvidenciaResponse creada = evidenciaService.crear(request, archivo, principal.getId());
		return ResponseEntity.created(URI.create("/api/evidencias/" + creada.id())).body(creada);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista evidencias. El docente solo ve las suyas; coordinacion y direccion pueden filtrar por docente")
	public PaginadoResponse<EvidenciaResponse> listar(
			@RequestParam(required = false) UUID usuarioId,
			@RequestParam(required = false) EstadoEvidencia estado,
			@RequestParam(required = false) String q,
			@AuthenticationPrincipal UsuarioPrincipal principal,
			@PageableDefault(size = 20, sort = "fecha") Pageable pageable) {
		return PaginadoResponse.de(evidenciaService
				.listar(usuarioId, estado, q, principal.getRol(), principal.getId(), pageable)
				.map(EvidenciaResponse::from));
	}

	@GetMapping("/{id}")
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Consulta una evidencia. El docente propietario o los roles de revision")
	public EvidenciaResponse buscar(@PathVariable UUID id,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return EvidenciaResponse.from(evidenciaService.buscarPorId(id, principal.getRol(), principal.getId()));
	}

	@PutMapping("/{id}")
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Actualiza una evidencia propia. Si se envia archivo, reemplaza al anterior")
	public EvidenciaResponse actualizar(@PathVariable UUID id,
			@Valid @ModelAttribute EvidenciaRequest request,
			@RequestParam(value = "archivo", required = false) MultipartFile archivo,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return evidenciaService.actualizar(id, request, archivo, principal.getId());
	}

	@GetMapping("/{id}/archivo")
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Descarga el archivo adjunto de una evidencia")
	public ResponseEntity<?> descargarArchivo(@PathVariable UUID id,
			@AuthenticationPrincipal UsuarioPrincipal principal) throws IOException {
		com.example.proyecto.global.domain.Evidencia evidencia = evidenciaRepository.buscarPorIdConUsuario(id)
				.orElseThrow(() -> new com.example.proyecto.global.exception.ResourceNotFoundException(
						"Evidencia no encontrada: " + id));
		boolean esPropietario = evidencia.getUsuario().getId().equals(principal.getId());
		boolean esRevision = "COORDINADOR".equals(principal.getRol()) || "DIRECCION".equals(principal.getRol());
		if (!esPropietario && !esRevision) {
			throw new org.springframework.security.authorization.AuthorizationDeniedException(
					"No tienes permisos para consultar esta evidencia");
		}
		if (!evidencia.tieneArchivo()) {
			throw new com.example.proyecto.global.exception.ResourceNotFoundException(
					"La evidencia no tiene archivo adjunto: " + id);
		}
		if (evidencia.getPublicId() != null && !"local".equalsIgnoreCase(evidencia.getProveedorAlmacenamiento())) {
			String url = storageService.generarUrlDescargaSegura(evidencia.getPublicId(),
					evidencia.getResourceType(), evidencia.getTipoEntrega(), evidencia.getNombreOriginal(),
					Duration.ofMinutes(properties.cloudinary().duracionUrlMinutos()));
			if (url != null) {
				return ResponseEntity.status(302).header(HttpHeaders.LOCATION, url).build();
			}
		}
		EvidenciaService.Descarga descarga = evidenciaService.descargarArchivo(id, principal.getRol(),
				principal.getId());
		InputStreamResource recurso = new InputStreamResource(descarga.contenido());
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(descarga.contentType()))
				.contentLength(descarga.tamano())
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(descarga.nombreOriginal()).build().toString())
				.body(recurso);
	}

	@DeleteMapping("/{id}")
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Elimina una evidencia propia junto con su archivo adjunto")
	public ResponseEntity<Void> eliminar(@PathVariable UUID id,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		evidenciaService.eliminar(id, principal.getId());
		return ResponseEntity.noContent().build();
	}
}
