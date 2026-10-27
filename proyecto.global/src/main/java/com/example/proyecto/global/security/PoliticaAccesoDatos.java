package com.example.proyecto.global.security;

import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import org.springframework.stereotype.Component;

/**
 * Politica central de control de acceso (RBAC) a datos personales (S3-09).
 *
 * <p>
 * Concentra las reglas que antes estaban duplicadas en los servicios. La regla
 * base es: un usuario solo accede a sus propios datos personales; los roles de
 * coordinacion y direccion pueden consultar los de terceros para revisar
 * planeaciones y evidencias, pero no modificarlos. ADMIN administra la
 * configuracion de seguridad y privacidad, no los datos personales.
 * </p>
 */
@Component
public class PoliticaAccesoDatos {

	public static final String ROL_DOCENTE = "DOCENTE";
	public static final String ROL_COORDINADOR = "COORDINADOR";
	public static final String ROL_DIRECCION = "DIRECCION";
	public static final String ROL_ADMIN = "ADMIN";
	public static final String ROL_JEFE_DE_CARRERA = "JEFE_DE_CARRERA";

	private static final Set<String> ROLES_CONSULTA_USUARIOS = Set.of(ROL_COORDINADOR, ROL_DIRECCION);
	private static final Set<String> ROLES_GESTION_USUARIOS = Set.of(ROL_DIRECCION);
	private static final Set<String> ROLES_CONSULTA_DATOS_TERCEROS = Set.of(ROL_COORDINADOR, ROL_DIRECCION);
	private static final Set<String> ROLES_REVISION = Set.of(ROL_COORDINADOR);
	private static final Set<String> ROLES_REPORTES = Set.of(ROL_COORDINADOR, ROL_DIRECCION);
	private static final Set<String> ROLES_ADMINISTRACION = Set.of(ROL_ADMIN);
	private static final Set<String> ROLES_CALENDARIO = Set.of(ROL_DOCENTE, ROL_COORDINADOR,
			ROL_JEFE_DE_CARRERA, ROL_ADMIN);

	public boolean puedeConsultarUsuarios(String rol) {
		return contiene(ROLES_CONSULTA_USUARIOS, rol);
	}

	public boolean puedeGestionarUsuarios(String rol) {
		return contiene(ROLES_GESTION_USUARIOS, rol);
	}

	public boolean puedeConsultarReportes(String rol) {
		return contiene(ROLES_REPORTES, rol);
	}

	public boolean puedeRevisar(String rol) {
		return contiene(ROLES_REVISION, rol);
	}

	public boolean puedeAdministrarSistema(String rol) {
		return contiene(ROLES_ADMINISTRACION, rol);
	}

	public boolean puedeUsarCalendario(String rol) {
		return contiene(ROLES_CALENDARIO, rol);
	}

	/**
	 * Indica si el rol puede consultar datos personales de terceros (no solo los
	 * propios). Es la regla que usan los listados para decidir si respetan el
	 * filtro por docente.
	 */
	public boolean puedeConsultarDatosDeTerceros(String rol) {
		return contiene(ROLES_CONSULTA_DATOS_TERCEROS, rol);
	}

	public boolean esPropietario(UUID idSolicitante, UUID idPropietario) {
		return idSolicitante != null && idSolicitante.equals(idPropietario);
	}

	/**
	 * Acceso de lectura a un dato personal: el propietario o un rol de revision.
	 */
	public boolean puedeConsultarDatosDe(String rolSolicitante, UUID idSolicitante, UUID idPropietario) {
		return esPropietario(idSolicitante, idPropietario) || puedeConsultarDatosDeTerceros(rolSolicitante);
	}

	/**
	 * Acceso de escritura a un dato personal: solo el propietario.
	 */
	public boolean puedeModificarDatosDe(UUID idSolicitante, UUID idPropietario) {
		return esPropietario(idSolicitante, idPropietario);
	}

	/**
	 * Matriz declarativa de la politica, expuesta para auditoria y pruebas.
	 */
	public List<ReglaAcceso> matriz() {
		return List.of(
				new ReglaAcceso("PUBLICO", "/api/auth/registro, /api/auth/login", List.of("PUBLICO"),
						"Registro y autenticacion; no exponen datos de terceros"),
				new ReglaAcceso("GET", "/api/auth/me", List.of("AUTENTICADO"),
						"Devuelve solo el perfil del usuario autenticado"),
				new ReglaAcceso("GET", "/api/usuarios, /api/usuarios/{id}, /api/roles",
						List.of(ROL_COORDINADOR, ROL_DIRECCION), "Consulta de datos personales de usuarios"),
				new ReglaAcceso("POST/PUT/DELETE",
						"/api/usuarios, /api/usuarios/{id}, /api/usuarios/{id}/reactivar",
						List.of(ROL_DIRECCION), "Alta, edicion, baja y reactivacion de usuarios"),
				new ReglaAcceso("GET", "/api/reportes/**", List.of(ROL_COORDINADOR, ROL_DIRECCION),
						"Reportes institucionales con datos agregados"),
				new ReglaAcceso("POST", "/api/planeaciones/{id}/revisiones, /api/evidencias/{id}/revisiones",
						List.of(ROL_COORDINADOR), "Revision de planeaciones y evidencias"),
				new ReglaAcceso("GET",
						"/api/planeaciones/**, /api/evidencias/**, /api/planeaciones/{id}/avances/**, "
								+ "/api/planeaciones/{id}/archivos/**",
						List.of(ROL_DOCENTE + " (propietario)", ROL_COORDINADOR, ROL_DIRECCION),
						"El docente solo ve sus datos; coordinacion y direccion pueden consultar"),
				new ReglaAcceso("POST/PUT/DELETE",
						"/api/planeaciones/**, /api/evidencias/**, /api/planeaciones/{id}/avances/**, "
								+ "/api/planeaciones/{id}/archivos/**",
						List.of(ROL_DOCENTE + " (propietario)"),
						"Solo el docente propietario modifica sus datos"),
				new ReglaAcceso("GET", "/api/calendario/**",
						List.of(ROL_DOCENTE, ROL_COORDINADOR, ROL_JEFE_DE_CARRERA, ROL_ADMIN),
						"Calendario personal del usuario autenticado"),
				new ReglaAcceso("GET", "/api/periodos/**, /api/materias/**, /api/parciales/**",
						List.of(ROL_DOCENTE, ROL_COORDINADOR, ROL_DIRECCION),
						"Catalogos academicos; no contienen datos personales"),
				new ReglaAcceso("POST/PUT/DELETE", "/api/periodos/**, /api/materias/**, /api/parciales/**",
						List.of(ROL_COORDINADOR, ROL_DIRECCION),
						"Gestion de catalogos academicos"),
				new ReglaAcceso("GET/POST", "/api/retencion/**, /api/cifrado/**, /api/seguridad/**",
						List.of(ROL_ADMIN), "Administracion de seguridad, cifrado y retencion"));
	}

	private boolean contiene(Set<String> roles, String rol) {
		return roles.contains(normalizar(rol));
	}

	private String normalizar(String rol) {
		return rol == null ? "" : rol.trim().toUpperCase(Locale.ROOT);
	}

	public record ReglaAcceso(String metodo, String ruta, List<String> roles, String descripcion) {
	}
}
