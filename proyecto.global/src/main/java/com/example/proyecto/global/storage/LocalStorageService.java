package com.example.proyecto.global.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.proyecto.global.config.StorageProperties;
import com.example.proyecto.global.exception.BusinessRuleException;

public class LocalStorageService implements StorageService {

	private static final Logger log = LoggerFactory.getLogger(LocalStorageService.class);
	private final Path directorioRaiz;

	public LocalStorageService(StorageProperties properties) {
		this.directorioRaiz = Paths.get(properties.local().directorio()).toAbsolutePath().normalize();
		if (properties.local().crearDirectorio()) {
			try {
				Files.createDirectories(directorioRaiz);
			} catch (IOException e) {
				throw new BusinessRuleException("No se pudo crear el directorio de almacenamiento: " + e.getMessage());
			}
		}
	}

	@Override
	public ArchivoReferencia guardar(MultipartFile archivo, String carpeta) {
		if (archivo == null || archivo.isEmpty()) {
			throw new BusinessRuleException("El archivo a almacenar esta vacio");
		}
		String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename());
		String extension = obtenerExtension(nombreOriginal);
		String nombreAlmacenado = UUID.randomUUID() + (extension.isEmpty() ? "" : "." + extension);
		Path destino = directorioRaiz.resolve(nombreAlmacenado);

		try {
			Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			log.error("Error guardando archivo local: {}", e.getMessage());
			throw new BusinessRuleException("No se pudo guardar el archivo");
		}

		return new ArchivoReferencia(
				"local",
				null,
				nombreAlmacenado,
				nombreAlmacenado,
				"raw",
				"local",
				extension,
				archivo.getContentType(),
				archivo.getSize(),
				nombreOriginal);
	}

	@Override
	public Optional<ArchivoReferencia> obtenerReferencia(String publicId, String resourceType, String tipoEntrega) {
		return Optional.empty();
	}

	@Override
	public String generarUrlDescargaSegura(String publicId, String resourceType, String tipoEntrega, String nombreOriginal,
			Duration expiracion) {
		return null;
	}

	@Override
	public InputStream leer(String publicId, String resourceType, String tipoEntrega) {
		throw new UnsupportedOperationException("Lectura directa por publicId no soportada en almacenamiento local");
	}

	@Override
	public void eliminar(String publicId, String resourceType, String tipoEntrega) {
	}

	private String obtenerExtension(String nombre) {
		if (nombre == null || !nombre.contains(".")) {
			return "";
		}
		return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase();
	}
}