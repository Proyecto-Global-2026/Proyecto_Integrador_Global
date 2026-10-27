package com.example.proyecto.global.storage;

import java.io.InputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.cloudinary.Cloudinary;
import com.cloudinary.api.ApiResponse;
import com.cloudinary.utils.ObjectUtils;
import com.example.proyecto.global.config.StorageProperties;
import com.example.proyecto.global.exception.BusinessRuleException;

/**
 * Almacenamiento externo en Cloudinary.
 *
 * <p>
 * Los archivos se suben con tipo de entrega "authenticated", por lo que el
 * recurso queda protegido: el CDN responde 401 a cualquier peticion que no
 * incluya una URL firmada valida.
 * </p>
 *
 * <p>
 * Las descargas no se sirven desde el CDN sino mediante la API administrativa
 * de Cloudinary ({@code /download}), que permite generar una URL firmada y con
 * vigencia limitada ({@code expires_at}) y devolver el archivo como adjunto
 * con su nombre original. Ese mecanismo es el unico que funciona con formatos
 * restringidos (PDF, ZIP) y el unico que ofrece expiracion real.
 * </p>
 */
public class CloudinaryStorageService implements StorageService {

	private static final Logger log = LoggerFactory.getLogger(CloudinaryStorageService.class);
	private static final String TIPO_ENTREGA_POR_DEFECTO = "authenticated";

	private final Cloudinary cloudinary;
	private final StorageProperties properties;

	public CloudinaryStorageService(Cloudinary cloudinary, StorageProperties properties) {
		this.cloudinary = cloudinary;
		this.properties = properties;
	}

	@Override
	public ArchivoReferencia guardar(MultipartFile archivo, String carpeta) {
		if (archivo == null || archivo.isEmpty()) {
			throw new BusinessRuleException("El archivo a almacenar esta vacio");
		}
		validarTamano(archivo.getSize());

		String nombreOriginal = archivo.getOriginalFilename();
		String contentType = archivo.getContentType();
		String resourceType = determinarResourceType(contentType, nombreOriginal);
		String tipoEntrega = properties.cloudinary().tipoEntrega() == null
				? TIPO_ENTREGA_POR_DEFECTO
				: properties.cloudinary().tipoEntrega();
		String carpetaFinal = (carpeta == null || carpeta.isBlank())
				? properties.cloudinary().carpetaBase()
				: properties.cloudinary().carpetaBase() + "/" + carpeta;
		// El public_id termina con el nombre original para que Cloudinary lo
		// devuelva como nombre de archivo en la descarga. El UUID intermedio
		// evita colisiones entre archivos con el mismo nombre.
		String publicId = carpetaFinal + "/" + UUID.randomUUID() + "/" + nombreSeguro(nombreOriginal);

		try {
			Map uploadResult = cloudinary.uploader().upload(archivo.getBytes(),
					ObjectUtils.asMap(
							"resource_type", resourceType,
							"type", tipoEntrega,
							"public_id", publicId,
							"overwrite", false,
							"use_filename", false,
							"unique_filename", false));

			String publicIdResuelto = (String) uploadResult.get("public_id");
			String secureUrl = (String) uploadResult.get("secure_url");
			String url = (String) uploadResult.getOrDefault("url", secureUrl);
			String formato = (String) uploadResult.get("format");
			long bytes = uploadResult.get("bytes") instanceof Number n ? n.longValue() : archivo.getSize();

			return new ArchivoReferencia(
					"cloudinary",
					publicIdResuelto != null ? publicIdResuelto : publicId,
					url,
					secureUrl,
					resourceType,
					tipoEntrega,
					formato != null ? formato : extension(nombreOriginal),
					contentType,
					bytes,
					nombreOriginal);
		} catch (Exception e) {
			log.error("Error subiendo archivo {} a Cloudinary (folder={}): {}", nombreOriginal, carpetaFinal,
					e.getMessage(), e);
			throw new BusinessRuleException("No se pudo almacenar el archivo en el almacenamiento externo");
		}
	}

	@Override
	public Optional<ArchivoReferencia> obtenerReferencia(String publicId, String resourceType, String tipoEntrega) {
		if (publicId == null || publicId.isBlank()) {
			return Optional.empty();
		}
		String tipoRecurso = resourceType == null ? "raw" : resourceType;
		try {
			ApiResponse response = cloudinary.api().resource(publicId,
					ObjectUtils.asMap("resource_type", tipoRecurso, "type", tipoEntrega));
			Map result = response.get("resource") != null ? (Map) response.get("resource") : response;
			String secureUrl = (String) result.get("secure_url");
			String url = (String) result.getOrDefault("url", secureUrl);
			String formato = (String) result.get("format");
			String contentType = (String) result.get("mime_type");
			long bytes = result.get("bytes") instanceof Number n ? n.longValue() : 0;
			String nombre = (String) result.getOrDefault("display_name", publicId);
			return Optional.of(new ArchivoReferencia(
					"cloudinary",
					publicId,
					url,
					secureUrl,
					(String) result.getOrDefault("resource_type", tipoRecurso),
					(String) result.getOrDefault("type", tipoEntrega),
					formato,
					contentType,
					bytes,
					nombre));
		} catch (Exception e) {
			log.warn("No se pudo obtener recurso {} de Cloudinary: {}", publicId, e.getMessage());
			return Optional.empty();
		}
	}

