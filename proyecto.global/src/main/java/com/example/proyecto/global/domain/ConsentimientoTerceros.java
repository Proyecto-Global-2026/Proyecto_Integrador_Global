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
@Table(name = "consentimiento_terceros")
public class ConsentimientoTerceros {

	public enum Tercero {
		GOOGLE_CALENDAR
	}

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Enumerated(EnumType.STRING)
	@Column(name = "tercero", nullable = false, length = 50)
	private Tercero tercero;

	@Column(name = "consentimiento_otorgado", nullable = false)
	private boolean consentimientoOtorgado = false;

	@Column(name = "version_aviso", length = 20)
	private String versionAviso;

	@Column(name = "ip_otorgamiento", columnDefinition = "inet")
	private String ipOtorgamiento;

	@Column(name = "user_agent", length = 500)
	private String userAgent;

	@Column(name = "fecha_otorgamiento")
	private OffsetDateTime fechaOtorgamiento;

	@Column(name = "fecha_revocacion")
	private OffsetDateTime fechaRevocacion;

	@Column(name = "motivo_revocacion", length = 255)
	private String motivoRevocacion;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected ConsentimientoTerceros() {
	}

	public ConsentimientoTerceros(Usuario usuario, Tercero tercero) {
		this.usuario = usuario;
		this.tercero = tercero;
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

	public void otorgar(String versionAviso, String ipOtorgamiento, String userAgent) {
		this.consentimientoOtorgado = true;
		this.versionAviso = versionAviso;
		this.ipOtorgamiento = ipOtorgamiento;
		this.userAgent = userAgent;
		this.fechaOtorgamiento = OffsetDateTime.now();
		this.fechaRevocacion = null;
		this.motivoRevocacion = null;
	}

	public void revocar(String motivo) {
		this.consentimientoOtorgado = false;
		this.fechaRevocacion = OffsetDateTime.now();
		this.motivoRevocacion = motivo;
	}

	public UUID getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public Tercero getTercero() {
		return tercero;
	}

	public boolean isConsentimientoOtorgado() {
		return consentimientoOtorgado;
	}

	public String getVersionAviso() {
		return versionAviso;
	}

	public String getIpOtorgamiento() {
		return ipOtorgamiento;
	}

	public String getUserAgent() {
		return userAgent;
	}

	public OffsetDateTime getFechaOtorgamiento() {
		return fechaOtorgamiento;
	}

	public OffsetDateTime getFechaRevocacion() {
		return fechaRevocacion;
	}

	public String getMotivoRevocacion() {
		return motivoRevocacion;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}