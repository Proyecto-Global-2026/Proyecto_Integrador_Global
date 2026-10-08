package com.example.proyecto.global.controller;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.example.proyecto.global.dto.ArchivoResponse;
import com.example.proyecto.global.security.UsuarioPrincipal;
import com.example.proyecto.global.service.ArchivoService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/planeaciones/{planeacionId}/archivos")
@Tag(name = "Archivos de planeaciones")
public class ArchivoController {

	private final ArchivoService archivoService;

	public ArchivoController(ArchivoService archivoService) {
		this.archivoService = archivoService;
	}

	@PostMapping
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Adjunta un archivo (multipart) a una planeacion propia")
	public ResponseEntity<ArchivoResponse> subir(@PathVariable UUID planeacionId,
			@RequestParam("archivo") MultipartFile archivo,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		ArchivoResponse creado = archivoService.subir(planeacionId, archivo, principal.getId());
		return ResponseEntity
				.created(URI.create("/api/planeaciones/" + planeacionId + "/archivos/" + creado.id()))
				.body(creado);
	}

	@GetMapping
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Lista los archivos de una planeacion. El docente propietario o los roles de revision")
	public List<ArchivoResponse> listar(@PathVariable UUID planeacionId,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		return archivoService.listarPorPlaneacion(planeacionId, principal.getRol(), principal.getId());
	}

	@GetMapping("/{archivoId}")
	@PreAuthorize("hasAnyRole('DOCENTE', 'COORDINADOR', 'DIRECCION')")
	@Operation(summary = "Descarga un archivo adjunto")
	public ResponseEntity<InputStreamResource> descargar(@PathVariable UUID planeacionId,
			@PathVariable UUID archivoId,
			@AuthenticationPrincipal UsuarioPrincipal principal) throws IOException {
		ArchivoService.Descarga descarga = archivoService.descargar(archivoId, principal.getRol(),
				principal.getId());
		InputStreamResource recurso = new InputStreamResource(descarga.contenido());
		return ResponseEntity.ok()
				.contentType(MediaType.parseMediaType(descarga.contentType()))
				.contentLength(descarga.tamano())
				.header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
						.filename(descarga.nombreOriginal()).build().toString())
				.body(recurso);
	}

	@DeleteMapping("/{archivoId}")
	@PreAuthorize("hasRole('DOCENTE')")
	@Operation(summary = "Elimina un archivo adjunto propio")
	public ResponseEntity<Void> eliminar(@PathVariable UUID planeacionId,
			@PathVariable UUID archivoId,
			@AuthenticationPrincipal UsuarioPrincipal principal) {
		archivoService.eliminar(archivoId, principal.getId());
		return ResponseEntity.noContent().build();
	}
}