package com.example.proyecto.global.service;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.RegistroRequest;
import com.example.proyecto.global.dto.UsuarioCreateRequest;
import com.example.proyecto.global.dto.UsuarioResponse;
import com.example.proyecto.global.dto.UsuarioUpdateRequest;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.EmailDuplicadoException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.RolRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

import jakarta.persistence.criteria.Predicate;

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
		rechazarSiElCorreoYaExiste(email, null);

		Usuario usuario = new Usuario(
				request.nombre().trim(),
				email,
				passwordEncoder.encode(request.password()),
				obtenerRol(ROL_DOCENTE));
		return UsuarioResponse.from(usuarioRepository.save(usuario));
	}

	@Transactional
	public UsuarioResponse crear(UsuarioCreateRequest request) {
		String email = Usuario.normalizarEmail(request.email());
		rechazarSiElCorreoYaExiste(email, null);

		Usuario usuario = new Usuario(
				request.nombre().trim(),
				email,
				passwordEncoder.encode(request.password()),
				obtenerRol(request.rol()));
		return UsuarioResponse.from(usuarioRepository.save(usuario));
	}

	@Transactional(readOnly = true)
	public UsuarioResponse buscarPorId(UUID id) {
		return UsuarioResponse.from(obtenerUsuario(id));
	}

	@Transactional
	public UsuarioResponse actualizar(UUID id, UsuarioUpdateRequest request) {
		Usuario usuario = obtenerUsuario(id);
		String email = Usuario.normalizarEmail(request.email());
		rechazarSiElCorreoYaExiste(email, id);

		usuario.actualizar(request.nombre().trim(), email, obtenerRol(request.rol()));
		return UsuarioResponse.from(usuarioRepository.save(usuario));
	}

	/**
	 * Baja logica: el usuario se marca inactivo en vez de borrarse, para no
	 * perder la trazabilidad de las planeaciones que registro o aprobo.
	 */
	@Transactional
	public void desactivar(UUID id, UUID solicitanteId) {
		Usuario usuario = obtenerUsuario(id);
		if (usuario.getId().equals(solicitanteId)) {
			throw new BusinessRuleException("No puedes desactivar tu propia cuenta");
		}
		usuario.setActivo(false);
		usuarioRepository.save(usuario);
	}

	@Transactional
	public UsuarioResponse reactivar(UUID id) {
		Usuario usuario = obtenerUsuario(id);
		usuario.setActivo(true);
		return UsuarioResponse.from(usuarioRepository.save(usuario));
	}

	@Transactional(readOnly = true)
	public Page<UsuarioResponse> listar(String rol, Boolean activo, Pageable pageable) {
		return usuarioRepository.findAll(filtros(rol, activo), pageable).map(UsuarioResponse::from);
	}

	@Transactional(readOnly = true)
	public List<Rol> listarRoles() {
		return rolRepository.findAllByOrderByNombreAsc();
	}

	private Specification<Usuario> filtros(String rol, Boolean activo) {
		return (root, query, builder) -> {
			Predicate condicion = builder.conjunction();
			if (rol != null && !rol.isBlank()) {
				String nombre = normalizarRol(rol);
				// Si el rol no existe, la busqueda devuelve vacio en vez de 500.
				Rol existente = rolRepository.findByNombre(nombre).orElse(null);
				if (existente == null) {
					return builder.disjunction();
				}
				condicion = builder.and(condicion, builder.equal(root.get("rol").get("nombre"), nombre));
			}
			if (activo != null) {
				condicion = builder.and(condicion, builder.equal(root.get("activo"), activo));
			}
			return condicion;
		};
	}

	private Usuario obtenerUsuario(UUID id) {
		return usuarioRepository.findById(id)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el usuario con id " + id));
	}

	private Rol obtenerRol(String nombre) {
		String normalizado = nombre == null ? "" : normalizarRol(nombre);
		return rolRepository.findByNombre(normalizado)
				.orElseThrow(() -> new ResourceNotFoundException("No existe el rol " + nombre));
	}

	private String normalizarRol(String nombre) {
		return nombre.trim().toUpperCase(Locale.ROOT);
	}

	private void rechazarSiElCorreoYaExiste(String email, UUID idExcluido) {
		usuarioRepository.findByEmail(email).ifPresent(existente -> {
			if (!existente.getId().equals(idExcluido)) {
				throw new EmailDuplicadoException(email);
			}
		});
	}
}