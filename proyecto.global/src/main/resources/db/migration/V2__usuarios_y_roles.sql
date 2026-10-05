-- ===========================================================================
-- V2: Usuarios y roles
-- ===========================================================================

CREATE TABLE roles (
    id          SMALLSERIAL PRIMARY KEY,
    nombre      VARCHAR(50)  NOT NULL,
    descripcion VARCHAR(255),
    CONSTRAINT uq_roles_nombre UNIQUE (nombre)
);

CREATE TABLE usuarios (
    id           UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    nombre       VARCHAR(120) NOT NULL,
    email        VARCHAR(180) NOT NULL,
    password     VARCHAR(100) NOT NULL,
    rol_id       SMALLINT     NOT NULL,
    activo       BOOLEAN      NOT NULL DEFAULT TRUE,
    proveedor    VARCHAR(50),
    proveedor_id VARCHAR(180),
    created_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    CONSTRAINT uq_usuarios_email UNIQUE (email),
    CONSTRAINT uq_usuarios_proveedor UNIQUE (proveedor, proveedor_id),
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles (id)
);

-- El correo se normaliza a minusculas en la aplicacion; este indice
-- refuerza la unicidad ante consultas directas a la base.
CREATE UNIQUE INDEX uq_usuarios_email_lower ON usuarios (LOWER(email));
CREATE INDEX ix_usuarios_rol_id ON usuarios (rol_id);
CREATE INDEX ix_usuarios_nombre_trgm ON usuarios USING gin (nombre gin_trgm_ops);

CREATE TRIGGER trg_usuarios_updated_at
    BEFORE UPDATE ON usuarios
    FOR EACH ROW
    EXECUTE FUNCTION set_updated_at();