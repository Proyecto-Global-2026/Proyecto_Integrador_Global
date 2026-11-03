CREATE TABLE IF NOT EXISTS auditoria_eventos (
    id UUID PRIMARY KEY,
    usuario_id UUID,
    rol VARCHAR(50),
    usuario_email_mask VARCHAR(255),
    accion VARCHAR(50) NOT NULL,
    resultado VARCHAR(50) NOT NULL,
    recurso VARCHAR(100),
    recurso_id UUID,
    descripcion TEXT,
    metodo VARCHAR(10),
    ruta VARCHAR(500),
    ip VARCHAR(100),
    user_agent VARCHAR(500),
    fecha_evento TIMESTAMP WITH TIME ZONE NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_auditoria_eventos_fecha ON auditoria_eventos(fecha_evento);
CREATE INDEX IF NOT EXISTS idx_auditoria_eventos_usuario ON auditoria_eventos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_auditoria_eventos_recurso ON auditoria_eventos(recurso);
