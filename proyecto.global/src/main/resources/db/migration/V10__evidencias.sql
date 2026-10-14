-- ===========================================================================
-- V10: Evidencias de capacitacion docente
-- El docente registra cursos/capacitaciones con sus horas acreditadas y,
-- opcionalmente, un archivo adjunto.
-- ===========================================================================
CREATE TABLE evidencias (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id         UUID         NOT NULL,
    curso              VARCHAR(200) NOT NULL,
    institucion        VARCHAR(200) NOT NULL,
    fecha              DATE         NOT NULL,
    horas_acreditadas  INTEGER      NOT NULL,
    nombre_original    VARCHAR(255),
    nombre_almacenado  VARCHAR(255) UNIQUE,
    content_type       VARCHAR(100),
    tamano             BIGINT,
    ruta               VARCHAR(500),
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_evidencias_horas CHECK (horas_acreditadas > 0),
    CONSTRAINT fk_evidencias_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE INDEX ix_evidencias_usuario_id ON evidencias (usuario_id);
CREATE INDEX ix_evidencias_fecha ON evidencias (fecha);
CREATE INDEX ix_evidencias_curso_trgm ON evidencias USING gin (curso gin_trgm_ops);

CREATE TRIGGER trg_evidencias_updated_at
    BEFORE UPDATE ON evidencias
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
