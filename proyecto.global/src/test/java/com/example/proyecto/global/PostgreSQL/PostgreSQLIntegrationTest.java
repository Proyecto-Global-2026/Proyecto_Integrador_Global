package com.example.proyecto.global.PostgreSQL;

import com.example.proyecto.global.domain.Rol;
import com.example.proyecto.global.domain.Usuario;
import com.example.proyecto.global.repository.UsuarioRepository;

import org.flywaydb.core.Flyway;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

import jakarta.persistence.EntityManager;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest(properties = {
        "spring.jpa.hibernate.ddl-auto=validate",
        "spring.jpa.properties.hibernate.default_schema=proyecto_global",
        "spring.flyway.enabled=true",
        "spring.flyway.schemas=proyecto_global",
        "spring.flyway.default-schema=proyecto_global",
        "spring.flyway.create-schemas=true",
        "spring.flyway.baseline-on-migrate=true"
})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Testcontainers
class PostgreSQLIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES =
            new PostgreSQLContainer("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDatabase(DynamicPropertyRegistry registry) {

        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add(
                "spring.datasource.driver-class-name",
                () -> "org.postgresql.Driver"
        );

        registry.add("spring.flyway.enabled", () -> true);
        registry.add("spring.flyway.schemas", () -> "proyecto_global");
        registry.add(
                "spring.flyway.default-schema",
                () -> "proyecto_global"
        );
        registry.add("spring.flyway.create-schemas", () -> true);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "validate");
        registry.add(
                "spring.jpa.properties.hibernate.default_schema",
                () -> "proyecto_global"
        );
    }

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private EntityManager entityManager;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private Flyway flyway;

    /**
     * Verifica que Flyway ejecute correctamente las migraciones.
     */
    @Test
    void debeEjecutarTodasLasMigraciones() {

        // 1. Verificar que Flyway tenga una migración actual.
        assertNotNull(
                flyway.info().current(),
                "Flyway debe tener una migración actual"
        );

        // 2. Verificar que la última versión sea V21.
        assertEquals(
                "21",
                flyway.info().current().getVersion().getVersion(),
                "La versión actual de Flyway debe ser 21"
        );

        // 3. Validar los checksums de las migraciones.
        assertTrue(
                flyway.validateWithResult().validationSuccessful,
                "Todas las migraciones deben tener checksums válidos"
        );

        // 4. Diagnóstico: mostrar todos los registros de Flyway.
        System.out.println("\n==============================================");
        System.out.println(" DIAGNÓSTICO DE FLYWAY");
        System.out.println("==============================================");

        jdbcTemplate.query(
                """
                SELECT
                    installed_rank,
                    version,
                    description,
                    type,
                    script,
                    success
                FROM proyecto_global.flyway_schema_history
                ORDER BY installed_rank
                """,
                rs -> {

                    System.out.println(
                            "Registro: " + rs.getInt("installed_rank")
                                    + " | Versión: " + rs.getString("version")
                                    + " | Descripción: " + rs.getString("description")
                                    + " | Tipo: " + rs.getString("type")
                                    + " | Archivo: " + rs.getString("script")
                                    + " | Exitosa: " + rs.getBoolean("success")
                    );
                }
        );

        System.out.println("==============================================\n");

        // 5. Contar las migraciones exitosas.
        Integer cantidadMigraciones = jdbcTemplate.queryForObject(
                """
                SELECT COUNT(*)
                FROM proyecto_global.flyway_schema_history
                WHERE success = TRUE
                  AND type = 'SQL'
                """,
                Integer.class
        );

        assertNotNull(cantidadMigraciones);

        assertEquals(
                21,
                cantidadMigraciones,
                "Deben haberse aplicado correctamente las 21 migraciones"
        );

        // 6. Verificar que existan los cuatro roles iniciales.
        Integer cantidadRoles = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM proyecto_global.roles",
                Integer.class
        );

        assertNotNull(cantidadRoles);

        assertEquals(
                4,
                cantidadRoles,
                "Deben existir los cuatro roles iniciales"
        );
    }

    /**
     * Verifica que PostgreSQL permita guardar y recuperar usuarios
     * relacionados con un rol.
     */
    @Test
    void debeGuardarYRecuperarUsuarioConRolEnPostgreSQL() {

        Short rolId = jdbcTemplate.queryForObject(
                "SELECT id FROM proyecto_global.roles WHERE nombre = ?",
                Short.class,
                "DOCENTE"
        );

        assertNotNull(rolId);

        Rol rol = entityManager.find(Rol.class, rolId);

        assertNotNull(rol);

        Usuario usuario = new Usuario(
                "Usuario de integración",
                "integracion@test.local",
                "$2a$10$hashDePrueba",
                rol
        );

        Usuario guardado = usuarioRepository.saveAndFlush(usuario);

        assertNotNull(
                guardado.getId(),
                "El usuario debe tener un ID generado"
        );

        entityManager.clear();

        Usuario recuperado = usuarioRepository
                .findByEmail("integracion@test.local")
                .orElseThrow();

        assertEquals(
                "Usuario de integración",
                recuperado.getNombre()
        );

        assertEquals(
                "DOCENTE",
                recuperado.getRol().getNombre()
        );

        assertTrue(
                recuperado.isActivo(),
                "El usuario debe estar activo por defecto"
        );

        assertNotNull(
                recuperado.getCreatedAt(),
                "La fecha de creación no debe ser nula"
        );

        assertNotNull(
                recuperado.getUpdatedAt(),
                "La fecha de actualización no debe ser nula"
        );
    }

    /**
     * Verifica que PostgreSQL impida registrar dos usuarios
     * con el mismo correo electrónico.
     */
    @Test
    void debeRespetarLaUnicidadDelCorreo() {

        Short rolId = jdbcTemplate.queryForObject(
                "SELECT id FROM proyecto_global.roles WHERE nombre = ?",
                Short.class,
                "DOCENTE"
        );

        assertNotNull(rolId);

        Rol rol = entityManager.find(Rol.class, rolId);

        assertNotNull(rol);

        Usuario primero = new Usuario(
                "Primer usuario",
                "duplicado@test.local",
                "$2a$10$hashDePrueba",
                rol
        );

        usuarioRepository.saveAndFlush(primero);

        Usuario segundo = new Usuario(
                "Segundo usuario",
                "duplicado@test.local",
                "$2a$10$otroHashDePrueba",
                rol
        );

        assertThrows(
                DataIntegrityViolationException.class,
                () -> usuarioRepository.saveAndFlush(segundo),
                "PostgreSQL debe rechazar dos usuarios con el mismo correo"
        );
    }
}