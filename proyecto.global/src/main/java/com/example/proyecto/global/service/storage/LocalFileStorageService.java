package com.example.proyecto.global.service.storage;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.proyecto.global.exception.BusinessRuleException;

/**
 * Almacenamiento local en disco. La carpeta es configurable con
 * app.storage.local.directorio (por defecto: uploads/planeaciones).
 */
@Service
public class LocalFileStorageService implements FileStorageService {

	private final Path directorioBase;

	public LocalFileStorageService(
			@Value("${app.storage.local.directorio:uploads/planeaciones}") String directorio) {
		this.directorioBase = Paths.get(directorio).toAbsolutePath().normalize();
		try {
			Files.createDirectories(directorioBase);
		} catch (IOException ex) {
			throw new IllegalStateException("No se pudo crear el directorio de almacenamiento: " + directorioBase,
					ex);
		}
	}

	@Override
	public String guardar(MultipartFile archivo, String nombreAlmacenado) {
		Path destino = resolveSeguro(nombreAlmacenado);
		try {
			Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
			return nombreAlmacenado;
		} catch (IOException ex) {
			throw new BusinessRuleException("No se pudo guardar el archivo");
		}
	}

	@Override
	public InputStream abrir(String nombreAlmacenado) {
		Path origen = resolveSeguro(nombreAlmacenado);
		try {
			return Files.newInputStream(origen);
		} catch (IOException ex) {
			throw new BusinessRuleException("No se pudo leer el archivo almacenado");
		}
	}

	@Override
	public void eliminar(String nombreAlmacenado) {
		try {
			Files.deleteIfExists(resolveSeguro(nombreAlmacenado));
		} catch (IOException ex) {
			throw new BusinessRuleException("No se pudo eliminar el archivo almacenado");
		}
	}

	/**
	 * Evita path traversal: el nombre almacenado se genera en el servidor
	 * (UUID + extension), nunca viene del cliente, pero se valida igual.
	 */
	private Path resolveSeguro(String nombreAlmacenado) {
		Path resuelto = directorioBase.resolve(nombreAlmacenado).normalize();
		if (!resuelto.startsWith(directorioBase)) {
			throw new BusinessRuleException("Nombre de archivo invalido");
		}
		return resuelto;
	}
}