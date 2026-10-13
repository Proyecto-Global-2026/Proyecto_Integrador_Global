-- ===========================================================================
-- V9: Avances por parcial
-- El docente registra el avance logrado en el parcial de su planeacion.
-- Estado: CUMPLIDO, PARCIAL, NO_CUMPLIDO.
-- ===========================================================================
CREATE TABLE avances_parcial (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    planeacion_id   UUID         NOT NULL,
    parcial_id      UUID         NOT NULL,
    estado          VARCHAR(20)  NOT NULL,
    observaciones   VARCHAR(1000),
    registrado_por  UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_avances_parcial_estado CHECK (estado IN ('CUMPLIDO', 'PARCIAL', 'NO_CUMPLIDO')),
    CONSTRAINT fk_avances_parcial_planeacion FOREIGN KEY (planeacion_id)
        REFERENCES planeaciones (id) ON DELETE CASCADE,
    CONSTRAINT fk_avances_parcial_parcial FOREIGN KEY (parcial_id)
        REFERENCES parciales (id) ON DELETE RESTRICT,
    CONSTRAINT fk_avances_parcial_registrado_por FOREIGN KEY (registrado_por)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT uq_avances_planeacion_parcial UNIQUE (planeacion_id, parcial_id)
);

CREATE INDEX ix_avances_parcial_planeacion_id ON avances_parcial (planeacion_id);
CREATE INDEX ix_avances_parcial_parcial_id ON avances_parcial (parcial_id);
CREATE INDEX ix_avances_parcial_estado ON avances_parcial (estado);

CREATE TRIGGER trg_avances_parcial_updated_at
    BEFORE UPDATE ON avances_parcial
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
