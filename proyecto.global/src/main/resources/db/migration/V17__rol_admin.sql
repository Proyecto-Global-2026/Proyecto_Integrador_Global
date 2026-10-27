-- ===========================================================================
-- V17: Rol ADMIN para administracion de seguridad y privacidad (S3-09)
--
-- Los endpoints de retencion (S3-07) y cifrado (S3-08) ya exigian el rol
-- ADMIN, pero el catalogo inicial solo tenia DOCENTE, COORDINADOR y DIRECCION.
-- Se agrega el rol para que esas funciones sean accesibles sin relajar las
-- reglas de acceso a datos personales: ADMIN administra configuracion, no
-- consulta los datos personales de usuarios, planeaciones o evidencias.
-- ===========================================================================

INSERT INTO roles (nombre, descripcion) VALUES
    ('ADMIN', 'Administra la configuracion de seguridad, cifrado y retencion de datos')
ON CONFLICT (nombre) DO NOTHING;
