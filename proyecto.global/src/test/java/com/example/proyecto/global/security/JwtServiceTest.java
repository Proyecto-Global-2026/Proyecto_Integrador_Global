package com.example.proyecto.global.security;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;

class JwtServiceTest {

	private static final String SECRETO = "clave-de-pruebas-que-solo-sirve-para-los-tests-unitarios-1234567890";

	private JwtService jwtService;

	@BeforeEach
	void setUp() {
		jwtService = new JwtService(SECRETO, 30);
	}

	@Test
	void elTokenLlevaCorreoNombreYRol() {
		Usuario usuario = usuario("coordinador@escuela.edu", "COORDINADOR");

		String token = jwtService.generateToken(usuario);

		assertThat(jwtService.extractUsername(token)).isEqualTo("coordinador@escuela.edu");
		assertThat(jwtService.extractRol(token)).isEqualTo("COORDINADOR");
	}

	@Test
	void unTokenValidoPasalaValidacion() {
		String token = jwtService.generateToken(usuario("docente@escuela.edu", "DOCENTE"));

		assertThat(jwtService.isTokenValido(token, "docente@escuela.edu")).isTrue();
	}

	@Test
	void tokenDeOtroUsuarioEsRechazado() {
		String token = jwtService.generateToken(usuario("docente@escuela.edu", "DOCENTE"));

		assertThat(jwtService.isTokenValido(token, "otro@escuela.edu")).isFalse();
	}

	@Test
	void tokenConFirmaDistintaEsRechazado() {
		String token = new JwtService("otra-clave-distinta-que-also-es-larga-123456789012", 30)
				.generateToken(usuario("docente@escuela.edu", "DOCENTE"));

		assertThat(jwtService.isTokenValido(token, "docente@escuela.edu")).isFalse();
	}

	@Test
	void tokenBasuraNoRompeElServicio() {
		assertThat(jwtService.isTokenValido("no-es-un-jwt", "docente@escuela.edu")).isFalse();
	}

	@Test
	void elVencimientoSeExponeEnSegundos() {
		assertThat(jwtService.getExpirationSeconds()).isEqualTo(1800);
	}

	private Usuario usuario(String email, String rol) {
		return new Usuario("Nombre", email, "hash", new Rol(rol, "descripcion"));
	}
}