package com.example.proyecto.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.config.RetencionProperties;
import com.example.proyecto.global.dto.PoliticaRetencionResponse;
import com.example.proyecto.global.service.retencion.RetencionDatosService;
import com.example.proyecto.global.service.retencion.RetencionDatosService.ResultadoRetencion;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/retencion")
@Tag(name = "Retencion de datos", description = "Minimizacion, anonimizacion y borrado de datos personales (S3-07)")
@SecurityRequirement(name = "bearerAuth")
public class RetencionDatosController {

	private final RetencionDatosService retencionDatosService;
	private final RetencionProperties properties;

	public RetencionDatosController(RetencionDatosService retencionDatosService, RetencionProperties properties) {
		this.retencionDatosService = retencionDatosService;
		this.properties = properties;
	}

	@Operation(summary = "Consultar la politica de retencion de datos personales")
	@GetMapping("/politica")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<PoliticaRetencionResponse> politica() {
		return ResponseEntity.ok(PoliticaRetencionResponse.from(properties));
	}

	@Operation(summary = "Ejecutar manualmente el proceso de retencion")
	@PostMapping("/ejecutar")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<ResultadoRetencion> ejecutar() {
		return ResponseEntity.ok(retencionDatosService.ejecutar());
	}
}
