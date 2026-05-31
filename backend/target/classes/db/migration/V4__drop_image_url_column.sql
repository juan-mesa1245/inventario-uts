-- V4: Eliminar columna image_url de la tabla assets
ALTER TABLE assets DROP COLUMN IF EXISTS image_url;
