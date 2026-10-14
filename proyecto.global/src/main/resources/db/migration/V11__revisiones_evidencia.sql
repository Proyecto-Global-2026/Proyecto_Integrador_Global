-- ===========================================================================
-- V11: Revisiones de evidencias
-- El coordinador valida (aprueba/rechaza) las evidencias de capacitacion.
-- Se agrega el estado a la evidencia y se registra cada revision.
-- ===========================================================================
ALTER TABLE evidencias ADD COLUMN estado VARCHAR(20) NOT NULL DEFAULT 'PENDIENTE';
ALTER TABLE evidencias ADD CONSTRAINT chk_evidencias_estado
    CHECK (estado IN ('PENDIENTE', 'APROBADA', 'RECHAZADA'));

CREATE INDEX ix_evidencias_estado ON evidencias (estado);

CREATE TABLE revisiones_evidencia (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    evidencia_id    UUID         NOT NULL,
    estado_anterior VARCHAR(20)  NOT NULL,
    estado_nuevo    VARCHAR(20)  NOT NULL,
    comentario      VARCHAR(1000),
    revisado_por    UUID         NOT NULL,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_revisiones_evidencia FOREIGN KEY (evidencia_id)
        REFERENCES evidencias (id) ON DELETE CASCADE,
    CONSTRAINT fk_revisiones_evidencia_revisado_por FOREIGN KEY (revisado_por)
        REFERENCES usuarios (id) ON DELETE RESTRICT,
    CONSTRAINT chk_revisiones_evidencia_estados CHECK (
        estado_anterior IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')
        AND estado_nuevo IN ('PENDIENTE', 'APROBADA', 'RECHAZADA')
    )
);

CREATE INDEX ix_revisiones_evidencia_id ON revisiones_evidencia (evidencia_id);
CREATE INDEX ix_revisiones_evidencia_created_at ON revisiones_evidencia (created_at);
