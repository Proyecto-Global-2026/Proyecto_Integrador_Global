package com.example.proyecto.global.dto;

import java.util.UUID;

import com.example.proyecto.global.domain.Usuario;

public record UsuarioResponse(

		UUID id,

		String nombre,

		String email,

		String rol,

		boolean activo,

		String proveedor) {

	public static UsuarioResponse from(Usuario usuario) {
		return new UsuarioResponse(
				usuario.getId(),
				usuario.getNombre(),
				usuario.getEmail(),
				usuario.getRol().getNombre(),
				usuario.isActivo(),
				usuario.getProveedor());
	}
}