	@Override
	public String generarUrlDescargaSegura(String publicId, String resourceType, String tipoEntrega,
			String nombreOriginal, Duration expiracion) {
		if (publicId == null || publicId.isBlank()) {
			throw new BusinessRuleException("Referencia de archivo invalida");
		}
		String tipoRecurso = (resourceType == null || resourceType.isBlank())
				? properties.cloudinary().resourceTypePorDefecto()
				: resourceType;
		String entrega = (tipoEntrega == null || tipoEntrega.isBlank()) ? TIPO_ENTREGA_POR_DEFECTO : tipoEntrega;
		Duration duracion = expiracion == null
				? Duration.ofMinutes(properties.cloudinary().duracionUrlMinutos())
				: expiracion;
		long expiresAt = Instant.now().plus(duracion).getEpochSecond();
		try {
			// El formato se deduce del public_id (por ejemplo, "evidencia.pdf" -> "pdf").
			String formato = extension(publicId);
			// Cloudinary exige "attachment=true" para forzar la descarga. Si se
			// conoce el nombre original se envia para que el navegador guarde el
			// archivo con ese nombre.
			Object adjunto = StringUtils.hasText(nombreOriginal) ? nombreOriginal : Boolean.TRUE;
			Map<String, Object> opciones = ObjectUtils.asMap(
					"resource_type", tipoRecurso,
					"type", entrega,
					"expires_at", expiresAt,
					"attachment", adjunto);
			return cloudinary.privateDownload(publicId, formato, opciones);
		} catch (Exception e) {
			log.error("Error generando URL de descarga para {}: {}", publicId, e.getMessage());
			throw new BusinessRuleException("No se pudo generar el enlace de descarga del archivo");
		}
	}

	@Override
	public InputStream leer(String publicId, String resourceType, String tipoEntrega) {
		String tipoRecurso = resourceType == null ? "raw" : resourceType;
		try {
			String url = generarUrlDescargaSegura(publicId, tipoRecurso, tipoEntrega, null,
					Duration.ofMinutes(properties.cloudinary().duracionUrlMinutos()));
			return java.net.URI.create(url).toURL().openStream();
		} catch (Exception e) {
			log.error("Error leyendo archivo {} desde Cloudinary: {}", publicId, e.getMessage());
			throw new BusinessRuleException("No se pudo leer el archivo");
		}
	}

	@Override
	public void eliminar(String publicId, String resourceType, String tipoEntrega) {
		if (publicId == null || publicId.isBlank()) {
			return;
		}
		String tipoRecurso = resourceType == null ? "raw" : resourceType;
		try {
			cloudinary.uploader().destroy(publicId,
					ObjectUtils.asMap("resource_type", tipoRecurso, "type", tipoEntrega));
		} catch (Exception e) {
			log.warn("No se pudo eliminar archivo {} en Cloudinary: {}", publicId, e.getMessage());
		}
	}

	private String determinarResourceType(String contentType, String nombreOriginal) {
		if (contentType != null) {
			String ct = contentType.toLowerCase();
			if (ct.startsWith("image/")) {
				return "image";
			}
			if (ct.startsWith("video/")) {
				return "video";
			}
		}
		String nombre = nombreOriginal == null ? "" : nombreOriginal.toLowerCase();
		if (nombre.endsWith(".pdf") || nombre.endsWith(".doc") || nombre.endsWith(".docx")
				|| nombre.endsWith(".xls") || nombre.endsWith(".xlsx") || nombre.endsWith(".ppt")
				|| nombre.endsWith(".pptx") || nombre.endsWith(".zip") || nombre.endsWith(".rar")
				|| nombre.endsWith(".txt") || nombre.endsWith(".csv")) {
			return "raw";
		}
		return properties.cloudinary().resourceTypePorDefecto();
	}

	private void validarTamano(long tamano) {
		if (tamano > properties.maxTamanoBytes()) {
			throw new BusinessRuleException("El archivo supera el tamano maximo permitido");
		}
	}

	private String nombreDesdePublicId(String publicId) {
		if (publicId == null) {
			return "archivo";
		}
		int ultimoSlash = publicId.lastIndexOf('/');
		return ultimoSlash >= 0 ? publicId.substring(ultimoSlash + 1) : publicId;
	}

	String nombreSeguro(String nombreOriginal) {
		String nombre = StringUtils.cleanPath(nombreOriginal == null ? "archivo" : nombreOriginal);
		nombre = nombre.replaceAll("[\\\\/:*?\"<>|\\r\\n\\t]", "_").replaceAll("\\s+", "_");
		if (nombre.isBlank() || nombre.replace(".", "").replace("_", "").isBlank()) {
			nombre = "archivo";
		}
		return nombre.length() > 120 ? nombre.substring(nombre.length() - 120) : nombre;
	}

	String extension(String nombre) {
		if (nombre == null || !nombre.contains(".")) {
			return "";
		}
		return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
	}
}
