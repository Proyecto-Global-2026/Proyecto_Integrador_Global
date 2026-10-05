package com.example.proyecto.global.security;

import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;

public class UsuarioPrincipal implements UserDetails {

	private static final long serialVersionUID = 1L;

	public static final String PREFIJO_AUTORIDAD = "ROLE_";

	private final UUID id;
	private final String nombre;
	private final String email;
	private final String password;
	private final String rol;
	private final boolean activo;
	private final Collection<? extends GrantedAuthority> authorities;

	public UsuarioPrincipal(Usuario usuario) {
		this.id = usuario.getId();
		this.nombre = usuario.getNombre();
		this.email = usuario.getEmail();
		this.password = usuario.getPassword();
		this.rol = usuario.getRol().getNombre();
		this.activo = usuario.isActivo();
		this.authorities = List.of(new SimpleGrantedAuthority(PREFIJO_AUTORIDAD + rol));
	}

	public static Collection<? extends GrantedAuthority> autoridadesDe(Rol rol) {
		return List.of(new SimpleGrantedAuthority(PREFIJO_AUTORIDAD + rol.getNombre()));
	}

	public UUID getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getRol() {
		return rol;
	}

	public boolean isActivo() {
		return activo;
	}

	@Override
	public Collection<? extends GrantedAuthority> getAuthorities() {
		return authorities;
	}

	@Override
	public String getPassword() {
		return password;
	}

	@Override
	public String getUsername() {
		return email;
	}

	@Override
	public boolean isAccountNonExpired() {
		return true;
	}

	@Override
	public boolean isAccountNonLocked() {
		return activo;
	}

	@Override
	public boolean isCredentialsNonExpired() {
		return true;
	}

	@Override
	public boolean isEnabled() {
		return activo;
	}
}