package com.example.proyecto.global.controller;

import java.util.List;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.proyecto.global.dto.MatrizRbacResponse;
import com.example.proyecto.global.dto.MatrizRbacResponse.ReglaRbac;
import com.example.proyecto.global.security.PoliticaAccesoDatos;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/seguridad")
@Tag(name = "Seguridad", description = "Control de acceso estricto (RBAC) a datos personales (S3-09)")
@SecurityRequirement(name = "bearerAuth")
public class SeguridadController {

	private final PoliticaAccesoDatos politica;

	public SeguridadController(PoliticaAccesoDatos politica) {
		this.politica = politica;
	}

	@Operation(summary = "Consulta la matriz de acceso a datos personales")
	@GetMapping("/rbac")
	@PreAuthorize("hasRole('ADMIN')")
	public MatrizRbacResponse rbac() {
		List<ReglaRbac> reglas = politica.matriz().stream()
				.map(regla -> new ReglaRbac(regla.metodo(), regla.ruta(), regla.roles(), regla.descripcion()))
				.toList();
		return new MatrizRbacResponse(
				"Control de acceso estricto a datos personales: el propietario accede a sus datos; "
						+ "COORDINADOR y DIRECCION consultan para revision; solo el propietario modifica.",
				reglas);
	}
}
