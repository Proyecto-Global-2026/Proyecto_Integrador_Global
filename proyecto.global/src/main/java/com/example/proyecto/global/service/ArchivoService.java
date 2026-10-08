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

import com.example.proyecto.global.domain.ArchivoPlaneacion;
import com.example.proyecto.global.domain.Planeacion;
import com.example.proyecto.global.dto.ArchivoResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.ArchivoPlaneacionRepository;
import com.example.proyecto.global.repository.PlaneacionRepository;
import com.example.proyecto.global.service.storage.FileStorageService;

@Service
public class ArchivoService {

	private static final String ROL_COORDINADOR = "COORDINADOR";
	private static final String ROL_DIRECCION = "DIRECCION";

	private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
			"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png");

	private final ArchivoPlaneacionRepository archivoRepository;
	private final PlaneacionRepository planeacionRepository;
	private final FileStorageService storageService;
	private final long maxTamanoBytes;

	public ArchivoService(ArchivoPlaneacionRepository archivoRepository,
			PlaneacionRepository planeacionRepository,
			FileStorageService storageService,
			@Value("${app.storage.max-tamano-bytes:10485760}") long maxTamanoBytes) {
		this.archivoRepository = archivoRepository;
		this.planeacionRepository = planeacionRepository;
		this.storageService = storageService;
		this.maxTamanoBytes = maxTamanoBytes;
	}

	/**
	 * El docente propietario adjunta un archivo a su planeacion.
	 */
	@Transactional
	public ArchivoResponse subir(UUID planeacionId, MultipartFile archivo, UUID idSolicitante) {
		Planeacion planeacion = planeacionRepository.findById(planeacionId)
				.orElseThrow(() -> new ResourceNotFoundException("Planeacion no encontrada: " + planeacionId));
		if (!planeacion.getDocente().getId().equals(idSolicitante)) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede adjuntar archivos");
		}
		validarArchivo(archivo);

		String nombreAlmacenado = UUID.randomUUID() + "." + extension(archivo.getOriginalFilename());
		storageService.guardar(archivo, nombreAlmacenado);

		ArchivoPlaneacion entidad = new ArchivoPlaneacion(
				planeacion,
				archivo.getOriginalFilename(),
				nombreAlmacenado,
				archivo.getContentType(),
				archivo.getSize(),
				nombreAlmacenado);
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
		return new Descarga(
				archivo.getNombreOriginal(),
				archivo.getContentType(),
				archivo.getTamano(),
				storageService.abrir(archivo.getNombreAlmacenado()));
	}

	/**
	 * Solo el docente propietario elimina sus adjuntos.
	 */
	@Transactional
	public void eliminar(UUID archivoId, UUID idSolicitante) {
		ArchivoPlaneacion archivo = archivoRepository.buscarPorIdConPlaneacion(archivoId)
				.orElseThrow(() -> new ResourceNotFoundException("Archivo no encontrado: " + archivoId));
		if (!archivo.getPlaneacion().getDocente().getId().equals(idSolicitante)) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede eliminar archivos");
		}
		storageService.eliminar(archivo.getNombreAlmacenado());
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
		boolean esPropietario = planeacion.getDocente().getId().equals(idSolicitante);
		boolean esRevision = ROL_COORDINADOR.equals(rolSolicitante) || ROL_DIRECCION.equals(rolSolicitante);
		if (!esPropietario && !esRevision) {
			throw new AuthorizationDeniedException("No tienes permisos para consultar los archivos de esta planeacion");
		}
	}

	/**
	 * Datos de una descarga: metadatos + contenido.
	 */
	public record Descarga(String nombreOriginal, String contentType, long tamano, InputStream contenido) {
	}
}