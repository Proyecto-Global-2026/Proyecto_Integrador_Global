package com.example.proyecto.global.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.config.CifradoProperties;
import com.example.proyecto.global.dto.EstadoCifradoResponse;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/cifrado")
@Tag(name = "Cifrado de datos", description = "Estado del cifrado de datos en reposo y configuración (S3-08)")
@SecurityRequirement(name = "bearerAuth")
public class CifradoDatosController {

	private final CifradoProperties properties;

	public CifradoDatosController(CifradoProperties properties) {
		this.properties = properties;
	}

	@Operation(summary = "Consultar estado de cifrado en reposo")
	@GetMapping("/estado")
	@PreAuthorize("hasRole('ADMIN')")
	public ResponseEntity<EstadoCifradoResponse> estado() {
		return ResponseEntity.ok(EstadoCifradoResponse.from(properties));
	}
}
