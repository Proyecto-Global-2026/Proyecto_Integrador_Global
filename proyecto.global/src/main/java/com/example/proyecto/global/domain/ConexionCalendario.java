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
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "conexiones_calendario")
public class ConexionCalendario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "usuario_id", nullable = false)
	private Usuario usuario;

	@Column(name = "proveedor", nullable = false, length = 30)
	private String proveedor = "google";

	@Column(name = "correo_calendario", length = 180)
	private String correoCalendario;

	@Column(name = "access_token", columnDefinition = "text")
	private String accessToken;

	@Column(name = "refresh_token", columnDefinition = "text")
	private String refreshToken;

	@Column(name = "expira_en")
	private OffsetDateTime expiraEn;

	@Column(name = "alcance", length = 300)
	private String alcance;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	protected ConexionCalendario() {
	}

	public ConexionCalendario(Usuario usuario, String proveedor) {
		this.usuario = usuario;
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

	public void actualizarCredenciales(String accessToken, String refreshToken, OffsetDateTime expiraEn,
			String alcance, String correoCalendario) {
		this.accessToken = accessToken;
		if (refreshToken != null && !refreshToken.isBlank()) {
			this.refreshToken = refreshToken;
		}
		this.expiraEn = expiraEn;
		if (alcance != null && !alcance.isBlank()) {
			this.alcance = alcance;
		}
		if (correoCalendario != null && !correoCalendario.isBlank()) {
			this.correoCalendario = correoCalendario;
		}
		this.activo = true;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public boolean tokenVigente() {
		return accessToken != null && !accessToken.isBlank()
				&& expiraEn != null
				&& expiraEn.isAfter(OffsetDateTime.now().plusMinutes(1));
	}

	public boolean puedeRenovar() {
		return refreshToken != null && !refreshToken.isBlank();
	}

	public UUID getId() {
		return id;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public String getProveedor() {
		return proveedor;
	}

	public String getCorreoCalendario() {
		return correoCalendario;
	}

	public String getAccessToken() {
		return accessToken;
	}

	public String getRefreshToken() {
		return refreshToken;
	}

	public OffsetDateTime getExpiraEn() {
		return expiraEn;
	}

	public String getAlcance() {
		return alcance;
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
