-- ===========================================================================
-- V4: Materias
-- ===========================================================================
CREATE TABLE materias (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre       VARCHAR(150) NOT NULL,
    codigo       VARCHAR(50)  NOT NULL,
    descripcion  VARCHAR(255),
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_materias_codigo UNIQUE (codigo)
);

CREATE UNIQUE INDEX uq_materias_codigo_lower ON materias (LOWER(codigo));
CREATE INDEX ix_materias_nombre_trgm ON materias USING gin (nombre gin_trgm_ops);
CREATE INDEX ix_materias_activo ON materias (activo);

CREATE TRIGGER trg_materias_updated_at
    BEFORE UPDATE ON materias
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();