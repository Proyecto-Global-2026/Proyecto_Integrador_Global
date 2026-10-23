package com.example.proyecto.global.service.retencion;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.config.RetencionProperties;
import com.example.proyecto.global.domain.ArchivoPlaneacion;
import com.example.proyecto.global.domain.RevisionPlaneacion;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.repository.ArchivoPlaneacionRepository;
import com.example.proyecto.global.repository.RevisionPlaneacionRepository;
import com.example.proyecto.global.repository.UsuarioRepository;
import com.example.proyecto.global.service.calendario.CalendarioService;
import com.example.proyecto.global.storage.StorageService;

/**
 * Aplica la politica de retencion y minimizacion de datos personales (S3-07).
 *
 * <p>
 * Cuando un dato personal deja de ser necesario para la finalidad con la que se
 * recogio, el proceso lo anonimiza o lo elimina:
 * </p>
 *
 * <ul>
 * <li>Usuarios dados de baja: se anonimizan nombre, correo y credenciales
 * cuando superan el plazo configurado. La fila se conserva para mantener la
 * trazabilidad de planeaciones y revisiones historicas.</li>
 * <li>Archivos adjuntos: se eliminan del proveedor de almacenamiento y del
 * disco cuando superan el plazo de conservacion.</li>
 * <li>Historial de revisiones: se elimina cuando supera el plazo maximo de
 * conservacion.</li>
 * <li>Conexiones con Google Calendar: se revocan al anonimizar al usuario,
 * eliminando los tokens y eventos asociados.</li>
 * </ul>
 */
@Service
public class RetencionDatosService {

	private static final Logger log = LoggerFactory.getLogger(RetencionDatosService.class);

	static final String MOTIVO_RETENCION = "RETENCION_DATOS";
	static final String NOMBRE_ANONIMIZADO = "Usuario Anonimizado";

	private final RetencionProperties properties;
	private final UsuarioRepository usuarioRepository;
	private final ArchivoPlaneacionRepository archivoPlaneacionRepository;
	private final RevisionPlaneacionRepository revisionPlaneacionRepository;
	private final StorageService storageService;
	private final CalendarioService calendarioService;
	private final PasswordEncoder passwordEncoder;

	public RetencionDatosService(RetencionProperties properties, UsuarioRepository usuarioRepository,
			ArchivoPlaneacionRepository archivoPlaneacionRepository,
			RevisionPlaneacionRepository revisionPlaneacionRepository, StorageService storageService,
			CalendarioService calendarioService, PasswordEncoder passwordEncoder) {
		this.properties = properties;
		this.usuarioRepository = usuarioRepository;
		this.archivoPlaneacionRepository = archivoPlaneacionRepository;
		this.revisionPlaneacionRepository = revisionPlaneacionRepository;
		this.storageService = storageService;
		this.calendarioService = calendarioService;
		this.passwordEncoder = passwordEncoder;
	}

	@Transactional
	public ResultadoRetencion ejecutar() {
		if (!properties.habilitado()) {
			log.debug("Proceso de retencion deshabilitado por configuracion");
			return ResultadoRetencion.vacio();
		}

		OffsetDateTime ahora = OffsetDateTime.now();
		int usuariosAnonimizados = anonimizarUsuariosInactivos(ahora);
		int archivosEliminados = eliminarArchivosAntiguos(ahora);
		int revisionesEliminadas = limpiarRevisionesAntiguas(ahora);

		log.info("Retencion de datos finalizada: usuariosAnonimizados={}, archivosEliminados={}, revisionesEliminadas={}",
				usuariosAnonimizados, archivosEliminados, revisionesEliminadas);

		return new ResultadoRetencion(usuariosAnonimizados, archivosEliminados, revisionesEliminadas);
	}

	private int anonimizarUsuariosInactivos(OffsetDateTime ahora) {
		OffsetDateTime limite = ahora.minusDays(properties.diasInactividadUsuario());
		List<Usuario> candidatos = usuarioRepository.findInactivosParaAnonimizar(limite);

		int contador = 0;
		for (Usuario usuario : candidatos) {
			if (usuario.isAnonimizado()) {
				continue;
			}
			String uuid = UUID.randomUUID().toString().substring(0, 8);
			String emailAnonimo = "anonimizado-" + uuid + "@" + properties.dominioAnonimizado();
			String passwordHash = passwordEncoder.encode(UUID.randomUUID().toString());

			revocarCalendario(usuario);
			usuario.anonimizar(NOMBRE_ANONIMIZADO, emailAnonimo, passwordHash, MOTIVO_RETENCION);
			usuarioRepository.save(usuario);
			contador++;
			log.info("Usuario {} anonimizado por politica de retencion", usuario.getId());
		}
		return contador;
	}

	/**
	 * Al anonimizar al usuario se revoca tambien su acceso a Google Calendar:
	 * los tokens y el correo de la cuenta son datos personales que ya no deben
	 * conservarse.
	 */
	private void revocarCalendario(Usuario usuario) {
		try {
			calendarioService.desconectar(usuario.getId());
		} catch (Exception ex) {
			log.warn("No se pudo revocar el calendario del usuario {}: {}", usuario.getId(), ex.getMessage());
		}
	}

	private int eliminarArchivosAntiguos(OffsetDateTime ahora) {
		OffsetDateTime limite = ahora.minusDays(properties.diasEliminacionArchivos());
		List<ArchivoPlaneacion> archivos = archivoPlaneacionRepository.findAntiguosParaEliminar(limite);

		int contador = 0;
		for (ArchivoPlaneacion archivo : archivos) {
			try {
				if (archivo.getPublicId() != null) {
					storageService.eliminar(archivo.getPublicId(), archivo.getResourceType(), archivo.getTipoEntrega());
				}
			} catch (Exception ex) {
				log.warn("No se pudo eliminar el archivo {} del proveedor de almacenamiento: {}",
						archivo.getId(), ex.getMessage());
			}
			eliminarArchivoLocal(archivo);
			archivo.marcarEliminadoPorRetencion();
			archivoPlaneacionRepository.save(archivo);
			contador++;
			log.info("Archivo {} eliminado por politica de retencion", archivo.getId());
		}
		return contador;
	}

	private void eliminarArchivoLocal(ArchivoPlaneacion archivo) {
		if (archivo.getRuta() == null || archivo.getRuta().isBlank()) {
			return;
		}
		try {
			Path ruta = Paths.get(archivo.getRuta());
			if (Files.deleteIfExists(ruta)) {
				log.debug("Archivo fisico {} eliminado", archivo.getRuta());
			}
		} catch (IOException ex) {
			log.debug("No se pudo borrar el archivo fisico {}: {}", archivo.getRuta(), ex.getMessage());
		}
	}

	private int limpiarRevisionesAntiguas(OffsetDateTime ahora) {
		OffsetDateTime limite = ahora.minusDays(properties.diasLimpiezaRevisiones());
		List<RevisionPlaneacion> revisiones = revisionPlaneacionRepository.findAntiguasParaLimpieza(limite);
		if (revisiones.isEmpty()) {
			return 0;
		}
		revisionPlaneacionRepository.deleteAllInBatch(revisiones);
		log.info("{} revisiones historicas eliminadas por politica de retencion", revisiones.size());
		return revisiones.size();
	}

	public record ResultadoRetencion(int usuariosAnonimizados, int archivosEliminados, int revisionesEliminadas) {
		public static ResultadoRetencion vacio() {
			return new ResultadoRetencion(0, 0, 0);
		}
	}
}
