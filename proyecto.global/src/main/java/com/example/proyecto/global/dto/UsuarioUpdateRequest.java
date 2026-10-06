package com.example.proyecto.global.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UsuarioUpdateRequest(

		@NotBlank(message = "El nombre es obligatorio")
		@Size(max = 120, message = "El nombre no puede superar 120 caracteres")
		String nombre,

		@NotBlank(message = "El correo es obligatorio")
		@Email(message = "El correo no tiene un formato valido")
		@Size(max = 180, message = "El correo no puede superar 180 caracteres")
		String email,

		@NotBlank(message = "El rol es obligatorio")
		String rol) {
}