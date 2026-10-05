package com.example.proyecto.global;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.test.context.ActiveProfiles;

import com.example.proyecto.global.config.CorsProperties;

@SpringBootTest
@ActiveProfiles("test")
class ApplicationTests {

	@Autowired
	private CorsProperties corsProperties;

	@Autowired
	private SecurityFilterChain securityFilterChain;

	@Test
	void contextLoads() {
	}

	@Test
	void corsSeBindeaDesdeElPerfilDeTest() {
		assertThat(corsProperties.allowedOrigins()).containsExactly("http://localhost:5173");
		assertThat(corsProperties.allowCredentials()).isTrue();
	}

	@Test
	void laCadenaDeFiltrosDeSeguridadQuedaRegistrada() {
		assertThat(securityFilterChain).isNotNull();
	}
}