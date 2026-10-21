package com.example.proyecto.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(
		@DefaultValue("local") String proveedor,
		@DefaultValue LocalProps local,
		@DefaultValue CloudinaryProps cloudinary,
		@DefaultValue("10485760") long maxTamanoBytes,
		@DefaultValue({ "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png", "gif",
				"webp", "zip", "rar", "txt", "csv" }) List<String> extensionesPermitidas) {

	public record LocalProps(
			@DefaultValue("uploads/planeaciones") String directorio,
			@DefaultValue("true") boolean crearDirectorio) {
	}

	public record CloudinaryProps(
			String cloudName,
			String apiKey,
			String apiSecret,
			@DefaultValue("true") boolean secure,
			@DefaultValue("5000") long timeoutMs,
			@DefaultValue("raw") String resourceTypePorDefecto,
			@DefaultValue("proyecto-global") String carpetaBase,
			/**
			 * Tipo de entrega de Cloudinary con el que se suben los archivos.
			 * "authenticated" deja el recurso protegido: sin URL firmada responde 401.
			 */
			@DefaultValue("authenticated") String tipoEntrega,
			/**
			 * Vigencia en minutos de las URLs firmadas de descarga.
			 */
			@DefaultValue("5") long duracionUrlMinutos) {
	}
}