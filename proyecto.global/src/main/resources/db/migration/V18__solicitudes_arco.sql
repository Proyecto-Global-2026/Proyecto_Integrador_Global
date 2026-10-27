-- V18: Solicitudes ARCO (Acceso, Rectificacion, Cancelacion, Oposicion)
CREATE TABLE IF NOT EXISTS solicitudes_arco (
    id              UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    usuario_id      UUID         NOT NULL,
    tipo            VARCHAR(20)  NOT NULL,
    estado          VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    motivo          VARCHAR(500),
    detalles        TEXT,
    respuesta       TEXT,
    atendido_por_id UUID,
    fecha_solicitud TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    fecha_atencion  TIMESTAMPTZ,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT fk_solicitudes_arco_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id),
    CONSTRAINT fk_solicitudes_arco_atendido_por FOREIGN KEY (atendido_por_id) REFERENCES usuarios (id),
    CONSTRAINT chk_solicitudes_arco_tipo CHECK (tipo IN ('ACCESO','RECTIFICACION','CANCELACION','OPOSICION')),
    CONSTRAINT chk_solicitudes_arco_estado CHECK (estado IN ('PENDIENTE','EN_PROCESO','ATENDIDA','RECHAZADA'))
);

CREATE INDEX IF NOT EXISTS idx_solicitudes_arco_usuario ON solicitudes_arco (usuario_id);
CREATE INDEX IF NOT EXISTS idx_solicitudes_arco_tipo ON solicitudes_arco (tipo);
CREATE INDEX IF NOT EXISTS idx_solicitudes_arco_estado ON solicitudes_arco (estado);
