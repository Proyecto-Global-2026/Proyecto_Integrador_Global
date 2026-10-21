package com.example.proyecto.global.storage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Duration;
import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.example.proyecto.global.config.StorageProperties;
import com.example.proyecto.global.exception.BusinessRuleException;

/**
 * Verifica el contrato de las URLs firmadas de descarga de Cloudinary.
 *
 * <p>
 * La firma se calcula localmente con el SDK, por lo que la prueba no necesita
 * acceso a la red ni a una cuenta real de Cloudinary.
 * </p>
 */
class CloudinaryStorageServiceTest {

	private static final String CLOUD_NAME = "pruebas";
	private static final String API_KEY = "123456789012345";
	private static final String API_SECRET = "clave-de-prueba-para-firmar";

	private CloudinaryStorageService servicio;

	@BeforeEach
	void setUp() {
		StorageProperties properties = new StorageProperties(
				"cloudinary",
				new StorageProperties.LocalProps("uploads/pruebas", true),
				new StorageProperties.CloudinaryProps(
						CLOUD_NAME, API_KEY, API_SECRET, true, 5000, "raw", "proyecto-global",
						"authenticated", 5),
				10485760,
				java.util.List.of("pdf", "txt", "png"));
		Cloudinary cloudinary = new Cloudinary(ObjectUtils.asMap(
				"cloud_name", CLOUD_NAME,
				"api_key", API_KEY,
				"api_secret", API_SECRET,
				"secure", true));
		servicio = new CloudinaryStorageService(cloudinary, properties);
	}

	@Test
	void generaUrlFirmadaConExpiracionYComoAdjunto() {
		String publicId = "proyecto-global/evidencias/6f1c/evidencia.pdf";

		String url = servicio.generarUrlDescargaSegura(publicId, "raw", "authenticated", null, Duration.ofMinutes(5));

		assertTrue(url.startsWith("https://api.cloudinary.com/v1_1/" + CLOUD_NAME + "/raw/download"),
				"La URL debe apuntar al endpoint de descarga de la API: " + url);
		assertTrue(url.contains("expires_at="), "La URL debe incluir la expiracion: " + url);
		assertTrue(url.contains("attachment=true"), "La URL debe forzar la descarga como adjunto: " + url);
		assertTrue(url.contains("type=authenticated"), "La URL debe respetar el tipo de entrega: " + url);
		assertTrue(url.contains("format=pdf"), "La URL debe incluir el formato: " + url);
		assertTrue(url.contains("signature="), "La URL debe estar firmada: " + url);
		assertTrue(url.contains("api_key=" + API_KEY), "La URL debe identificar la api key: " + url);
		assertTrue(!url.contains(API_SECRET), "La URL nunca debe incluir el api secret: " + url);
	}

	@Test
	void laExpiracionRespetaLaDuracionSolicitada() {
		long antes = Instant.now().getEpochSecond();

		String url = servicio.generarUrlDescargaSegura("proyecto-global/evidencias/6f1c/nota.txt", "raw",
				"authenticated", null, Duration.ofMinutes(5));

		long expiresAt = valorParametro(url, "expires_at");
		long ahora = Instant.now().getEpochSecond();
		assertTrue(expiresAt >= antes + 300 && expiresAt <= ahora + 300,
				"expires_at deberia estar 5 minutos en el futuro: " + expiresAt);
	}

	@Test
	void laUrlDifiereSegunElTipoDeEntrega() {
		String autenticada = servicio.generarUrlDescargaSegura("proyecto-global/x/archivo.pdf", "raw",
				"authenticated", null, Duration.ofMinutes(5));
		String publica = servicio.generarUrlDescargaSegura("proyecto-global/x/archivo.pdf", "raw", "upload", null, Duration.ofMinutes(5));

		assertTrue(autenticada.contains("type=authenticated"));
		assertTrue(publica.contains("type=upload"));
	}

	@Test
	void fallaConReferenciaInvalida() {
		BusinessRuleException error = assertThrows(BusinessRuleException.class,
				() -> servicio.generarUrlDescargaSegura("  ", "raw", "authenticated", null, Duration.ofMinutes(5)));

		assertTrue(error.getMessage().contains("invalida"));
	}

	@Test
	void normalizaElNombreOriginalParaElPublicId() {
		assertEquals("evidencia_de_prueba.pdf", servicio.nombreSeguro("evidencia de prueba.pdf"));
		assertEquals("evidencia_de_prueba.pdf", servicio.nombreSeguro("evidencia/de\\prueba.pdf"));
		assertEquals("archivo", servicio.nombreSeguro("..."));
		assertTrue(servicio.nombreSeguro("a".repeat(200) + ".pdf").length() <= 120);
	}

	@Test
	void extraeLaExtensionDelFormato() {
		assertEquals("pdf", servicio.extension("evidencia.pdf"));
		assertEquals("png", servicio.extension("FOTO.PNG"));
		assertEquals("", servicio.extension("sin-extension"));
	}

	@Test
	void elAlmacenamientoLocalNoGeneraUrlDeRedireccion() {
		StorageProperties properties = new StorageProperties(
				"local",
				new StorageProperties.LocalProps("uploads/pruebas", true),
				new StorageProperties.CloudinaryProps(
						CLOUD_NAME, API_KEY, API_SECRET, true, 5000, "raw", "proyecto-global",
						"authenticated", 5),
				10485760,
				java.util.List.of("pdf"));
		StorageService local = new LocalStorageService(properties);

		assertNull(local.generarUrlDescargaSegura("cualquier.pdf", "raw", "local", null, Duration.ofMinutes(5)),
				"En modo local la API debe servir el contenido, no redirigir");
	}

	private long valorParametro(String url, String nombre) {
		for (String parte : url.split("[?&]")) {
			if (parte.startsWith(nombre + "=")) {
				return Long.parseLong(parte.substring(nombre.length() + 1));
			}
		}
		throw new AssertionError("No se encontro el parametro " + nombre + " en " + url);
	}
}
