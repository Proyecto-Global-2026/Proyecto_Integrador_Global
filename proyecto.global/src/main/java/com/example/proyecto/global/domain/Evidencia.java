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
@Table(name = "evidencias")
public class Evidencia {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "curso", nullable = false, length = 200)
	private String curso;

	@Column(name = "institucion", nullable = false, length = 200)
	private String institucion;

	@Column(name = "fecha", nullable = false)
	private LocalDate fecha;

	@Column(name = "horas_acreditadas", nullable = false)
	private Integer horasAcreditadas;

	@Enumerated(EnumType.STRING)
	@Column(name = "estado", nullable = false, length = 20)
	private EstadoEvidencia estado = EstadoEvidencia.PENDIENTE;

	@Column(name = "nombre_original", length = 255)
	private String nombreOriginal;

	@Column(name = "nombre_almacenado", length = 255)
	private String nombreAlmacenado;

	@Column(name = "content_type", length = 100)
	private String contentType;

	@Column(name = "tamano")
	private Long tamano;

	@Column(name = "ruta", length = 500)
	private String ruta;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected Evidencia() {
	}

	public Evidencia(Usuario usuario, String curso, String institucion, LocalDate fecha, Integer horasAcreditadas) {
		this.usuario = usuario;
		this.curso = curso;
		this.institucion = institucion;
		this.fecha = fecha;
		this.horasAcreditadas = horasAcreditadas;
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

	public void actualizar(String curso, String institucion, LocalDate fecha, Integer horasAcreditadas) {
		this.curso = curso;
		this.institucion = institucion;
		this.fecha = fecha;
		this.horasAcreditadas = horasAcreditadas;
	}

	/**
	 * El docente que corrige una evidencia rechazada vuelve a PENDIENTE para
	 * una nueva revision.
	 */
	public void reenviarRevision() {
		if (this.estado == EstadoEvidencia.RECHAZADA) {
			this.estado = EstadoEvidencia.PENDIENTE;
		}
	}

	public void cambiarEstado(EstadoEvidencia estado) {
		this.estado = estado;
	}

	public void adjuntarArchivo(String nombreOriginal, String nombreAlmacenado, String contentType, long tamano,
			String ruta) {
		this.nombreOriginal = nombreOriginal;
		this.nombreAlmacenado = nombreAlmacenado;
		this.contentType = contentType;
		this.tamano = tamano;
		this.ruta = ruta;
	}

	public void quitarArchivo() {
		this.nombreOriginal = null;
		this.nombreAlmacenado = null;
		this.contentType = null;
		this.tamano = null;
		this.ruta = null;
	}

	public boolean tieneArchivo() {
		return nombreAlmacenado != null;
	}

	public UUID getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public String getCurso() {
		return curso;
	}

	public String getInstitucion() {
		return institucion;
	}

	public LocalDate getFecha() {
		return fecha;
	}

	public Integer getHorasAcreditadas() {
		return horasAcreditadas;
	}

	public EstadoEvidencia getEstado() {
		return estado;
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

	public Long getTamano() {
		return tamano;
	}

	public String getRuta() {
		return ruta;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}
