package com.example.proyecto.global.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.example.proyecto.global.domain.Evidencia;
import com.example.proyecto.global.domain.EstadoEvidencia;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.EvidenciaRequest;
import com.example.proyecto.global.dto.EvidenciaResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.EvidenciaRepository;
import com.example.proyecto.global.repository.UsuarioRepository;
import com.example.proyecto.global.config.StorageProperties;
import com.example.proyecto.global.storage.ArchivoReferencia;
import com.example.proyecto.global.storage.StorageService;

@Service
public class EvidenciaService {

	private static final Logger log = LoggerFactory.getLogger(EvidenciaService.class);

	private static final String ROL_DOCENTE = "DOCENTE";
	private static final String ROL_COORDINADOR = "COORDINADOR";
	private static final String ROL_DIRECCION = "DIRECCION";

	private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
			"pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "jpg", "jpeg", "png");

	private final EvidenciaRepository evidenciaRepository;
	private final UsuarioRepository usuarioRepository;
	private final StorageService storageService;
	private final StorageProperties properties;
	private final long maxTamanoBytes;

	public EvidenciaService(EvidenciaRepository evidenciaRepository,
			UsuarioRepository usuarioRepository,
			StorageService storageService,
			StorageProperties properties,
			@Value("${app.storage.max-tamano-bytes:10485760}") long maxTamanoBytes) {
		this.evidenciaRepository = evidenciaRepository;
		this.usuarioRepository = usuarioRepository;
		this.storageService = storageService;
		this.properties = properties;
		this.maxTamanoBytes = maxTamanoBytes;
	}

