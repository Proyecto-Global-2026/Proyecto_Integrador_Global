package com.example.proyecto.global.service.calendario;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.proyecto.global.config.CalendarioProperties;
import com.example.proyecto.global.domain.ConexionCalendario;
import com.example.proyecto.global.domain.ConsentimientoTerceros;
import com.example.proyecto.global.domain.EventoCalendario;
import com.example.proyecto.global.domain.Parcial;
import com.example.proyecto.global.domain.Periodo;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.ConexionCalendarioResponse;
import com.example.proyecto.global.dto.EventoCalendarioResponse;
import com.example.proyecto.global.dto.SincronizacionCalendarioResponse;
import com.example.proyecto.global.exception.BusinessRuleException;
import com.example.proyecto.global.exception.ResourceNotFoundException;
import com.example.proyecto.global.repository.ConexionCalendarioRepository;
import com.example.proyecto.global.repository.ConsentimientoTercerosRepository;
import com.example.proyecto.global.repository.EventoCalendarioRepository;
import com.example.proyecto.global.repository.ParcialRepository;
import com.example.proyecto.global.repository.UsuarioRepository;

@Service
public class CalendarioService {

	private static final Logger log = LoggerFactory.getLogger(CalendarioService.class);

	private static final int MAX_PARCIALES_POR_SINCRONIZACION = 200;

	private final CalendarioProperties properties;
	private final CalendarioCliente calendarioCliente;
	private final ConexionCalendarioRepository conexionRepository;
	private final EventoCalendarioRepository eventoRepository;
	private final ParcialRepository parcialRepository;
	private final UsuarioRepository usuarioRepository;
	private final ConsentimientoTercerosRepository consentimientoRepository;

	public CalendarioService(CalendarioProperties properties,
			CalendarioCliente calendarioCliente,
			ConexionCalendarioRepository conexionRepository,
			EventoCalendarioRepository eventoRepository,
			ParcialRepository parcialRepository,
			UsuarioRepository usuarioRepository,
			ConsentimientoTercerosRepository consentimientoRepository) {
		this.properties = properties;
		this.calendarioCliente = calendarioCliente;
		this.conexionRepository = conexionRepository;
		this.eventoRepository = eventoRepository;
		this.parcialRepository = parcialRepository;
		this.usuarioRepository = usuarioRepository;
		this.consentimientoRepository = consentimientoRepository;
	}

	@Transactional(readOnly = true)
	public ConexionCalendarioResponse estadoConexion(UUID usuarioId) {
		Optional<ConexionCalendario> conexion = conexionRepository
				.findByUsuarioIdAndProveedor(usuarioId, properties.proveedor());
		if (conexion.isEmpty() || !conexion.get().isActivo()) {
			return ConexionCalendarioResponse.desconectado(properties.habilitado(), properties.proveedor());
		}
		ConexionCalendario c = conexion.get();
		return new ConexionCalendarioResponse(true, c.getProveedor(), c.getCorreoCalendario(),
				c.getAlcance(), c.getExpiraEn(), properties.habilitado());
	}

