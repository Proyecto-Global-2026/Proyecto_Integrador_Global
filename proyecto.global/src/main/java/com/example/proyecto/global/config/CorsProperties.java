package com.example.proyecto.global.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Origenes permitidos por la capa de presentacion para consumir la API.
 * Configurable por entorno; nunca hardcodeada.
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(

		@DefaultValue("http://localhost:5173,http://localhost:3000") List<String> allowedOrigins,

		@DefaultValue({ "GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS" }) List<String> allowedMethods,

		@DefaultValue("*") List<String> allowedHeaders,

		@DefaultValue({ "Location", "Content-Disposition" }) List<String> exposedHeaders,

		@DefaultValue("true") boolean allowCredentials,

		@DefaultValue("3600") long maxAge) {
}