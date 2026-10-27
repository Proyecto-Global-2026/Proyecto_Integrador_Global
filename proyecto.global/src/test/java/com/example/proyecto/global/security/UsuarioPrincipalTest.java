package com.example.proyecto.global.security;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;

class UsuarioPrincipalTest {

	@Test
	void usuarioInactivoSigueSiendoNoBloqueado() {
		Usuario usuario = new Usuario("Docente", "docente@escuela.mx", "hash", new Rol("DOCENTE", "Docente"));
		usuario.setActivo(false);

		UsuarioPrincipal principal = new UsuarioPrincipal(usuario);

		assertFalse(principal.isEnabled());
		assertTrue(principal.isAccountNonLocked());
	}
}
