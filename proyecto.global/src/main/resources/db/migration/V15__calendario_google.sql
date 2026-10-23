-- ===========================================================================
-- V15: Integracion con Google Calendar (S3-03)
--
-- Guarda la conexion OAuth2 del docente con su calendario de Google y el
-- registro de sincronizacion de cada fecha limite de parcial, para no
-- duplicar eventos ni perder el id del evento creado en Google.
-- ===========================================================================

-- ---------------------------------------------------------------------------
-- Conexion OAuth2 del usuario con Google Calendar.
-- El refresh_token se guarda para poder crear/actualizar eventos sin que el
-- usuario tenga que volver a autorizar en cada peticion.
-- ---------------------------------------------------------------------------
CREATE TABLE conexiones_calendario (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id         UUID         NOT NULL,
    proveedor          VARCHAR(30)  NOT NULL DEFAULT 'google',
    correo_calendario  VARCHAR(180),
    access_token       TEXT,
    refresh_token      TEXT,
    expira_en          TIMESTAMPTZ,
    alcance            VARCHAR(300),
    activo             BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_conexiones_calendario_usuario_proveedor UNIQUE (usuario_id, proveedor),
    CONSTRAINT fk_conexiones_calendario_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE
);

CREATE INDEX ix_conexiones_calendario_usuario_id ON conexiones_calendario (usuario_id);
CREATE INDEX ix_conexiones_calendario_activo ON conexiones_calendario (activo);

CREATE TRIGGER trg_conexiones_calendario_updated_at
    BEFORE UPDATE ON conexiones_calendario
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();

-- ---------------------------------------------------------------------------
-- Registro de eventos creados en Google Calendar.
-- Una fila por (usuario, parcial): permite sincronizar de forma idempotente
-- y actualizar o borrar el evento cuando cambia la fecha limite.
-- ---------------------------------------------------------------------------
CREATE TABLE eventos_calendario (
    id                 UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id         UUID         NOT NULL,
    parcial_id         UUID         NOT NULL,
    proveedor          VARCHAR(30)  NOT NULL DEFAULT 'google',
    evento_id          VARCHAR(300),
    titulo             VARCHAR(250) NOT NULL,
    fecha_limite       DATE         NOT NULL,
    estado             VARCHAR(20)  NOT NULL DEFAULT 'SINCRONIZADO',
    mensaje_error      VARCHAR(500),
    sincronizado_en    TIMESTAMPTZ,
    created_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at         TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT chk_eventos_calendario_estado
        CHECK (estado IN ('SINCRONIZADO', 'PENDIENTE', 'ERROR')),
    CONSTRAINT uq_eventos_calendario_usuario_parcial UNIQUE (usuario_id, parcial_id),
    CONSTRAINT fk_eventos_calendario_usuario FOREIGN KEY (usuario_id)
        REFERENCES usuarios (id) ON DELETE CASCADE,
    CONSTRAINT fk_eventos_calendario_parcial FOREIGN KEY (parcial_id)
        REFERENCES parciales (id) ON DELETE CASCADE
);

CREATE INDEX ix_eventos_calendario_usuario_id ON eventos_calendario (usuario_id);
CREATE INDEX ix_eventos_calendario_parcial_id ON eventos_calendario (parcial_id);
CREATE INDEX ix_eventos_calendario_estado ON eventos_calendario (estado);

CREATE TRIGGER trg_eventos_calendario_updated_at
    BEFORE UPDATE ON eventos_calendario
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();
