package com.example.proyecto.global.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.repository.UsuarioRepository;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * Traduce el flujo OAuth2 de Spring Security al contrato JWT que consume el
 * frontend: al cerrar el ciclo con el proveedor emite el token y redirige al
 * SPA; si algo falla, regresa al login con un codigo de error legible.
 */
@Component
public class OAuth2JwtHandler implements AuthenticationSuccessHandler, AuthenticationFailureHandler {

	private static final Logger log = LoggerFactory.getLogger(OAuth2JwtHandler.class);

	private static final String ATRIBUTO_CORREO = "email";

	private final JwtService jwtService;
	private final UsuarioRepository usuarioRepository;
	private final String frontendUrl;

	public OAuth2JwtHandler(JwtService jwtService, UsuarioRepository usuarioRepository,
			@Value("${app.frontend.url:http://localhost:5173}") String frontendUrl) {
		this.jwtService = jwtService;
		this.usuarioRepository = usuarioRepository;
		this.frontendUrl = frontendUrl;
	}

	@Override
	public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
			Authentication authentication) throws IOException {
		try {
			String email = correoDel(authentication);
			Usuario usuario = usuarioRepository.findByEmail(email)
					.orElseThrow(() -> new IllegalStateException("Usuario OAuth2 no encontrado: " + email));

			String token = jwtService.generateToken(usuario);
			log.info("Login OAuth2 exitoso, JWT emitido para {}", email);

			response.sendRedirect(UriComponentsBuilder.fromUriString(frontendUrl + "/oauth2/callback")
					.queryParam("token", token)
					.build()
					.encode()
					.toUriString());
		} catch (RuntimeException ex) {
			log.error("No se pudo emitir el JWT tras el login OAuth2", ex);
			response.sendRedirect(frontendUrl + "/login?error=oauth2");
		}
	}

	@Override
	public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
			AuthenticationException ex) throws IOException {
		log.warn("Fallo de login OAuth2: {}", ex.getMessage());
		response.sendRedirect(frontendUrl + "/login?error=oauth2");
	}

	private String correoDel(Authentication authentication) {
		if (!(authentication.getPrincipal() instanceof OAuth2User principal)) {
			throw new IllegalStateException("El principal no es un OAuth2User");
		}
		Object correo = principal.getAttribute(ATRIBUTO_CORREO);
		if (correo == null || correo.toString().isBlank()) {
			throw new IllegalStateException("El proveedor no devolvio el correo");
		}
		return Usuario.normalizarEmail(correo.toString());
	}
}
