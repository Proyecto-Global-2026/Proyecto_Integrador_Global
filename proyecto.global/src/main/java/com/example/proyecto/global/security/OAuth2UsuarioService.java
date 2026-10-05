package com.example.proyecto.global.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.repository.RolRepository;
import com.example.proyecto.global.repository.UsuarioRepository;
import com.example.proyecto.global.service.UsuarioService;

/**
 * Login alterno via proveedor externo (OAuth2). Crea o actualiza el usuario
 * local a partir de la identidad que entrega el proveedor, y devuelve las
 * autoridades con el rol asignado en el sistema.
 */
@Service
public class OAuth2UsuarioService extends DefaultOAuth2UserService {

	private static final Logger log = LoggerFactory.getLogger(OAuth2UsuarioService.class);

	private static final String ATRIBUTO_CORREO = "email";
	private static final String ATRIBUTO_ID = "sub";

	private final UsuarioRepository usuarioRepository;
	private final RolRepository rolRepository;

	public OAuth2UsuarioService(UsuarioRepository usuarioRepository, RolRepository rolRepository) {
		this.usuarioRepository = usuarioRepository;
		this.rolRepository = rolRepository;
	}

	@Override
	@Transactional
	public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
		OAuth2User oauthUser = super.loadUser(userRequest);
		String proveedor = userRequest.getClientRegistration().getRegistrationId();
		String correo = oauthUser.getAttribute(ATRIBUTO_CORREO);

		if (correo == null || correo.isBlank()) {
			log.warn("El proveedor {} no devolvio el correo", proveedor);
			throw new OAuth2AuthenticationException("El proveedor no devolvio el correo del usuario");
		}

		String email = Usuario.normalizarEmail(correo);
		String proveedorId = String.valueOf(oauthUser.getAttribute(ATRIBUTO_ID));

		Usuario usuario = usuarioRepository.findByProveedorAndProveedorId(proveedor, proveedorId)
				.or(() -> usuarioRepository.findByEmail(email))
				.orElseGet(() -> crearUsuario(oauthUser, proveedor, email, proveedorId));

		usuario.setProveedor(proveedor);
		usuario.setProveedorId(proveedorId);
		usuarioRepository.save(usuario);

		log.info("Login OAuth2 exitoso: proveedor={}, correo={}, rol={}", proveedor, email,
				usuario.getRol().getNombre());

		return new DefaultOAuth2User(UsuarioPrincipal.autoridadesDe(usuario.getRol()), oauthUser.getAttributes(),
				ATRIBUTO_CORREO);
	}

	private Usuario crearUsuario(OAuth2User oauthUser, String proveedor, String email, String proveedorId) {
		Rol rol = rolRepository.findByNombre(UsuarioService.ROL_DOCENTE)
				.orElseThrow(() -> new IllegalStateException("El rol DOCENTE no esta inicializado"));
		Object nombreProveedor = oauthUser.getAttribute("name");
		String nombre = nombreProveedor == null || nombreProveedor.toString().isBlank()
				? email
				: nombreProveedor.toString();

		// El usuarioiloogueado por proveedor externo no tiene contrasena local;
		// se guarda un valor aleatorio que nunca se podria validar.
		return new Usuario(nombre, email, "{sin-password}:" + java.util.UUID.randomUUID(), rol);
	}
}