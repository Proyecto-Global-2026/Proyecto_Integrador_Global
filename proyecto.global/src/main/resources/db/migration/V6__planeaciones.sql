-- ===========================================================================
-- V6: Planeaciones didacticas
-- Una planeacion pertenece a un docente y apunta a una materia y un parcial.
-- Estado inicial: PENDIENTE (despues el flujo de revision la aprueba o rechaza).
-- ===========================================================================
CREATE TABLE planeaciones (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    docente_id  UUID         NOT NULL,
    materia_id  UUID         NOT NULL,
    parcial_id  UUID         NOT NULL,
    titulo      VARCHAR(200) NOT NULL,
    contenido   TEXT         NOT NULL,
    estado      VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_planeaciones_estado CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')),
    CONSTRAINT fk_planeaciones_docente FOREIGN KEY (docente_id)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT fk_planeaciones_materia FOREIGN KEY (materia_id)
        REFERENCES materias (id) ON DELETE RESTRICT,
    CONSTRAINT fk_planeaciones_parcial FOREIGN KEY (parcial_id)
        REFERENCES parciales (id) ON DELETE RESTRICT,
    CONSTRAINT uq_planeaciones_docente_materia_parcial UNIQUE (docente_id, materia_id, parcial_id)
);

CREATE INDEX ix_planeaciones_docente_id ON planeaciones (docente_id);
CREATE INDEX ix_planeaciones_materia_id ON planeaciones (materia_id);
CREATE INDEX ix_planeaciones_parcial_id ON planeaciones (parcial_id);
CREATE INDEX ix_planeaciones_estado ON planeaciones (estado);
CREATE INDEX ix_planeaciones_titulo_trgm ON planeaciones USING gin (titulo gin_trgm_ops);

CREATE TRIGGER trg_planeaciones_updated_at
    BEFORE UPDATE ON planeaciones
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();