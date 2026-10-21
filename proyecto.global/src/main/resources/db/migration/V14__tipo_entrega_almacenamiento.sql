ALTER TABLE proyecto_global.evidencias
	ADD COLUMN IF NOT EXISTS tipo_entrega VARCHAR(30) DEFAULT 'upload';

ALTER TABLE proyecto_global.archivos_planeacion
	ADD COLUMN IF NOT EXISTS tipo_entrega VARCHAR(30) DEFAULT 'upload';

UPDATE proyecto_global.evidencias
	SET tipo_entrega = 'upload'
	WHERE tipo_entrega IS NULL;

UPDATE proyecto_global.archivos_planeacion
	SET tipo_entrega = 'upload'
	WHERE tipo_entrega IS NULL;
