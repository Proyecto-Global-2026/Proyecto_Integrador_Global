package com.example.proyecto.global.storage;

/**
 * Referencia a un archivo persistido en un proveedor de almacenamiento.
 *
 * @param proveedor     identificador del proveedor ("local", "cloudinary")
 * @param publicId      identificador del recurso en el proveedor
 * @param url           ruta publica o clave local del archivo
 * @param secureUrl     ruta segura del archivo en el proveedor
 * @param resourceType  tipo de recurso en Cloudinary ("image", "video", "raw")
 * @param tipoEntrega   tipo de entrega en Cloudinary ("authenticated", "upload", "private")
 * @param formato       extension del archivo
 * @param contentType   tipo MIME del archivo
 * @param tamano        tamano en bytes
 * @param nombreOriginal nombre original del archivo subido por el usuario
 */
public record ArchivoReferencia(
		String proveedor,
		String publicId,
		String url,
		String secureUrl,
		String resourceType,
		String tipoEntrega,
		String formato,
		String contentType,
		long tamano,
		String nombreOriginal) {
}
