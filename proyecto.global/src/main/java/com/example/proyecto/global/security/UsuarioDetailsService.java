package com.example.proyecto.global.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.repository.UsuarioRepository;

@Service
public class UsuarioDetailsService implements UserDetailsService {

	private static final Logger log = LoggerFactory.getLogger(UsuarioDetailsService.class);

	private final UsuarioRepository usuarioRepository;

	public UsuarioDetailsService(UsuarioRepository usuarioRepository) {
		this.usuarioRepository = usuarioRepository;
	}

	@Override
	@Transactional(readOnly = true)
	public UsuarioPrincipal loadUserByUsername(String email) {
		Usuario usuario = usuarioRepository.findByEmail(Usuario.normalizarEmail(email))
				.orElseThrow(() -> {
					log.debug("Intento de autenticacion con correo desconocido");
					return new UsernameNotFoundException("Credenciales invalidas");
				});
		return new UsuarioPrincipal(usuario);
	}
}