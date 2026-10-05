package com.example.proyecto.global.config;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

class CorsConfigTest {

	private final CorsProperties properties = new CorsProperties(
			List.of("http://localhost:5173", "https://app.proyectoglobal.com"),
			List.of("GET", "POST", "OPTIONS"),
			List.of("*"),
			List.of("Location"),
			true,
			1800L);

	@Test
	void origenesPermitidosVienenDeConfiguracion() {
		CorsConfiguration configuration = source().getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/x"));

		assertThat(configuration).isNotNull();
		assertThat(configuration.getAllowedOrigins())
				.containsExactly("http://localhost:5173", "https://app.proyectoglobal.com");
		assertThat(configuration.getAllowedMethods()).contains("GET", "POST", "OPTIONS");
		assertThat(configuration.getAllowCredentials()).isTrue();
		assertThat(configuration.getMaxAge()).isEqualTo(1800L);
	}

	@Test
	void noSePermiteComodinEnOrigenes() {
		CorsConfiguration configuration = source().getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/x"));

		assertThat(configuration).isNotNull();
		assertThat(configuration.getAllowedOrigins()).doesNotContain("*");
	}

	@Test
	void origenesPorDefectoApuntanAFrontendLocal() {
		CorsProperties defaults = new CorsProperties(
				List.of("http://localhost:5173", "http://localhost:3000"),
				List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"),
				List.of("*"),
				List.of("Location", "Content-Disposition"),
				true,
				3600L);

		CorsConfiguration configuration = new CorsConfig(defaults).corsConfigurationSource()
				.getCorsConfiguration(new MockHttpServletRequest("OPTIONS", "/api/planeaciones"));

		assertThat(configuration).isNotNull();
		assertThat(configuration.getAllowedOrigins())
				.containsExactly("http://localhost:5173", "http://localhost:3000");
		assertThat(configuration.getExposedHeaders()).containsExactly("Location", "Content-Disposition");
		assertThat(configuration.getMaxAge()).isEqualTo(3600L);
	}

	@Test
	void filtroCorsSeConstruyeSinErrores() {
		assertThat(new CorsFilter(source())).isNotNull();
	}

	private CorsConfigurationSource source() {
		return new CorsConfig(properties).corsConfigurationSource();
	}
}