	@Transactional
	public ConexionCalendarioResponse conectar(UUID usuarioId, String codigo, String redirectUri) {
		exigirHabilitado();
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));

		consentimientoRepository.findByUsuarioIdAndTercero(usuarioId, ConsentimientoTerceros.Tercero.GOOGLE_CALENDAR)
				.filter(ConsentimientoTerceros::isConsentimientoOtorgado)
				.orElseThrow(() -> new BusinessRuleException(
						"Se requiere consentimiento expreso para la transferencia de datos a Google Calendar"));

		TokenCalendario token = calendarioCliente.intercambiarCodigo(codigo, redirectUri);
		if (token.refreshToken() == null || token.refreshToken().isBlank()) {
			throw new BusinessRuleException(
					"Google no devolvio un refresh token. Vuelve a autorizar la aplicacion con acceso sin conexion");
		}

		ConexionCalendario conexion = conexionRepository
				.findByUsuarioIdAndProveedor(usuarioId, properties.proveedor())
				.orElseGet(() -> new ConexionCalendario(usuario, properties.proveedor()));
		conexion.actualizarCredenciales(token.accessToken(), token.refreshToken(), token.expiraEn(),
				token.alcance(), token.correo());
		conexionRepository.save(conexion);

		log.info("Calendario vinculado para el usuario {} (proveedor={})", usuario.getEmail(),
				properties.proveedor());
		return new ConexionCalendarioResponse(true, conexion.getProveedor(), conexion.getCorreoCalendario(),
				conexion.getAlcance(), conexion.getExpiraEn(), properties.habilitado());
	}

	@Transactional
	public void desconectar(UUID usuarioId) {
		Optional<ConexionCalendario> conexion = conexionRepository
				.findByUsuarioIdAndProveedor(usuarioId, properties.proveedor());
		if (conexion.isEmpty()) {
			return;
		}
		ConexionCalendario c = conexion.get();
		if (c.getRefreshToken() != null) {
			try {
				String accessToken = accessTokenVigente(c);
				for (EventoCalendario evento : eventoRepository.findByUsuarioIdOrderByFechaLimiteAsc(usuarioId)) {
					if (evento.getEventoId() != null) {
						try {
							calendarioCliente.eliminarEvento(accessToken, properties.calendarioId(),
									evento.getEventoId());
						} catch (Exception e) {
							log.warn("No se pudo eliminar el evento {} al desconectar: {}",
									evento.getEventoId(), e.getMessage());
						}
					}
				}
			} catch (Exception e) {
				log.warn("No se pudieron limpiar los eventos al desconectar el calendario: {}", e.getMessage());
			}
		}
		eventoRepository.deleteAll(eventoRepository.findByUsuarioIdOrderByFechaLimiteAsc(usuarioId));
		conexionRepository.delete(c);
		log.info("Calendario desvinculado para el usuario {}", usuarioId);
	}

	@Transactional(readOnly = true)
	public List<EventoCalendarioResponse> listarEventos(UUID usuarioId) {
		return eventoRepository.findByUsuarioIdOrderByFechaLimiteAsc(usuarioId).stream()
				.map(EventoCalendarioResponse::from)
				.toList();
	}

	@Transactional
	public SincronizacionCalendarioResponse sincronizar(UUID usuarioId) {
		exigirHabilitado();
		Usuario usuario = usuarioRepository.findById(usuarioId)
				.orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + usuarioId));
		consentimientoRepository.findByUsuarioIdAndTercero(usuarioId, ConsentimientoTerceros.Tercero.GOOGLE_CALENDAR)
				.filter(ConsentimientoTerceros::isConsentimientoOtorgado)
				.orElseThrow(() -> new BusinessRuleException(
						"Se requiere consentimiento expreso para la transferencia de datos a Google Calendar"));
		ConexionCalendario conexion = obtenerConexionActiva(usuarioId);
		String accessToken = accessTokenVigente(conexion);

		LocalDate desde = LocalDate.now();
		LocalDate hasta = desde.plusDays(properties.diasVigencia());
		List<Parcial> parciales = parcialRepository.buscarActivosEntreFechas(desde, hasta,
				PageRequest.of(0, MAX_PARCIALES_POR_SINCRONIZACION));

		int creados = 0;
		int actualizados = 0;
		int omitidos = 0;
		int errores = 0;

		for (Parcial parcial : parciales) {
			try {
				String resultado = sincronizarParcial(usuario, conexion, accessToken, parcial);
				switch (resultado) {
					case "CREADO" -> creados++;
					case "ACTUALIZADO" -> actualizados++;
					default -> omitidos++;
				}
			} catch (Exception e) {
				errores++;
				marcarError(usuario, parcial, e.getMessage());
				log.error("Error sincronizando el parcial {} para {}: {}", parcial.getId(),
						usuario.getEmail(), e.getMessage());
			}
		}

		String mensaje = String.format(
				"Sincronizacion completada: %d creados, %d actualizados, %d sin cambios, %d con error",
				creados, actualizados, omitidos, errores);
		log.info("{} (usuario={})", mensaje, usuario.getEmail());
		return new SincronizacionCalendarioResponse(true, creados, actualizados, omitidos, errores, mensaje);
	}

	private String sincronizarParcial(Usuario usuario, ConexionCalendario conexion, String accessToken,
			Parcial parcial) {
		Optional<EventoCalendario> existente = eventoRepository
				.findByUsuarioIdAndParcialId(usuario.getId(), parcial.getId());

		String titulo = tituloDe(parcial);
		LocalDate fechaLimite = parcial.getFechaFin();
		EventoGoogle evento = new EventoGoogle(titulo, descripcionDe(parcial), fechaLimite,
				properties.zonaHoraria(), properties.recordatorioMinutos());

		if (existente.isEmpty()) {
			String eventoId = calendarioCliente.crearEvento(accessToken, properties.calendarioId(), evento);
			EventoCalendario nuevo = new EventoCalendario(usuario, parcial, titulo, fechaLimite,
					properties.proveedor());
			nuevo.marcarSincronizado(eventoId, fechaLimite, titulo);
			eventoRepository.save(nuevo);
			return "CREADO";
		}

		EventoCalendario registro = existente.get();
		boolean cambioFecha = !fechaLimite.equals(registro.getFechaLimite());
		boolean cambioTitulo = !titulo.equals(registro.getTitulo());
		if (registro.getEventoId() != null && !cambioFecha && !cambioTitulo
				&& registro.getEstado() == com.example.proyecto.global.domain.EstadoEventoCalendario.SINCRONIZADO) {
			return "OMITIDO";
		}

		if (registro.getEventoId() == null) {
			String eventoId = calendarioCliente.crearEvento(accessToken, properties.calendarioId(), evento);
			registro.marcarSincronizado(eventoId, fechaLimite, titulo);
			return "ACTUALIZADO";
		}

		try {
			calendarioCliente.actualizarEvento(accessToken, properties.calendarioId(), registro.getEventoId(),
					evento);
			registro.marcarSincronizado(registro.getEventoId(), fechaLimite, titulo);
			return "ACTUALIZADO";
		} catch (EventoNoEncontradoException e) {
			String eventoId = calendarioCliente.crearEvento(accessToken, properties.calendarioId(), evento);
			registro.marcarSincronizado(eventoId, fechaLimite, titulo);
			return "ACTUALIZADO";
		}
	}

	private void marcarError(Usuario usuario, Parcial parcial, String mensaje) {
		try {
			EventoCalendario registro = eventoRepository
					.findByUsuarioIdAndParcialId(usuario.getId(), parcial.getId())
					.orElseGet(() -> new EventoCalendario(usuario, parcial, tituloDe(parcial),
							parcial.getFechaFin(), properties.proveedor()));
			registro.marcarError(mensaje);
			eventoRepository.save(registro);
		} catch (Exception e) {
			log.warn("No se pudo registrar el error de sincronizacion del parcial {}: {}", parcial.getId(),
					e.getMessage());
		}
	}

	private String accessTokenVigente(ConexionCalendario conexion) {
		if (conexion.tokenVigente()) {
			return conexion.getAccessToken();
		}
		if (!conexion.puedeRenovar()) {
			conexion.setActivo(false);
			throw new BusinessRuleException(
					"La conexion con Google Calendar expiro. Vuelve a vincular tu calendario");
		}
		TokenCalendario renovado = calendarioCliente.renovarToken(conexion.getRefreshToken());
		conexion.actualizarCredenciales(renovado.accessToken(), renovado.refreshToken(), renovado.expiraEn(),
				renovado.alcance(), renovado.correo());
		conexionRepository.save(conexion);
		return conexion.getAccessToken();
	}

	private ConexionCalendario obtenerConexionActiva(UUID usuarioId) {
		return conexionRepository.findByUsuarioIdAndProveedor(usuarioId, properties.proveedor())
				.filter(ConexionCalendario::isActivo)
				.orElseThrow(() -> new BusinessRuleException(
						"Tu calendario no esta vinculado. Autoriza Google Calendar para sincronizar las fechas limite"));
	}

	private void exigirHabilitado() {
		if (!properties.listoParaSincronizar()) {
			throw new BusinessRuleException(
					"La integracion con Google Calendar no esta habilitada en este entorno");
		}
	}

	private String tituloDe(Parcial parcial) {
		return "Fecha limite de " + parcial.getNombre();
	}

	private String descripcionDe(Parcial parcial) {
		Periodo periodo = parcial.getPeriodo();
		StringBuilder texto = new StringBuilder("Fecha limite del parcial ");
		texto.append(parcial.getNombre());
		if (periodo != null && periodo.getNombre() != null) {
			texto.append(" del periodo ").append(periodo.getNombre());
		}
		texto.append(". Recordatorio generado por Proyecto Global.");
		return texto.toString();
	}
}
