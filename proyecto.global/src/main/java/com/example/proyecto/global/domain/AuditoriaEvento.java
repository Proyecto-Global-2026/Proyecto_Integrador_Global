package com.example.proyecto.global.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "auditoria_eventos")
public class AuditoriaEvento {

	public enum Accion {
		ACCESO,
		CREACION,
		LECTURA,
		ACTUALIZACION,
		ELIMINACION,
		ANONIMIZACION,
		RECTIFICACION,
		CANCELACION,
		OPOSICION,
		LOGIN,
		LOGOUT
	}

	public enum Resultado {
		EXITOSO,
		FALLIDO,
		DENEGADO
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "usuario_id")
	private UUID usuarioId;

	@Column(name = "usuario_email_mask")
	private String usuarioEmailMask;

	@Column(name = "rol")
	private String rol;

	@Column(name = "ip")
	private String ip;

	@Column(name = "user_agent", length = 500)
	private String userAgent;

	@Column(name = "recurso", length = 150)
	private String recurso;

	@Column(name = "recurso_id")
	private UUID recursoId;

	@Enumerated(EnumType.STRING)
	@Column(name = "accion", nullable = false, length = 30)
	private Accion accion;

	@Enumerated(EnumType.STRING)
	@Column(name = "resultado", nullable = false, length = 20)
	private Resultado resultado = Resultado.EXITOSO;

	@Column(name = "descripcion", length = 255)
	private String descripcion;

	@Column(name = "metodo", length = 10)
	private String metodo;

	@Column(name = "ruta", length = 255)
	private String ruta;

	@Column(name = "fecha_evento", nullable = false)
	private OffsetDateTime fechaEvento;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	public AuditoriaEvento() {
	}

	@PrePersist
	void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		if (fechaEvento == null) {
			fechaEvento = now;
		}
		this.createdAt = now;
	}

	public UUID getId() {
		return id;
	}

	public UUID getUsuarioId() {
		return usuarioId;
	}

	public void setUsuarioId(UUID usuarioId) {
		this.usuarioId = usuarioId;
	}

	public String getUsuarioEmailMask() {
		return usuarioEmailMask;
	}

	public void setUsuarioEmailMask(String usuarioEmailMask) {
		this.usuarioEmailMask = usuarioEmailMask;
	}

	public String getRol() {
		return rol;
	}

	public void setRol(String rol) {
		this.rol = rol;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public void setUserAgent(String userAgent) {
		this.userAgent = userAgent;
	}

	public String getRecurso() {
		return recurso;
	}

	public void setRecurso(String recurso) {
		this.recurso = recurso;
	}

	public UUID getRecursoId() {
		return recursoId;
	}

	public void setRecursoId(UUID recursoId) {
		this.recursoId = recursoId;
	}

	public Accion getAccion() {
		return accion;
	}

	public void setAccion(Accion accion) {
		this.accion = accion;
	}

	public Resultado getResultado() {
		return resultado;
	}

	public void setResultado(Resultado resultado) {
		this.resultado = resultado;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public String getMetodo() {
		return metodo;
	}

	public void setMetodo(String metodo) {
		this.metodo = metodo;
	}

	public String getRuta() {
		return ruta;
	}

	public void setRuta(String ruta) {
		this.ruta = ruta;
	}

	public OffsetDateTime getFechaEvento() {
		return fechaEvento;
	}

	public void setFechaEvento(OffsetDateTime fechaEvento) {
		this.fechaEvento = fechaEvento;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}
