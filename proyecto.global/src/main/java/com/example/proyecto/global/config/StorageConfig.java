package com.example.proyecto.global.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.proyecto.global.storage.CloudinaryStorageService;
import com.example.proyecto.global.storage.LocalStorageService;
import com.example.proyecto.global.storage.StorageService;

@Configuration
@EnableConfigurationProperties(StorageProperties.class)
public class StorageConfig {

	private static final Logger log = LoggerFactory.getLogger(StorageConfig.class);

	private final StorageProperties properties;

	public StorageConfig(StorageProperties properties) {
		this.properties = properties;
	}

	@Bean
	@ConditionalOnProperty(name = "app.storage.proveedor", havingValue = "cloudinary")
	public Cloudinary cloudinary() {
		StorageProperties.CloudinaryProps c = properties.cloudinary();
		log.info("Almacenamiento externo activo: Cloudinary (cloud_name={}, folder={}, tipo_entrega={}, vigencia_url={} min)",
				c.cloudName(), c.carpetaBase(), c.tipoEntrega(), c.duracionUrlMinutos());
		return new Cloudinary(ObjectUtils.asMap(
				"cloud_name", c.cloudName(),
				"api_key", c.apiKey(),
				"api_secret", c.apiSecret(),
				"secure", c.secure()));
	}

	@Bean
	@Primary
	@ConditionalOnProperty(name = "app.storage.proveedor", havingValue = "cloudinary")
	public StorageService cloudinaryStorageService(Cloudinary cloudinary) {
		return new CloudinaryStorageService(cloudinary, properties);
	}

	@Bean
	@Primary
	@ConditionalOnProperty(name = "app.storage.proveedor", havingValue = "local", matchIfMissing = true)
	public StorageService localStorageService() {
		log.info("Almacenamiento local activo en: {}", properties.local().directorio());
		return new LocalStorageService(properties);
	}
}