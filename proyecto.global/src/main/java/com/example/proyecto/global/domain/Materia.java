package com.example.proyecto.global.domain;

import java.time.OffsetDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "materias")
public class Materia {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "nombre", nullable = false, length = 150)
	private String nombre;

	@Column(name = "codigo", nullable = false, unique = true, length = 50)
	private String codigo;

	@Column(name = "descripcion", length = 255)
	private String descripcion;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected Materia() {
	}

	public Materia(String nombre, String codigo, String descripcion) {
		this.nombre = nombre;
		this.codigo = codigo;
		this.descripcion = descripcion;
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

	public static String normalizarCodigo(String codigo) {
		return codigo == null ? null : codigo.trim().toUpperCase();
	}

	public void actualizar(String nombre, String codigo, String descripcion, boolean activo) {
		this.nombre = nombre;
		this.codigo = codigo;
		this.descripcion = descripcion;
		this.activo = activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public UUID getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getCodigo() {
		return codigo;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public boolean isActivo() {
		return activo;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}