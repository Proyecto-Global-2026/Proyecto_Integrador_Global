package com.example.proyecto.global.exception;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;

class GlobalExceptionHandlerTest {

	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		mockMvc = MockMvcBuilders.standaloneSetup(new TestController())
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void recursoNoEncontradoRetorna404ConPath() throws Exception {
		mockMvc.perform(get("/api/planeaciones/99"))
				.andExpect(status().isNotFound())
				.andExpect(jsonPath("$.status").value(404))
				.andExpect(jsonPath("$.message").value("No existe la planeacion"))
				.andExpect(jsonPath("$.path").value("/api/planeaciones/99"))
				.andExpect(jsonPath("$.timestamp").exists());
	}

	@Test
	void reglaDeNegocioRetorna422() throws Exception {
		mockMvc.perform(get("/api/planeaciones/aprobada"))
				.andExpect(status().isUnprocessableEntity())
				.andExpect(jsonPath("$.status").value(422))
				.andExpect(jsonPath("$.message").value("La planeacion ya fue aprobada"));
	}

	@Test
	void bodyInvalidoRetorna400ConErroresPorCampo() throws Exception {
		mockMvc.perform(post("/api/planeaciones")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"titulo\":\"\"}"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.status").value(400))
				.andExpect(jsonPath("$.fieldErrors.titulo").value("El titulo es obligatorio"));
	}

	@Test
	void jsonMalformadoRetorna400SinFiltrarInternals() throws Exception {
		mockMvc.perform(post("/api/planeaciones")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{no-json"))
				.andExpect(status().isBadRequest())
				.andExpect(jsonPath("$.message").value("La solicitud enviada no es valida"));
	}

	@Test
	void errorInesperadoRetorna500SinFiltrarInternals() throws Exception {
		mockMvc.perform(get("/api/planeaciones/error"))
				.andExpect(status().isInternalServerError())
				.andExpect(jsonPath("$.status").value(500))
				.andExpect(jsonPath("$.message").value("Ocurrio un error inesperado. Intenta nuevamente."));
	}

	@RestController
	@RequestMapping("/api/planeaciones")
	static class TestController {

		@GetMapping("/99")
		ResponseEntity<Void> noEncontrada() {
			throw new ResourceNotFoundException("No existe la planeacion");
		}

		@GetMapping("/aprobada")
		ResponseEntity<Void> yaAprobada() {
			throw new BusinessRuleException("La planeacion ya fue aprobada");
		}

		@GetMapping("/error")
		ResponseEntity<Void> errorInesperado() {
			throw new IllegalStateException("detalle interno sensible");
		}

		@PostMapping
		ResponseEntity<Void> crear(@Valid @RequestBody PlaneacionRequest request) {
			return ResponseEntity.noContent().build();
		}
	}

	record PlaneacionRequest(@NotBlank(message = "El titulo es obligatorio") String titulo) {
	}
}