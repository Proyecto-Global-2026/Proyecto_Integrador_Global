package com.example.proyecto.global.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableConfigurationProperties(CorsProperties.class)
public class CorsConfig implements WebMvcConfigurer {

	private static final Logger log = LoggerFactory.getLogger(CorsConfig.class);

	private final CorsProperties properties;

	public CorsConfig(CorsProperties properties) {
		this.properties = properties;
	}

	@Override
	public void addCorsMappings(CorsRegistry registry) {
		log.info("CORS habilitado para origenes: {}", properties.allowedOrigins());
		registry.addMapping("/api/**")
				.allowedOrigins(properties.allowedOrigins().toArray(String[]::new))
				.allowedMethods(properties.allowedMethods().toArray(String[]::new))
				.allowedHeaders(properties.allowedHeaders().toArray(String[]::new))
				.exposedHeaders(properties.exposedHeaders().toArray(String[]::new))
				.allowCredentials(properties.allowCredentials())
				.maxAge(properties.maxAge());
	}

	/**
	 * Fuente de configuracion CORS para la cadena de filtros de Spring Security,
	 * de modo que los preflight OPTIONS se respondan antes de la autorizacion.
	 */
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(properties.allowedOrigins());
		configuration.setAllowedMethods(properties.allowedMethods());
		configuration.setAllowedHeaders(properties.allowedHeaders());
		configuration.setExposedHeaders(properties.exposedHeaders());
		configuration.setAllowCredentials(properties.allowCredentials());
		configuration.setMaxAge(properties.maxAge());

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/api/**", configuration);
		return source;
	}
}