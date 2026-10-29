-- V20: Consentimiento para transferencia de datos personales a terceros (S3-12)
CREATE TABLE IF NOT EXISTS consentimiento_terceros (
    id                        UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id                UUID         NOT NULL,
    tercero                   VARCHAR(50)  NOT NULL,
    consentimiento_otorgado   BOOLEAN      NOT NULL DEFAULT FALSE,
    version_aviso             VARCHAR(20),
    ip_otorgamiento           VARCHAR(45),
    user_agent                VARCHAR(500),
    fecha_otorgamiento        TIMESTAMPTZ,
    fecha_revocacion          TIMESTAMPTZ,
    motivo_revocacion         VARCHAR(255),
    created_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at                TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_consentimiento_terceros_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT uq_consentimiento_terceros_usuario_tercero UNIQUE (usuario_id, tercero),
    CONSTRAINT chk_consentimiento_terceros_tercero CHECK (tercero IN ('GOOGLE_CALENDAR'))
);

CREATE INDEX IF NOT EXISTS idx_consentimiento_terceros_usuario ON consentimiento_terceros (usuario_id);
CREATE INDEX IF NOT EXISTS idx_consentimiento_terceros_tercero ON consentimiento_terceros (tercero);

CREATE TRIGGER trg_consentimiento_terceros_updated_at
    BEFORE UPDATE ON consentimiento_terceros
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();