package com.example.proyecto.global.storage;

import java.io.InputStream;
import java.time.Duration;
import java.util.Optional;

import org.springframework.web.multipart.MultipartFile;

public interface StorageService {

	ArchivoReferencia guardar(MultipartFile archivo, String carpeta);

	Optional<ArchivoReferencia> obtenerReferencia(String publicId, String resourceType, String tipoEntrega);

	/**
	 * Genera una URL firmada y con vigencia limitada para descargar el archivo.
	 * Los proveedores que no soportan redireccion (almacenamiento local) deben
	 * devolver {@code null} para que la API sirva el contenido directamente.
	 */
	String generarUrlDescargaSegura(String publicId, String resourceType, String tipoEntrega, String nombreOriginal,
			Duration expiracion);

	InputStream leer(String publicId, String resourceType, String tipoEntrega);

	void eliminar(String publicId, String resourceType, String tipoEntrega);
}
