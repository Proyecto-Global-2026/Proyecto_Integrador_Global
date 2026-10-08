package com.example.proyecto.global.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

@Entity
@Table(name = "archivos_planeacion")
public class ArchivoPlaneacion {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "planeacion_id", nullable = false)
	private Planeacion planeacion;

	@Column(name = "nombre_original", nullable = false, length = 255)
	private String nombreOriginal;

	@Column(name = "nombre_almacenado", nullable = false, unique = true, length = 255)
	private String nombreAlmacenado;

	@Column(name = "content_type", nullable = false, length = 100)
	private String contentType;

	@Column(name = "tamano", nullable = false)
	private long tamano;

	@Column(name = "ruta", nullable = false, length = 500)
	private String ruta;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	protected ArchivoPlaneacion() {
	}

	public ArchivoPlaneacion(Planeacion planeacion, String nombreOriginal, String nombreAlmacenado,
			String contentType, long tamano, String ruta) {
		this.planeacion = planeacion;
		this.nombreOriginal = nombreOriginal;
		this.nombreAlmacenado = nombreAlmacenado;
		this.contentType = contentType;
		this.tamano = tamano;
		this.ruta = ruta;
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

	public String getNombreOriginal() {
		return nombreOriginal;
	}

	public String getNombreAlmacenado() {
		return nombreAlmacenado;
	}

	public String getContentType() {
		return contentType;
	}

	public long getTamano() {
		return tamano;
	}

	public String getRuta() {
		return ruta;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}
}