-- Script para actualizar los nombres de roles a MAYÚSCULAS
-- Esto permitirá que @PreAuthorize funcione correctamente

UPDATE ROL SET NOMBRE_ROL = 'ADMIN' WHERE ID_ROL = 1;
UPDATE ROL SET NOMBRE_ROL = 'TRABAJADOR' WHERE ID_ROL = 2;
UPDATE ROL SET NOMBRE_ROL = 'CLIENTE' WHERE ID_ROL = 3;

COMMIT;

-- Verificar los cambios
SELECT * FROM ROL ORDER BY ID_ROL;
