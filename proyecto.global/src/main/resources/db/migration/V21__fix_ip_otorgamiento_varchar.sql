-- V21: Corregir tipo ip_otorgamiento a VARCHAR(45)
ALTER TABLE proyecto_global.consentimiento_terceros
    ALTER COLUMN ip_otorgamiento TYPE VARCHAR(45)
    USING ip_otorgamiento::text;