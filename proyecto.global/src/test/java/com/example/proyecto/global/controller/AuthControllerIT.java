package com.example.proyecto.global.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.repository.RolRepository;
import com.example.proyecto.global.repository.UsuarioRepository;
import com.example.proyecto.global.security.JwtService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AuthControllerIT {

	private static final String PASSWORD = "ClaveSegura123";

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private UsuarioRepository usuarioRepository;

	@Autowired
	private RolRepository rolRepository;

	@Autowired
	private PasswordEncoder passwordEncoder;

	@Autowired
	private JwtService jwtService;

	@BeforeEach
	void sembrarRoles() {
		for (Rol rol : new Rol[] { new Rol("DOCENTE", "Registra sus planeaciones"),
				new Rol("COORDINADOR", "Aprueba o rechaza"), new Rol("DIRECCION", "Consulta el historial") }) {
			if (rolRepository.findByNombre(rol.getNombre()).isEmpty()) {
				rolRepository.save(rol);
			}
		}
	}

	@Test
	void registroCreaUsuarioConRolDocente() throws Exception {
		mockMvc.perform(post("/api/auth/registro")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registro("nuevo.docente@escuela.edu")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("nuevo.docente@escuela.edu"))
				.andExpect(jsonPath("$.rol").value("DOCENTE"))
				.andExpect(jsonPath("$.activo").value(true))
				.andExpect(jsonPath("$.password").doesNotExist());
	}

	@Test
	void correoDuplicadoRetorna409() throws Exception {
		mockMvc.perform(post("/api/auth/registro")
				.contentType(MediaType.APPLICATION_JSON)
				.content(registro("duplicado@escuela.edu")))
				.andExpect(status().isCreated());

		mockMvc.perform(post("/api/auth/registro")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registro("duplicado@escuela.edu")))
				.andExpect(status().isConflict())
				.andExpect(jsonPath("$.message").value("El correo duplicado@escuela.edu ya esta registrado"));
	}

	@Test
	void registroNormalizaElCorreoAMinusculas() throws Exception {
		mockMvc.perform(post("/api/auth/registro")
						.contentType(MediaType.APPLICATION_JSON)
						.content(registro("Mayusculas@Escuela.EDU")))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.email").value("mayusculas@escuela.edu"));
	}

	@Test
	void registroValidaLosCamposObligatorios() throws Exception {
		mockMvc.perform(post("/api/auth/registro")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"nombre\":\"\",\"email\":\"no-es-correo\",\"password\":\"123\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.fieldErrors.nombre").exists())
				.andExpect(jsonPath("$.fieldErrors.email").exists())
				.andExpect(jsonPath("$.fieldErrors.password").exists());
	}

	@Test
	void loginDevuelveTokenYUsuario() throws Exception {
		crearUsuario("login@escuela.edu", "DOCENTE");

		mockMvc.perform(crearPeticionLogin("login@escuela.edu", PASSWORD))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty())
				.andExpect(jsonPath("$.tipo").value("Bearer"))
				.andExpect(jsonPath("$.expiraEnSegundos").value(3600))
				.andExpect(jsonPath("$.usuario.email").value("login@escuela.edu"))
				.andExpect(jsonPath("$.usuario.rol").value("DOCENTE"));
	}

	@Test
	void loginNormalizaElCorreoAMinusculas() throws Exception {
		crearUsuario("mayus.login@escuela.edu", "DOCENTE");

		mockMvc.perform(crearPeticionLogin("MAYUS.LOGIN@ESCUELA.EDU", PASSWORD))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").isNotEmpty());
	}

	@Test
	void loginConContrasenaIncorrectaRetorna401() throws Exception {
		crearUsuario("mala.clave@escuela.edu", "DOCENTE");

		mockMvc.perform(crearPeticionLogin("mala.clave@escuela.edu", "ContrasenaEquivocada999"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Credenciales invalidas"));
	}

	@Test
	void loginConCorreoInexistenteRetorna401() throws Exception {
		mockMvc.perform(crearPeticionLogin("nadie@escuela.edu", PASSWORD))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void perfilDevuelveElUsuarioDelToken() throws Exception {
		Usuario usuario = crearUsuario("perfil@escuela.edu", "DOCENTE");

		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + jwtService.generateToken(usuario)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.email").value("perfil@escuela.edu"))
				.andExpect(jsonPath("$.rol").value("DOCENTE"));
	}

	@Test
	void perfilSinTokenRetorna401() throws Exception {
		mockMvc.perform(get("/api/auth/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Debes iniciar sesion para acceder a este recurso"));
	}

	@Test
	void perfilConTokenInvalidoRetorna401() throws Exception {
		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer token-falso"))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void perfilConTokenFirmadoConOtraClaveRetorna401() throws Exception {
		crearUsuario("clave.ajena@escuela.edu", "DOCENTE");
		String tokenAjeno = new JwtService("otra-clave-de-pruebas-suficientemente-larga-9876543210", 30)
				.generateToken(new Usuario("Usuario de prueba", "clave.ajena@escuela.edu", "hash",
						rolRepository.findByNombre("DOCENTE").orElseThrow()));

		mockMvc.perform(get("/api/auth/me").header("Authorization", "Bearer " + tokenAjeno))
				.andExpect(status().isUnauthorized());
	}

	@Test
	void rbacDocenteNoPuedeListarUsuarios() throws Exception {
		Usuario docente = crearUsuario("docente.rbac@escuela.edu", "DOCENTE");

		mockMvc.perform(get("/api/auth/usuarios")
						.header("Authorization", "Bearer " + jwtService.generateToken(docente)))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.message").value("No tienes permisos para acceder a este recurso"));
	}

	@Test
	void rbacCoordinadorSiPuedeListarUsuarios() throws Exception {
		Usuario coordinador = crearUsuario("coord.rbac@escuela.edu", "COORDINADOR");

		mockMvc.perform(get("/api/auth/usuarios")
						.header("Authorization", "Bearer " + jwtService.generateToken(coordinador)))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$").isArray());
	}

	@Test
	void rbacDireccionSiPuedeListarUsuarios() throws Exception {
		Usuario direccion = crearUsuario("dir.rbac@escuela.edu", "DIRECCION");

		mockMvc.perform(get("/api/auth/usuarios")
						.header("Authorization", "Bearer " + jwtService.generateToken(direccion)))
				.andExpect(status().isOk());
	}

	@Test
	void listarUsuariosSinTokenRetorna401() throws Exception {
		mockMvc.perform(get("/api/auth/usuarios"))
				.andExpect(status().isUnauthorized());
	}

	private MockHttpServletRequestBuilder crearPeticionLogin(String email, String password) {
		String body = """
				{"email":"%s","password":"%s"}
				""".formatted(email, password);
		return post("/api/auth/login").contentType(MediaType.APPLICATION_JSON).content(body);
	}

	private String registro(String email) {
		return """
				{"nombre":"Usuario de prueba","email":"%s","password":"%s"}
				""".formatted(email, PASSWORD);
	}

	private Usuario crearUsuario(String email, String rolNombre) {
		return usuarioRepository.save(new Usuario(
				"Usuario de prueba",
				email,
				passwordEncoder.encode(PASSWORD),
				rolRepository.findByNombre(rolNombre).orElseThrow()));
	}
}