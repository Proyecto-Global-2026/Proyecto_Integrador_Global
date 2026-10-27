package com.example.proyecto.global.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "solicitudes_arco")
public class SolicitudArco {

	public enum Tipo {
		ACCESO,
		RECTIFICACION,
		CANCELACION,
		OPOSICION
	}

	public enum Estado {
		PENDIENTE,
		EN_PROCESO,
		ATENDIDA,
		RECHAZADA
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(name = "tipo", nullable = false, length = 20)
	private Tipo tipo;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private Estado estado = Estado.PENDIENTE;

	@Column(name = "motivo", length = 500)
	private String motivo;

	@Column(name = "detalles", columnDefinition = "text")
	private String detalles;

	@Column(name = "respuesta", columnDefinition = "text")
	private String respuesta;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "atendido_por_id")
	private Usuario atendidoPor;

	@Column(name = "fecha_solicitud", nullable = false)
	private OffsetDateTime fechaSolicitud;

	@Column(name = "fecha_atencion")
	private OffsetDateTime fechaAtencion;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected SolicitudArco() {
	}

	public SolicitudArco(Usuario usuario, Tipo tipo, String motivo, String detalles) {
		this.usuario = usuario;
		this.tipo = tipo;
		this.motivo = motivo;
		this.detalles = detalles;
	}

	@PrePersist
	void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		if (fechaSolicitud == null) {
			fechaSolicitud = now;
		}
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public void atender(Usuario atendidoPor, String respuesta) {
		this.atendidoPor = atendidoPor;
		this.respuesta = respuesta;
		this.estado = Estado.ATENDIDA;
		this.fechaAtencion = OffsetDateTime.now();
	}

	public void rechazar(Usuario atendidoPor, String respuesta) {
		this.atendidoPor = atendidoPor;
		this.respuesta = respuesta;
		this.estado = Estado.RECHAZADA;
		this.fechaAtencion = OffsetDateTime.now();
	}

	public void enProceso() {
		this.estado = Estado.EN_PROCESO;
	}

	public UUID getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Tipo getTipo() {
		return tipo;
	}

	public Estado getEstado() {
		return estado;
	}

	public String getMotivo() {
		return motivo;
	}

	public String getDetalles() {
		return detalles;
	}

	public String getRespuesta() {
		return respuesta;
	}

	public Usuario getAtendidoPor() {
		return atendidoPor;
	}

	public OffsetDateTime getFechaSolicitud() {
		return fechaSolicitud;
	}

	public OffsetDateTime getFechaAtencion() {
		return fechaAtencion;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
