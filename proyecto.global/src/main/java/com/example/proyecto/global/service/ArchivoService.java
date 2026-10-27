package com.example.proyecto.global.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.proyecto.global.domain.ArchivoPlaneacion;
import com.example.proyecto.global.domain.Planeacion;
import com.example.proyecto.global.dto.ArchivoResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.ArchivoPlaneacionRepository;
import com.example.proyecto.global.repository.PlaneacionRepository;
import com.example.proyecto.global.security.PoliticaAccesoDatos;
import com.example.proyecto.global.config.StorageProperties;
import com.example.proyecto.global.storage.ArchivoReferencia;
import com.example.proyecto.global.storage.StorageService;

@Service
public class ArchivoService {

	private static final Logger log = LoggerFactory.getLogger(ArchivoService.class);

	private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
			"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png");

	private final ArchivoPlaneacionRepository archivoRepository;
	private final PlaneacionRepository planeacionRepository;
	private final StorageService storageService;
	private final StorageProperties properties;
	private final PoliticaAccesoDatos politica;
	private final long maxTamanoBytes;

	public ArchivoService(ArchivoPlaneacionRepository archivoRepository,
			PlaneacionRepository planeacionRepository,
			StorageService storageService,
			StorageProperties properties,
			PoliticaAccesoDatos politica,
			@Value("${app.storage.max-tamano-bytes:10485760}") long maxTamanoBytes) {
		this.archivoRepository = archivoRepository;
		this.planeacionRepository = planeacionRepository;
		this.storageService = storageService;
		this.properties = properties;
		this.politica = politica;
		this.maxTamanoBytes = maxTamanoBytes;
	}

	/**
	 * El docente propietario adjunta un archivo a su planeacion.
	 */
	@Transactional
	public ArchivoResponse subir(UUID planeacionId, MultipartFile archivo, UUID idSolicitante) {
		Planeacion planeacion = planeacionRepository.findById(planeacionId)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + planeacionId));
		if (!politica.puedeModificarDatosDe(idSolicitante, planeacion.getDocente().getId())) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede adjuntar archivos");
		}
		validarArchivo(archivo);

		ArchivoReferencia ref;
		try {
			ref = storageService.guardar(archivo, "planeaciones");
		} catch (Exception e) {
			log.error("Error al guardar el archivo en el proveedor de almacenamiento: {}", e.getMessage(), e);
			throw new BusinessRuleException("No se pudo guardar el archivo");
		}
		String clave = ref.publicId() != null ? ref.publicId() : (ref.url() != null ? ref.url() : ref.secureUrl());
		String ruta = ref.secureUrl() != null ? ref.secureUrl() : ref.url();

		ArchivoPlaneacion entidad = new ArchivoPlaneacion(
				planeacion,
				archivo.getOriginalFilename(),
				clave,
				archivo.getContentType(),
				archivo.getSize(),
				ruta,
				ref.proveedor(),
				ref.publicId(),
				ref.resourceType(),
				ref.tipoEntrega());
		return ArchivoResponse.from(archivoRepository.save(entidad));
	}

	/**
	 * El docente propietario y los roles de revision pueden listar y
	 * descargar los archivos de una planeacion.
	 */
	@Transactional(readOnly = true)
	public List<ArchivoResponse> listarPorPlaneacion(UUID planeacionId, String rolSolicitante, UUID idSolicitante) {
		Planeacion planeacion = planeacionRepository.findById(planeacionId)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + planeacionId));
		verificarAccesoLectura(planeacion, rolSolicitante, idSolicitante);
		return archivoRepository.listarPorPlaneacionConPlaneacion(planeacionId).stream()
				.map(ArchivoResponse::from)
				.toList();
	}

	/**
	 * Devuelve los metadatos y el stream del archivo para descargarlo.
	 */
	@Transactional(readOnly = true)
	public Descarga descargar(UUID archivoId, String rolSolicitante, UUID idSolicitante) {
		ArchivoPlaneacion archivo = archivoRepository.buscarPorIdConPlaneacion(archivoId)
				.orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado: " + archivoId));
		verificarAccesoLectura(archivo.getPlaneacion(), rolSolicitante, idSolicitante);
		java.io.InputStream contenido;
		try {
			if (archivo.getPublicId() != null && !"local".equalsIgnoreCase(archivo.getProveedorAlmacenamiento())) {
				contenido = storageService.leer(archivo.getPublicId(), archivo.getResourceType(),
						archivo.getTipoEntrega());
			} else {
				contenido = new java.io.FileInputStream(
						java.nio.file.Paths.get(properties.local().directorio())
								.resolve(archivo.getNombreAlmacenado()).toFile());
			}
		} catch (java.io.IOException e) {
			throw new com.example.proyecto.global.exception.BusinessRuleException("No se pudo leer el archivo");
		}
		return new Descarga(
				archivo.getNombreOriginal(),
				archivo.getContentType(),
				archivo.getTamano(),
				contenido);
	}

	/**
	 * Solo el docente propietario elimina sus adjuntos.
	 */
	@Transactional
	public void eliminar(UUID archivoId, UUID idSolicitante) {
		ArchivoPlaneacion archivo = archivoRepository.buscarPorIdConPlaneacion(archivoId)
				.orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado: " + archivoId));
		if (!politica.puedeModificarDatosDe(idSolicitante, archivo.getPlaneacion().getDocente().getId())) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede eliminar archivos");
		}
		if (archivo.getPublicId() != null && !"local".equalsIgnoreCase(archivo.getProveedorAlmacenamiento())) {
			try {
				storageService.eliminar(archivo.getPublicId(), archivo.getResourceType(), archivo.getTipoEntrega());
			} catch (Exception ignored) {
			}
		}
		archivoRepository.delete(archivo);
	}

	private void validarArchivo(MultipartFile archivo) {
		if (archivo == null || archivo.isEmpty()) {
			throw new BusinessRuleException("El archivo es obligatorio");
		}
		String ext = extension(archivo.getOriginalFilename());
		if (!EXTENSIONES_PERMITIDAS.contains(ext)) {
			throw new BusinessRuleException(
					"Tipo de archivo no permitido: ." + ext + ". Formatos admitidos: "
							+ String.join(", ", EXTENSIONES_PERMITIDAS));
		}
		if (archivo.getSize() > maxTamanoBytes) {
			throw new BusinessRuleException(
					"El archivo excede el tamano maximo de " + (maxTamanoBytes / (1024 * 1024)) + " MB");
		}
		try (InputStream abierto = archivo.getInputStream()) {
			if (abierto.read() == -1) {
				throw new BusinessRuleException("El archivo esta vacio");
			}
		} catch (IOException ex) {
			throw new BusinessRuleException("No se pudo leer el archivo enviado");
		}
	}

	private String extension(String nombre) {
		if (nombre == null || !nombre.contains(".")) {
			return "";
		}
		return nombre.substring(nombre.lastIndexOf('.') + 1).toLowerCase(Locale.ROOT);
	}

	private void verificarAccesoLectura(Planeacion planeacion, String rolSolicitante, UUID idSolicitante) {
		if (!politica.puedeConsultarDatosDe(rolSolicitante, idSolicitante, planeacion.getDocente().getId())) {
			throw new AuthorizationDeniedException("No tienes permisos para consultar los archivos de esta planeacion");
		}
	}

	/**
	 * Datos de una descarga: metadatos + contenido.
	 */
	public record Descarga(String nombreOriginal, String contentType, long tamano, InputStream contenido) {
	}
}