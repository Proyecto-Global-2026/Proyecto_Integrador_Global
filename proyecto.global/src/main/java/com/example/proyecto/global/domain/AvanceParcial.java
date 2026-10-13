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
@Table(name = "avances_parcial")
public class AvanceParcial {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "planeacion_id", nullable = false)
	private Planeacion planeacion;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "parcial_id", nullable = false)
	private Parcial parcial;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoAvance estado;

	@Column(name = "observaciones", length = 1000)
	private String observaciones;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "registrado_por", nullable = false)
	private Usuario registradoPor;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected AvanceParcial() {
	}

	public AvanceParcial(Planeacion planeacion, Parcial parcial, EstadoAvance estado, String observaciones,
			Usuario registradoPor) {
		this.planeacion = planeacion;
		this.parcial = parcial;
		this.estado = estado;
		this.observaciones = observaciones;
		this.registradoPor = registradoPor;
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

	public void actualizar(EstadoAvance estado, String observaciones) {
		this.estado = estado;
		this.observaciones = observaciones;
	}

	public UUID getId() {
		return id;
	}

	public Planeacion getPlaneacion() {
		return planeacion;
	}

	public Parcial getParcial() {
		return parcial;
	}

	public EstadoAvance getEstado() {
		return estado;
	}

	public String getObservaciones() {
		return observaciones;
	}

	public Usuario getRegistradoPor() {
		return registradoPor;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