	/**
	 * El docente registra una evidencia de capacitacion con un archivo opcional.
	 */
	@Transactional
	public EvidenciaResponse crear(EvidenciaRequest request, MultipartFile archivo, UUID idSolicitante) {
		Usuario docente = usuarioRepository.findById(idSolicitante)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + idSolicitante));
		Evidencia evidencia = new Evidencia(docente, request.curso().trim(), request.institucion().trim(),
				request.fecha(), request.horasAcreditadas());
		adjuntarSiCorresponde(evidencia, archivo);
		return EvidenciaResponse.from(evidenciaRepository.save(evidencia));
	}

	/**
	 * El docente solo ve sus evidencias. Coordinacion y direccion pueden
	 * consultar todas y filtrar por docente.
	 */
	@Transactional(readOnly = true)
	public Page<Evidencia> listar(UUID usuarioId, EstadoEvidencia estado, String q, String rolSolicitante,
			UUID idSolicitante, Pageable pageable) {
		UUID filtroUsuario = usuarioId;
		if (ROL_DOCENTE.equals(rolSolicitante)) {
			filtroUsuario = idSolicitante;
		}
		return evidenciaRepository.buscar(filtroUsuario, estado, normalizar(q), pageable);
	}

	@Transactional(readOnly = true)
	public Evidencia buscarPorId(UUID id, String rolSolicitante, UUID idSolicitante) {
		Evidencia evidencia = obtener(id);
		verificarAccesoLectura(evidencia, rolSolicitante, idSolicitante);
		return evidencia;
	}

	/**
	 * Solo el docente propietario actualiza su evidencia. Si se envia un
	 * archivo nuevo, reemplaza al anterior.
	 */
	@Transactional
	public EvidenciaResponse actualizar(UUID id, EvidenciaRequest request, MultipartFile archivo, UUID idSolicitante) {
		Evidencia evidencia = obtener(id);
		if (!evidencia.getUsuario().getId().equals(idSolicitante)) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede actualizar su evidencia");
		}
		evidencia.actualizar(request.curso().trim(), request.institucion().trim(), request.fecha(),
				request.horasAcreditadas());
		if (archivo != null && !archivo.isEmpty()) {
			String anterior = evidencia.getNombreAlmacenado();
			String proveedorAnterior = evidencia.getProveedorAlmacenamiento();
			String publicIdAnterior = evidencia.getPublicId();
			String resourceTypeAnterior = evidencia.getResourceType();
			String tipoEntregaAnterior = evidencia.getTipoEntrega();
			adjuntarSiCorresponde(evidencia, archivo);
			if (anterior != null) {
				if (publicIdAnterior != null && !"local".equalsIgnoreCase(proveedorAnterior)) {
					try {
						storageService.eliminar(publicIdAnterior, resourceTypeAnterior, tipoEntregaAnterior);
					} catch (Exception ignored) {
					}
				}
			}
		}
		evidencia.reenviarRevision();
		return EvidenciaResponse.from(evidencia);
	}

	/**
	 * Solo el docente propietario elimina su evidencia junto con su archivo.
	 */
	@Transactional
	public void eliminar(UUID id, UUID idSolicitante) {
		Evidencia evidencia = obtener(id);
		if (!evidencia.getUsuario().getId().equals(idSolicitante)) {
			throw new AuthorizationDeniedException("Solo el docente propietario puede eliminar su evidencia");
		}
		if (evidencia.tieneArchivo()) {
			if (evidencia.getPublicId() != null && !"local".equalsIgnoreCase(evidencia.getProveedorAlmacenamiento())) {
				try {
					storageService.eliminar(evidencia.getPublicId(), evidencia.getResourceType(),
							evidencia.getTipoEntrega());
				} catch (Exception ignored) {
				}
			}
		}
		evidenciaRepository.delete(evidencia);
	}

	/**
	 * Descarga el archivo adjunto de una evidencia.
	 */
	@Transactional(readOnly = true)
	public Descarga descargarArchivo(UUID id, String rolSolicitante, UUID idSolicitante) {
		Evidencia evidencia = obtener(id);
		verificarAccesoLectura(evidencia, rolSolicitante, idSolicitante);
		if (!evidencia.tieneArchivo()) {
			throw new ResourceNotFoundException("La evidencia no tiene archivo adjunto: " + id);
		}
		java.io.InputStream contenido = null;
		try {
			if (evidencia.getPublicId() != null && !"local".equalsIgnoreCase(evidencia.getProveedorAlmacenamiento())) {
				contenido = storageService.leer(evidencia.getPublicId(), evidencia.getResourceType(),
						evidencia.getTipoEntrega());
			} else {
				contenido = new java.io.FileInputStream(java.nio.file.Paths.get(properties.local().directorio())
						.resolve(evidencia.getNombreAlmacenado()).toFile());
			}
		} catch (java.io.IOException e) {
			throw new BusinessRuleException("No se pudo leer el archivo");
		}
		return new Descarga(
				evidencia.getNombreOriginal(),
				evidencia.getContentType(),
				evidencia.getTamano(),
				contenido);
	}

	private void adjuntarSiCorresponde(Evidencia evidencia, MultipartFile archivo) {
		if (archivo == null || archivo.isEmpty()) {
			return;
		}
		validarArchivo(archivo);
		try {
			ArchivoReferencia ref = storageService.guardar(archivo, "evidencias");
			String clave = ref.publicId() != null ? ref.publicId() : (ref.url() != null ? ref.url() : ref.secureUrl());
			String ruta = ref.secureUrl() != null ? ref.secureUrl() : ref.url();
			evidencia.adjuntarArchivo(archivo.getOriginalFilename(), clave,
					archivo.getContentType(), archivo.getSize(), ruta,
					ref.proveedor(), ref.publicId(), ref.resourceType(), ref.tipoEntrega());
		} catch (Exception e) {
			log.error("Error al guardar el archivo adjunto en el proveedor de almacenamiento: {}",
					e.getMessage(), e);
			throw new BusinessRuleException("No se pudo guardar el archivo adjunto");
		}
	}

	private void validarArchivo(MultipartFile archivo) {
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

	private Evidencia obtener(UUID id) {
		return evidenciaRepository.buscarPorIdConUsuario(id)
				.orElseThrow(() -> new ResourceNotFoundException("Evidencia no encontrada: " + id));
	}

	private void verificarAccesoLectura(Evidencia evidencia, String rolSolicitante, UUID idSolicitante) {
		boolean esPropietario = evidencia.getUsuario().getId().equals(idSolicitante);
		boolean esRevision = ROL_COORDINADOR.equals(rolSolicitante) || ROL_DIRECCION.equals(rolSolicitante);
		if (!esPropietario && !esRevision) {
			throw new AuthorizationDeniedException("No tienes permisos para consultar esta evidencia");
		}
	}

	private String normalizar(String q) {
		return q == null ? null : q.trim().toLowerCase(Locale.ROOT);
	}

	/**
	 * Datos de una descarga: metadatos + contenido.
	 */
	public record Descarga(String nombreOriginal, String contentType, Long tamano, InputStream contenido) {
	}
}
