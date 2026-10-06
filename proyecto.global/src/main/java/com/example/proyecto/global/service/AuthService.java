package com.example.proyecto.global.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.AuthResponse;
import com.example.proyecto.global.dto.LoginRequest;
import com.example.proyecto.global.dto.UsuarioResponse;
import com.example.proyecto.global.exception.CredencialesInvalidasException;
import com.example.proyecto.global.repository.UsuarioRepository;
import com.example.proyecto.global.security.JwtService;
import com.example.proyecto.global.security.UsuarioPrincipal;

@Service
public class AuthService {

	private final AuthenticationManager authenticationManager;
	private final JwtService jwtService;
	private final UsuarioRepository usuarioRepository;

	public AuthService(AuthenticationManager authenticationManager, JwtService jwtService,
			UsuarioRepository usuarioRepository) {
		this.authenticationManager = authenticationManager;
		this.jwtService = jwtService;
		this.usuarioRepository = usuarioRepository;
	}

	@Transactional
	public AuthResponse login(LoginRequest request) {
		String email = Usuario.normalizarEmail(request.email());
		Authentication authentication;
		try {
			authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(email, request.password()));
		} catch (BadCredentialsException | DisabledException ex) {
			throw new CredencialesInvalidasException();
		}

		UsuarioPrincipal principal = (UsuarioPrincipal) authentication.getPrincipal();
		Usuario usuario = usuarioRepository.findByEmail(email)
				.orElseThrow(CredencialesInvalidasException::new);

		return new AuthResponse(
				jwtService.generateToken(usuario),
				"Bearer",
				jwtService.getExpirationSeconds(),
				UsuarioResponse.from(usuario));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse perfil(UsuarioPrincipal principal) {
		return usuarioRepository.findByEmail(principal.getUsername())
				.map(UsuarioResponse::from)
				.orElseThrow(CredencialesInvalidasException::new);
	}
}