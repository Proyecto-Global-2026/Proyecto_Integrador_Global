package com.example.proyecto.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Configuracion minima para que la API sea consumible por el frontend.
 * El bean de CORS se registra aca para que los preflight OPTIONS no sean
 * bloqueados por Spring Security.
 * Reemplazar por la configuracion JWT en la historia de usuario de autenticacion.
 */
@Configuration
public class SecurityConfig {

	@Bean
	SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
		return http
				.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/api/**", "/actuator/health", "/v3/api-docs/**", "/swagger-ui/**",
								"/swagger-ui.html")
						.permitAll())
				.httpBasic(AbstractHttpConfigurer::disable)
				.formLogin(AbstractHttpConfigurer::disable)
				.logout(AbstractHttpConfigurer::disable)
				.build();
	}
}