package com.example.proyecto.global.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.RegistroRequest;
import com.example.proyecto.global.dto.UsuarioResponse;
import com.example.proyecto.global.exception.EmailDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.RolRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

@Service
public class UsuarioService {

	public static final String ROL_DOCENTE = "DOCENTE";

	private final UsuarioRepository usuarioRepository;
	private final RolRepository rolRepository;
	private final PasswordEncoder passwordEncoder;

	public UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository,
			PasswordEncoder passwordEncoder) {
		this.usuarioRepository = usuarioRepository;
		this.rolRepository = rolRepository;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public UsuarioResponse registrar(RegistroRequest request) {
		String email = Usuario.normalizarEmail(request.email());
		if (usuarioRepository.existsByEmail(email)) {
			throw new EmailDuplicadoException(email);
		}
		Rol rol = rolRepository.findByNombre(ROL_DOCENTE)
				.orElseThrow(() -> new ResourceNotFoundException(
						"El rol " + ROL_DOCENTE + " no esta inicializado en la base de datos"));

		Usuario usuario = new Usuario(
				request.nombre().trim(),
				email,
				passwordEncoder.encode(request.password()),
				rol);
		return UsuarioResponse.from(usuarioRepository.save(usuario));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorEmail(String email) {
		return usuarioRepository.findByEmail(Usuario.normalizarEmail(email))
				.map(UsuarioResponse::from)
				.orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con ese correo"));
	}
}