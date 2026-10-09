-- ===========================================================================
-- V8: Revisiones de planeaciones
-- Amplia el ciclo de vida con AJUSTES_SOLICITADOS y registra cada
-- revision del coordinador (estado anterior/nuevo + comentario).
-- ===========================================================================
ALTER TABLE planeaciones DROP CONSTRAINT chk_planeaciones_estado;
ALTER TABLE planeaciones ADD CONSTRAINT chk_planeaciones_estado
    CHECK (estado IN ('PENDIENTE', 'AJUSTES_SOLICITADOS', 'APROBADA', 'RECHAZADA'));

CREATE TABLE revisiones_planeacion (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    planeacion_id   UUID         NOT NULL,
    estado_anterior VARCHAR(20)  NOT NULL,
    estado_nuevo    VARCHAR(20)  NOT NULL,
    comentario      VARCHAR(1000),
    revisado_por    UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_revisiones_planeacion FOREIGN KEY (planeacion_id)
        REFERENCES planeaciones (id) ON DELETE CASCADE,
    CONSTRAINT fk_revisiones_revisado_por FOREIGN KEY (revisado_por)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_revisiones_estados CHECK (
        estado_anterior IN ('PENDIENTE', 'AJUSTES_SOLICITADOS', 'APROBADA', 'RECHAZADA')
        AND estado_nuevo IN ('PENDIENTE', 'AJUSTES_SOLICITADOS', 'APROBADA', 'RECHAZADA')
    )
);

CREATE INDEX ix_revisiones_planeacion_id ON revisiones_planeacion (planeacion_id);
CREATE INDEX ix_revisiones_created_at ON revisiones_planeacion (created_at);