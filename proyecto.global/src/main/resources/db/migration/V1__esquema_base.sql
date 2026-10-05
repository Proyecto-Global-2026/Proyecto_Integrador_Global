-- ===========================================================================
-- V1: Esquema base
-- Solo contiene elementos transversales. Las tablas de negocio (usuarios,
-- planeaciones, etc.) se crean en la migracion propia de cada funcionalidad.
-- ===========================================================================

CREATE EXTENSION IF NOT EXISTS pgcrypto;
CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE OR REPLACE FUNCTION set_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

COMMENT ON FUNCTION set_updated_at() IS
    'Trigger comun para mantener updated_at en tablas de negocio.';