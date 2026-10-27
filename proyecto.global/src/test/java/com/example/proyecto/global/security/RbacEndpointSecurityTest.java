package com.example.proyecto.global.security;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.proyecto.global.config.CifradoProperties;
import com.example.proyecto.global.config.RetencionProperties;
import com.example.proyecto.global.config.SecurityConfig;
import com.example.proyecto.global.controller.AuthController;
import com.example.proyecto.global.controller.CifradoDatosController;
import com.example.proyecto.global.controller.ReporteController;
import com.example.proyecto.global.controller.RetencionDatosController;
import com.example.proyecto.global.controller.SeguridadController;
import com.example.proyecto.global.controller.UsuarioController;
import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.dto.ReportePlaneacionesResponse;
import com.example.proyecto.global.dto.UsuarioCreateRequest;
import com.example.proyecto.global.dto.UsuarioResponse;
import com.example.proyecto.global.service.AuthService;
import com.example.proyecto.global.service.ReporteExportService;
import com.example.proyecto.global.service.ReporteService;
import com.example.proyecto.global.service.UsuarioService;
import com.example.proyecto.global.service.retencion.RetencionDatosService;

/**
 * Verifica el control de acceso (RBAC) a nivel de endpoint (S3-09).
 *
 * <p>
 * Cubre las dos barreras: la regla de URL de {@link SecurityConfig} y el
 * {@code @PreAuthorize} de los controladores. Usa {@code @WithMockUser} para
 * simular cada rol sin depender de la base de datos.
 * </p>
 */
@WebMvcTest(controllers = {
		UsuarioController.class,
		AuthController.class,
		ReporteController.class,
		SeguridadController.class,
		CifradoDatosController.class,
		RetencionDatosController.class
})
@AutoConfigureMockMvc
@EnableConfigurationProperties({ CifradoProperties.class, RetencionProperties.class })
@Import({ SecurityConfig.class, SecurityErrorHandlers.class, PoliticaAccesoDatos.class })
class RbacEndpointSecurityTest {

	@Autowired
	private MockMvc mvc;

	@MockitoBean
	private UsuarioService usuarioService;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private ReporteService reporteService;

	@MockitoBean
	private ReporteExportService reporteExportService;

	@MockitoBean
	private RetencionDatosService retencionDatosService;

	@MockitoBean
	private JwtService jwtService;

	@MockitoBean
	private UsuarioDetailsService usuarioDetailsService;

	@Test
	void sinTokenDevuelve401() throws Exception {
		mvc.perform(get("/api/usuarios")).andExpect(status().isUnauthorized());
	}

	@Test
	@WithMockUser(roles = "DOCENTE")
	void docenteNoPuedeListarUsuarios() throws Exception {
		mvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "COORDINADOR")
	void coordinadorPuedeListarUsuarios() throws Exception {
		when(usuarioService.listar(isNull(), isNull(), any(Pageable.class))).thenReturn(Page.empty());

		mvc.perform(get("/api/usuarios")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "DIRECCION")
	void direccionPuedeListarUsuarios() throws Exception {
		when(usuarioService.listar(isNull(), isNull(), any(Pageable.class))).thenReturn(Page.empty());

		mvc.perform(get("/api/usuarios")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminNoPuedeListarDatosPersonales() throws Exception {
		mvc.perform(get("/api/usuarios")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "COORDINADOR")
	void coordinadorNoPuedeCrearUsuarios() throws Exception {
		mvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyUsuario()))
				.andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "DIRECCION")
	void direccionPuedeCrearUsuarios() throws Exception {
		when(usuarioService.crear(any(UsuarioCreateRequest.class))).thenReturn(new UsuarioResponse(
				UUID.randomUUID(), "Nuevo Docente", "nuevo@escuela.mx", "DOCENTE", true, "local"));

		mvc.perform(post("/api/usuarios")
				.contentType(MediaType.APPLICATION_JSON)
				.content(bodyUsuario()))
				.andExpect(status().isCreated());
	}

	@Test
	@WithMockUser(roles = "DOCENTE")
	void docenteNoPuedeConsultarReportes() throws Exception {
		mvc.perform(get("/api/reportes/planeaciones")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "COORDINADOR")
	void coordinadorPuedeConsultarReportes() throws Exception {
		when(reporteService.cumplimientoPlaneaciones(isNull()))
				.thenReturn(new ReportePlaneacionesResponse(0, 0, 0, 0, 0, 0.0));

		mvc.perform(get("/api/reportes/planeaciones")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "DIRECCION")
	void direccionNoPuedeVerLaMatrizRbac() throws Exception {
		mvc.perform(get("/api/seguridad/rbac")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminPuedeVerLaMatrizRbac() throws Exception {
		mvc.perform(get("/api/seguridad/rbac")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "DIRECCION")
	void direccionNoPuedeConsultarLaPoliticaDeRetencion() throws Exception {
		mvc.perform(get("/api/retencion/politica")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminPuedeConsultarLaPoliticaDeRetencion() throws Exception {
		mvc.perform(get("/api/retencion/politica")).andExpect(status().isOk());
	}

	@Test
	@WithMockUser(roles = "DIRECCION")
	void direccionNoPuedeConsultarElEstadoDeCifrado() throws Exception {
		mvc.perform(get("/api/cifrado/estado")).andExpect(status().isForbidden());
	}

	@Test
	@WithMockUser(roles = "ADMIN")
	void adminPuedeConsultarElEstadoDeCifrado() throws Exception {
		mvc.perform(get("/api/cifrado/estado")).andExpect(status().isOk());
	}

	@Test
	void sinTokenNoPuedeConsultarSuPerfil() throws Exception {
		mvc.perform(get("/api/auth/me")).andExpect(status().isUnauthorized());
	}

	@Test
	void autenticadoPuedeConsultarSuPerfil() throws Exception {
		Usuario usuario = new Usuario("Docente", "docente@escuela.mx", "password", new Rol("DOCENTE", "Docente"));
		UsuarioPrincipal principal = new UsuarioPrincipal(usuario);
		UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
				principal, null, principal.getAuthorities());
		when(authService.perfil(any(UsuarioPrincipal.class))).thenReturn(new UsuarioResponse(
				UUID.randomUUID(), "Docente", "docente@escuela.mx", "DOCENTE", true, "local"));

		mvc.perform(get("/api/auth/me").with(authentication(autenticacion)))
				.andExpect(status().isOk());
	}

	private String bodyUsuario() {
		return """
				{
				  "nombre": "Nuevo Docente",
				  "email": "nuevo@escuela.mx",
				  "password": "Password123",
				  "rol": "DOCENTE"
				}
				""";
	}
}
