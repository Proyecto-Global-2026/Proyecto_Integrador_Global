package com.example.proyecto.global.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.LockedException;

import com.example.proyecto.global.dto.LoginRequest;
import com.example.proyecto.global.exception.CredencialesInvalidasException;
import com.example.proyecto.global.repository.UsuarioRepository;
import com.example.proyecto.global.security.JwtService;

class AuthServiceTest {

	private AuthenticationManager authenticationManager;
	private AuthService authService;

	@BeforeEach
	void setUp() {
		authenticationManager = mock(AuthenticationManager.class);
		authService = new AuthService(authenticationManager, mock(JwtService.class), mock(UsuarioRepository.class));
	}

	@Test
	void cuentaBloqueadaSeTraduceEnCredencialesInvalidas() {
		when(authenticationManager.authenticate(any())).thenThrow(new LockedException("bloqueada"));

		assertThrows(CredencialesInvalidasException.class,
				() -> authService.login(new LoginRequest("docente@escuela.mx", "Password123")));
	}

	@Test
	void cuentaDeshabilitadaSeTraduceEnCredencialesInvalidas() {
		when(authenticationManager.authenticate(any())).thenThrow(new DisabledException("deshabilitada"));

		assertThrows(CredencialesInvalidasException.class,
				() -> authService.login(new LoginRequest("docente@escuela.mx", "Password123")));
	}
}
