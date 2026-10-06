package com.example.proyecto.global.dto;

import com.example.proyecto.global.domain.Rol;

public record RolResponse(

		Short id,

		String nombre,

		String descripcion) {

	public static RolResponse from(Rol rol) {
		return new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion());
	}
}