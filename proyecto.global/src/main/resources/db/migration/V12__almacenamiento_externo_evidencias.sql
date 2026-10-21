ALTER TABLE proyecto_global.evidencias
	ADD COLUMN IF NOT EXISTS proveedor_almacenamiento VARCHAR(30) DEFAULT 'local',
	ADD COLUMN IF NOT EXISTS public_id VARCHAR(255),
	ADD COLUMN IF NOT EXISTS resource_type VARCHAR(30);

UPDATE proyecto_global.evidencias
	SET proveedor_almacenamiento = 'local'
	WHERE proveedor_almacenamiento IS NULL;