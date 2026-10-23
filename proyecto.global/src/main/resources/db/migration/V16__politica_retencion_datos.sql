-- S3-07: Política de retención y minimización de datos personales
-- Añade campos para soporte de anonimización/borrado con trazabilidad

ALTER TABLE usuarios
    ADD COLUMN IF NOT EXISTS anonimizado BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS fecha_anonimizacion TIMESTAMP WITH TIME ZONE,
    ADD COLUMN IF NOT EXISTS motivo_baja VARCHAR(100);

COMMENT ON COLUMN usuarios.anonimizado IS 'Indica si los datos personales han sido anonimizados por política de retención';
COMMENT ON COLUMN usuarios.fecha_anonimizacion IS 'Fecha/hora en que se aplicó la anonimización';
COMMENT ON COLUMN usuarios.motivo_baja IS 'Motivo de baja/anonimización por política de retención';

ALTER TABLE archivos_planeacion
    ADD COLUMN IF NOT EXISTS eliminado_por_retencion BOOLEAN NOT NULL DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS fecha_eliminacion_retencion TIMESTAMP WITH TIME ZONE;

COMMENT ON COLUMN archivos_planeacion.eliminado_por_retencion IS 'Marcado cuando el archivo fue eliminado por proceso automático de retención';
COMMENT ON COLUMN archivos_planeacion.fecha_eliminacion_retencion IS 'Fecha/hora de eliminación por política de retención';
