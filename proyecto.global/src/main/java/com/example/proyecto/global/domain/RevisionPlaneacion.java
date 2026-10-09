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
import jakarta.persistence.Table;

@Entity
@Table(name = "revisiones_planeacion")
public class RevisionPlaneacion {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "planeacion_id", nullable = false)
	private Planeacion planeacion;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_anterior", nullable = false, length = 20)
	private EstadoPlaneacion estadoAnterior;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado_nuevo", nullable = false, length = 20)
	private EstadoPlaneacion estadoNuevo;

	@Column(name = "comentario", length = 1000)
	private String comentario;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "revisado_por", nullable = false)
	private Usuario revisadoPor;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected RevisionPlaneacion() {
	}

	public RevisionPlaneacion(Planeacion planeacion, EstadoPlaneacion estadoAnterior, EstadoPlaneacion estadoNuevo,
			String comentario, Usuario revisadoPor) {
		this.planeacion = planeacion;
		this.estadoAnterior = estadoAnterior;
		this.estadoNuevo = estadoNuevo;
		this.comentario = comentario;
		this.revisadoPor = revisadoPor;
	}

	@PrePersist
	void onCreate() {
		this.createdAt = OffsetDateTime.now();
	}

	public UUID getId() {
		return id;
	}

	public Planeacion getPlaneacion() {
		return planeacion;
	}

	public EstadoPlaneacion getEstadoAnterior() {
		return estadoAnterior;
	}

	public EstadoPlaneacion getEstadoNuevo() {
		return estadoNuevo;
	}

	public String getComentario() {
		return comentario;
	}

	public Usuario getRevisadoPor() {
		return revisadoPor;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}