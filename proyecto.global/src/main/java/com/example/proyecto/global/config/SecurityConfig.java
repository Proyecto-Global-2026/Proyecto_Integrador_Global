package com.example.proyecto.global.config;

import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_ADMIN;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_COORDINADOR;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_DOCENTE;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_DIRECCION;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_JEFE_DE_CARRERA;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

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
				.headers(headers -> headers
						.httpStrictTransportSecurity(hsts -> hsts
								.includeSubDomains(true)
								.maxAgeInSeconds(31536000)
								.preload(true))
						.frameOptions(frame -> frame.deny())
						.referrerPolicy(referrer -> referrer
								.policy(ReferrerPolicyHeaderWriter.ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN)))
				.exceptionHandling(ex -> ex.authenticationEntryPoint(securityErrorHandlers)
						.accessDeniedHandler(securityErrorHandlers))
				.authorizeHttpRequests(requests -> requests
						.requestMatchers("/api/auth/registro", "/api/auth/login").permitAll()
						.requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html", "/actuator/health")
						.permitAll()
						// S3-09: datos personales de usuarios. La regla de URL es la primera
						// barrera; los @PreAuthorize de los controladores la refuerzan.
						.requestMatchers(HttpMethod.GET, "/api/usuarios", "/api/usuarios/**", "/api/roles")
						.hasAnyRole(ROL_COORDINADOR, ROL_DIRECCION)
						.requestMatchers(HttpMethod.POST, "/api/usuarios", "/api/usuarios/*/reactivar")
						.hasRole(ROL_DIRECCION)
						.requestMatchers(HttpMethod.PUT, "/api/usuarios/**").hasRole(ROL_DIRECCION)
						.requestMatchers(HttpMethod.DELETE, "/api/usuarios/**").hasRole(ROL_DIRECCION)
						// Reportes institucionales con datos personales agregados.
						.requestMatchers("/api/reportes/**").hasAnyRole(ROL_COORDINADOR, ROL_DIRECCION)
						// Administracion de seguridad, cifrado y retencion.
						.requestMatchers("/api/retencion/**", "/api/cifrado/**", "/api/seguridad/**")
						.hasRole(ROL_ADMIN)
						// Calendario personal: cada usuario solo opera el suyo.
						.requestMatchers("/api/calendario/**")
						.hasAnyRole(ROL_DOCENTE, ROL_COORDINADOR, ROL_JEFE_DE_CARRERA, ROL_ADMIN)
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