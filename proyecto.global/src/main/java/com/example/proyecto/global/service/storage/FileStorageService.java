package com.example.proyecto.global.service.storage;

import java.io.InputStream;

import org.springframework.web.multipart.MultipartFile;

/**
 * Abstraccion del almacenamiento de archivos. La implementacion actual es
 * local (carpeta configurable); la issue #29 agregara la implementacion
 * externa (S3 u otro) sin tocar el resto del codigo.
 */
public interface FileStorageService {

	/**
	 * Guarda el archivo y devuelve el nombre/clave con el que quedo almacenado.
	 */
	String guardar(MultipartFile archivo, String nombreAlmacenado);

	/**
	 * Abre el contenido almacenado para descargarlo.
	 */
	InputStream abrir(String nombreAlmacenado);

	/**
	 * Elimina el archivo almacenado. Si no existe, no hace nada.
	 */
	void eliminar(String nombreAlmacenado);
}