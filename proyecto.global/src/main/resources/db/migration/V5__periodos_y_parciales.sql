-- ===========================================================================
-- V5: Periodos y parciales
-- ===========================================================================
CREATE TABLE periodos (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre        VARCHAR(100) NOT NULL,
    fecha_inicio  DATE         NOT NULL,
    fecha_fin     DATE         NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_periodos_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT uq_periodos_nombre UNIQUE (nombre)
);

CREATE INDEX ix_periodos_activo ON periodos (activo);
CREATE INDEX ix_periodos_fechas ON periodos (fecha_inicio, fecha_fin);

CREATE TRIGGER trg_periodos_updated_at
    BEFORE UPDATE ON periodos
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

CREATE TABLE parciales (
    id            UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    periodo_id    UUID         NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    fecha_inicio  DATE         NOT NULL,
    fecha_fin     DATE         NOT NULL,
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at    TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_parciales_fechas CHECK (fecha_fin >= fecha_inicio),
    CONSTRAINT fk_parciales_periodo FOREIGN KEY (periodo_id)
        REFERENCES periodos (id) ON DELETE RESTRICT,
    CONSTRAINT uq_parciales_periodo_nombre UNIQUE (periodo_id, nombre)
);

CREATE INDEX ix_parciales_periodo_id ON parciales (periodo_id);
CREATE INDEX ix_parciales_activo ON parciales (activo);
CREATE INDEX ix_parciales_fechas ON parciales (fecha_inicio, fecha_fin);

CREATE TRIGGER trg_parciales_updated_at
    BEFORE UPDATE ON parciales
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();