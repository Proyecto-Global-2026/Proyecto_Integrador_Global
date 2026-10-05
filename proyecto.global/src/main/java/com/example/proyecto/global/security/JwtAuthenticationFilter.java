package com.example.proyecto.global.security;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(JwtAuthenticationFilter.class);

	private static final String HEADER = "Authorization";
	private static final String PREFIJO = "Bearer ";

	private final JwtService jwtService;
	private final UserDetailsService userDetailsService;

	public JwtAuthenticationFilter(JwtService jwtService, UserDetailsService userDetailsService) {
		this.jwtService = jwtService;
		this.userDetailsService = userDetailsService;
	}

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws ServletException, IOException {

		String token = resolverToken(request);
		if (token != null && SecurityContextHolder.getContext().getAuthentication() == null) {
			autenticar(token, request);
		}
		chain.doFilter(request, response);
	}

	private void autenticar(String token, HttpServletRequest request) {
		try {
			String email = jwtService.extractUsername(token);
			if (email != null && jwtService.isTokenValido(token, email)) {
				UsuarioPrincipal principal = (UsuarioPrincipal) userDetailsService.loadUserByUsername(email);
				if (principal.isEnabled()) {
					var authentication = new UsernamePasswordAuthenticationToken(principal, null,
							principal.getAuthorities());
					authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
					SecurityContextHolder.getContext().setAuthentication(authentication);
				}
			}
		} catch (UsernameNotFoundException ex) {
			log.debug("Token de un usuario inexistente: {}", ex.getMessage());
		} catch (RuntimeException ex) {
			log.debug("Token invalido rechazado: {}", ex.getMessage());
		}
	}

	private String resolverToken(HttpServletRequest request) {
		String header = request.getHeader(HEADER);
		if (header != null && header.startsWith(PREFIJO)) {
			String token = header.substring(PREFIJO.length()).trim();
			return token.isEmpty() ? null : token;
		}
		return null;
	}
}