package com.example.proyecto.global.domain;

import java.time.LocalDate;
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
@Table(name = "eventos_calendario")
public class EventoCalendario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "parcial_id", nullable = false)
	private Parcial parcial;

	@Column(name = "proveedor", nullable = false, length = 30)
	private String proveedor = "google";

	@Column(name = "evento_id", length = 300)
	private String eventoId;

	@Column(name = "titulo", nullable = false, length = 250)
	private String titulo;

	@Column(name = "fecha_limite", nullable = false)
	private LocalDate fechaLimite;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoEventoCalendario estado = EstadoEventoCalendario.PENDIENTE;

	@Column(name = "mensaje_error", length = 500)
	private String mensajeError;

	@Column(name = "sincronizado_en")
	private OffsetDateTime sincronizadoEn;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected EventoCalendario() {
	}

	public EventoCalendario(Usuario usuario, Parcial parcial, String titulo, LocalDate fechaLimite,
			String proveedor) {
		this.usuario = usuario;
		this.parcial = parcial;
		this.titulo = titulo;
		this.fechaLimite = fechaLimite;
		this.proveedor = proveedor == null ? "google" : proveedor;
	}

	@PrePersist
	void onCreate() {
		OffsetDateTime now = OffsetDateTime.now();
		this.createdAt = now;
		this.updatedAt = now;
	}

	@PreUpdate
	void onUpdate() {
		this.updatedAt = OffsetDateTime.now();
	}

	public void marcarSincronizado(String eventoId, LocalDate fechaLimite, String titulo) {
		this.eventoId = eventoId;
		this.fechaLimite = fechaLimite;
		this.titulo = titulo;
		this.estado = EstadoEventoCalendario.SINCRONIZADO;
		this.mensajeError = null;
		this.sincronizadoEn = OffsetDateTime.now();
	}

	public void marcarError(String mensaje) {
		this.estado = EstadoEventoCalendario.ERROR;
		this.mensajeError = mensaje == null ? null
				: mensaje.substring(0, Math.min(mensaje.length(), 500));
	}

	public UUID getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Parcial getParcial() {
		return parcial;
	}

	public void setParcial(Parcial parcial) {
		this.parcial = parcial;
	}

	public String getProveedor() {
		return proveedor;
	}

	public String getEventoId() {
		return eventoId;
	}

	public String getTitulo() {
		return titulo;
	}

	public LocalDate getFechaLimite() {
		return fechaLimite;
	}

	public EstadoEventoCalendario getEstado() {
		return estado;
	}

	public String getMensajeError() {
		return mensajeError;
	}

	public OffsetDateTime getSincronizadoEn() {
		return sincronizadoEn;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
