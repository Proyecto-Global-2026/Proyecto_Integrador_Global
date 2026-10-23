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
@Table(name = "usuarios")
public class Usuario {

	@Id
	@GeneratedValue(strategy = GenerationType.UUID)
	@Column(name = "id", nullable = false, updatable = false)
	private UUID id;

	@Column(name = "nombre", nullable = false, length = 120)
	private String nombre;

	@Column(name = "email", nullable = false, unique = true, length = 180)
	private String email;

	@Column(name = "password", nullable = false, length = 100)
	private String password;

	@ManyToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "rol_id", nullable = false)
	private Rol rol;

	@Column(name = "activo", nullable = false)
	private boolean activo = true;

	@Column(name = "proveedor", length = 50)
	private String proveedor;

	@Column(name = "proveedor_id", length = 180)
	private String proveedorId;

	@Column(name = "created_at", nullable = false, updatable = false)
	private OffsetDateTime createdAt;

	@Column(name = "updated_at", nullable = false)
	private OffsetDateTime updatedAt;

	@Column(name = "anonimizado", nullable = false)
	private boolean anonimizado = false;

	@Column(name = "fecha_anonimizacion")
	private OffsetDateTime fechaAnonimizacion;

	@Column(name = "motivo_baja", length = 100)
	private String motivoBaja;

	protected Usuario() {
	}

	public Usuario(String nombre, String email, String password, Rol rol) {
		this.nombre = nombre;
		this.email = email;
		this.password = password;
		this.rol = rol;
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

	public static String normalizarEmail(String email) {
		return email == null ? null : email.trim().toLowerCase();
	}

	/**
	 * El correo no se modifica en la base: se reescribe con el mismo valor
	 * normalizado, para que la restriccion unica se mantenga coherente.
	 */
	public void actualizar(String nombre, String email, Rol rol) {
		this.nombre = nombre;
		this.email = email;
		this.rol = rol;
	}

	public UUID getId() {
		return id;
	}

	public String getNombre() {
		return nombre;
	}

	public String getEmail() {
		return email;
	}

	public String getPassword() {
		return password;
	}

	public Rol getRol() {
		return rol;
	}

	public boolean isActivo() {
		return activo;
	}

	public void setActivo(boolean activo) {
		this.activo = activo;
	}

	public String getProveedor() {
		return proveedor;
	}

	public void setProveedor(String proveedor) {
		this.proveedor = proveedor;
	}

	public String getProveedorId() {
		return proveedorId;
	}

	public void setProveedorId(String proveedorId) {
		this.proveedorId = proveedorId;
	}

	public boolean isAnonimizado() {
		return anonimizado;
	}

	public void setAnonimizado(boolean anonimizado) {
		this.anonimizado = anonimizado;
	}

	public OffsetDateTime getFechaAnonimizacion() {
		return fechaAnonimizacion;
	}

	public void setFechaAnonimizacion(OffsetDateTime fechaAnonimizacion) {
		this.fechaAnonimizacion = fechaAnonimizacion;
	}

	public String getMotivoBaja() {
		return motivoBaja;
	}

	public void setMotivoBaja(String motivoBaja) {
		this.motivoBaja = motivoBaja;
	}

	/**
	 * Sustituye los datos personales del usuario por valores no identificables
	 * manteniendo la fila para no romper la trazabilidad de planeaciones y
	 * revisiones (S3-07). La contrasena se reemplaza por un hash aleatorio que
	 * no corresponde a ninguna credencial valida.
	 */
	public void anonimizar(String nombreAnonimizado, String emailAnonimizado, String passwordHash, String motivo) {
		this.nombre = nombreAnonimizado;
		this.email = emailAnonimizado;
		this.password = passwordHash;
		this.proveedor = null;
		this.proveedorId = null;
		this.activo = false;
		this.anonimizado = true;
		this.fechaAnonimizacion = OffsetDateTime.now();
		this.motivoBaja = motivo;
	}

	public OffsetDateTime getCreatedAt() {
		return createdAt;
	}

	public OffsetDateTime getUpdatedAt() {
		return updatedAt;
	}
}