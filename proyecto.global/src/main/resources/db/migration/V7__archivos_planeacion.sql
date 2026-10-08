-- ===========================================================================
-- V7: Archivos adjuntos de planeaciones
-- Metadatos del archivo subido; el binario vive en el almacenamiento local
-- (por ahora) y se mueve a almacenamiento externo con la issue #29.
-- ===========================================================================
CREATE TABLE archivos_planeacion (
    id                UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    planeacion_id     UUID         NOT NULL,
    nombre_original   VARCHAR(255) NOT NULL,
    nombre_almacenado VARCHAR(255) NOT NULL,
    content_type      VARCHAR(100) NOT NULL,
    tamano            BIGINT       NOT NULL,
    ruta              VARCHAR(500) NOT NULL,
    created_at        TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_archivos_planeacion FOREIGN KEY (planeacion_id)
        REFERENCES planeaciones (id) ON DELETE CASCADE,
    CONSTRAINT uq_archivos_nombre_almacenado UNIQUE (nombre_almacenado),
    CONSTRAINT chk_archivos_tamano CHECK (tamano > 0)
);

CREATE INDEX ix_archivos_planeacion_id ON archivos_planeacion (planeacion_id);
CREATE INDEX ix_archivos_created_at ON archivos_planeacion (created_at);