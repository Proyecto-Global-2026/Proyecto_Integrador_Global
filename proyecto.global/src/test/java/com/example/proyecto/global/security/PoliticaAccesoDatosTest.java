package com.example.proyecto.global.security;

import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_ADMIN;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_COORDINADOR;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_DOCENTE;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_DIRECCION;
import static com.example.proyecto.global.security.PoliticaAccesoDatos.ROL_JEFE_DE_CARRERA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class PoliticaAccesoDatosTest {

	private final PoliticaAccesoDatos politica = new PoliticaAccesoDatos();
	private final UUID docenteA = UUID.randomUUID();
	private final UUID docenteB = UUID.randomUUID();

	@Test
	void docenteSoloPuedeConsultarSusPropiosDatos() {
		assertTrue(politica.puedeConsultarDatosDe(ROL_DOCENTE, docenteA, docenteA));
		assertFalse(politica.puedeConsultarDatosDe(ROL_DOCENTE, docenteB, docenteA));
		assertFalse(politica.puedeConsultarDatosDeTerceros(ROL_DOCENTE));
	}

	@Test
	void coordinadorYDireccionPuedenConsultarDatosDeTerceros() {
		assertTrue(politica.puedeConsultarDatosDe(ROL_COORDINADOR, docenteB, docenteA));
		assertTrue(politica.puedeConsultarDatosDe(ROL_DIRECCION, docenteB, docenteA));
		assertTrue(politica.puedeConsultarDatosDeTerceros(ROL_COORDINADOR));
		assertTrue(politica.puedeConsultarDatosDeTerceros(ROL_DIRECCION));
	}

	@Test
	void adminNoPuedeConsultarDatosPersonales() {
		assertFalse(politica.puedeConsultarDatosDe(ROL_ADMIN, docenteB, docenteA));
		assertFalse(politica.puedeConsultarDatosDeTerceros(ROL_ADMIN));
	}

	@Test
	void soloElPropietarioPuedeModificarSusDatos() {
		assertTrue(politica.puedeModificarDatosDe(docenteA, docenteA));
		assertFalse(politica.puedeModificarDatosDe(docenteB, docenteA));
		assertFalse(politica.puedeModificarDatosDe(null, docenteA));
	}

	@Test
	void soloDireccionGestionaUsuarios() {
		assertTrue(politica.puedeGestionarUsuarios(ROL_DIRECCION));
		assertFalse(politica.puedeGestionarUsuarios(ROL_COORDINADOR));
		assertFalse(politica.puedeGestionarUsuarios(ROL_ADMIN));
		assertFalse(politica.puedeGestionarUsuarios(ROL_DOCENTE));
	}

	@Test
	void coordinacionYDireccionPuedenConsultarUsuarios() {
		assertTrue(politica.puedeConsultarUsuarios(ROL_COORDINADOR));
		assertTrue(politica.puedeConsultarUsuarios(ROL_DIRECCION));
		assertFalse(politica.puedeConsultarUsuarios(ROL_DOCENTE));
		assertFalse(politica.puedeConsultarUsuarios(ROL_ADMIN));
	}

	@Test
	void reportesSoloParaCoordinacionYDireccion() {
		assertTrue(politica.puedeConsultarReportes(ROL_COORDINADOR));
		assertTrue(politica.puedeConsultarReportes(ROL_DIRECCION));
		assertFalse(politica.puedeConsultarReportes(ROL_DOCENTE));
		assertFalse(politica.puedeConsultarReportes(ROL_ADMIN));
	}

	@Test
	void soloCoordinadorRevisaPlaneacionesYEvidencias() {
		assertTrue(politica.puedeRevisar(ROL_COORDINADOR));
		assertFalse(politica.puedeRevisar(ROL_DIRECCION));
		assertFalse(politica.puedeRevisar(ROL_ADMIN));
		assertFalse(politica.puedeRevisar(ROL_DOCENTE));
	}

	@Test
	void soloAdminAdministraElSistema() {
		assertTrue(politica.puedeAdministrarSistema(ROL_ADMIN));
		assertFalse(politica.puedeAdministrarSistema(ROL_DIRECCION));
		assertFalse(politica.puedeAdministrarSistema(ROL_COORDINADOR));
		assertFalse(politica.puedeAdministrarSistema(ROL_DOCENTE));
	}

	@Test
	void calendarioRespetaLosRolesPermitidos() {
		assertTrue(politica.puedeUsarCalendario(ROL_DOCENTE));
		assertTrue(politica.puedeUsarCalendario(ROL_COORDINADOR));
		assertTrue(politica.puedeUsarCalendario(ROL_JEFE_DE_CARRERA));
		assertTrue(politica.puedeUsarCalendario(ROL_ADMIN));
		assertFalse(politica.puedeUsarCalendario(ROL_DIRECCION));
	}

	@Test
	void rolesNulosVaciosODesconocidosNoTienenAcceso() {
		assertFalse(politica.puedeConsultarUsuarios(null));
		assertFalse(politica.puedeConsultarUsuarios(""));
		assertFalse(politica.puedeConsultarUsuarios("   "));
		assertFalse(politica.puedeConsultarUsuarios("SUPERUSER"));
		assertFalse(politica.puedeAdministrarSistema("superuser"));
	}

	@Test
	void normalizaElRolRecibido() {
		assertTrue(politica.puedeConsultarUsuarios(" coordinador "));
		assertTrue(politica.puedeConsultarUsuarios("Coordinador"));
		assertTrue(politica.puedeAdministrarSistema(" admin "));
	}

	@Test
	void propietarioConIdNuloNoCoincide() {
		assertFalse(politica.esPropietario(null, docenteA));
		assertFalse(politica.esPropietario(docenteA, null));
		assertFalse(politica.esPropietario(null, null));
	}

	@Test
	void laMatrizDocumentaTodosLosDatosPersonales() {
		List<PoliticaAccesoDatos.ReglaAcceso> reglas = politica.matriz();

		assertFalse(reglas.isEmpty());
		assertTrue(reglas.stream().anyMatch(regla -> regla.ruta().contains("/api/usuarios")));
		assertTrue(reglas.stream().anyMatch(regla -> regla.ruta().contains("/api/reportes")));
		assertTrue(reglas.stream().anyMatch(regla -> regla.ruta().contains("/api/seguridad")));
		assertTrue(reglas.stream().allMatch(regla -> !regla.roles().isEmpty()));
		assertTrue(reglas.stream().allMatch(regla -> !regla.descripcion().isBlank()));
	}
}
