package com.example.proyecto.global.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.config.Customizer;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.proyecto.global.security.JwtAuthenticationFilter;
import com.example.proyecto.global.security.SecurityErrorHandlers;
import com.example.proyecto.global.security.UsuarioDetailsService;

/**
 * Cadena de filtros stateless con JWT. Los preflight de CORS se resuelven aca
 * para que Spring Security no los bloquee antes de llegar al controlador.
 */
@Configuration
@EnableMethodSecurity
public class SecurityConfig {

	@Bean
	SecurityFilterChain apiFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter,
			SecurityErrorHandlers securityErrorHandlers,
			@Value("${app.oauth2.enabled:false}") boolean oauth2Enabled) throws Exception {

		http.cors(Customizer.withDefaults())
				.csrf(AbstractHttpConfigurer::disable)
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(securityErrorHandlers)
						.accessDeniedHandler(securityErrorHandlers))
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/api/auth/registro", "/api/auth/login").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health")
						.permitAll()
						.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

		if (oauth2Enabled) {
			http.oauth2Login(Customizer.withDefaults());
		} else {
			http.oauth2Login(AbstractHttpConfigurer::disable);
			http.oauth2Client(AbstractHttpConfigurer::disable);
		}

		return http.build();
	}

	@Bean
	PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	@Bean
	AuthenticationManager authenticationManager(UsuarioDetailsService usuarioDetailsService,
			PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(usuarioDetailsService);
		provider.setPasswordEncoder(passwordEncoder);
		return provider::authenticate;
	}
}