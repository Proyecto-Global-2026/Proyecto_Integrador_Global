package com.example.proyecto.global.dto;

public record AuthResponse(

		String token,

		String tipo,

		long expiraEnSegundos,

		UsuarioResponse usuario) {
}