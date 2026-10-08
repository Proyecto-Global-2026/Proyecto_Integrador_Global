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
@Table(name = "planeaciones")
public class Planeacion {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "docente_id", nullable = false)
	private Usuario docente;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "materia_id", nullable = false)
	private Materia materia;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "parcial_id", nullable = false)
	private Parcial parcial;

	@Column(name = "titulo", nullable = false, length = 200)
	private String titulo;

	@Column(name = "contenido", nullable = false, columnDefinition = "TEXT")
	private String contenido;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoPlaneacion estado = EstadoPlaneacion.PENDIENTE;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected Planeacion() {
	}

	public Planeacion(Usuario docente, Materia materia, Parcial parcial, String titulo, String contenido) {
		this.docente = docente;
		this.materia = materia;
		this.parcial = parcial;
		this.titulo = titulo;
		this.contenido = contenido;
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

	public void actualizar(Materia materia, Parcial parcial, String titulo, String contenido) {
		this.materia = materia;
		this.parcial = parcial;
		this.titulo = titulo;
		this.contenido = contenido;
	}

	public UUID getId() {
		return id;
	}

	public Usuario getDocente() {
		return docente;
	}

	public Materia getMateria() {
		return materia;
	}

	public Parcial getParcial() {
		return parcial;
	}

	public String getTitulo() {
		return titulo;
	}

	public String getContenido() {
		return contenido;
	}

	public EstadoPlaneacion getEstado() {
		return estado;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}