-- ===========================================================================
-- V3: Seed inicial de roles
-- Los tres roles del sistema segun la historia de usuario S1-02.
-- ===========================================================================

INSERT INTO roles (nombre, descripcion) VALUES
    ('DOCENTE',      'Registra y gestiona sus propias planeaciones'),
    ('COORDINADOR',  'Revisa, aprueba o rechaza las planeaciones enviadas'),
    ('DIRECCION',    'Consulta el historial y los indicadores del sistema')
ON CONFLICT (nombre) DO NOTHING;