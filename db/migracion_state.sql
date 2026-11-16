-- Migracion para el patron State del CU28.
-- Correr UNA vez en el SQL Editor de Supabase, despues de seed_cu28.sql (o sobre
-- una base que ya tenia el seed). Es repetible: no duplica nada si se corre de nuevo.
--
-- Que hace:
--   1. Agrega id_empleado (el responsable) a los dos historiales de estado.
--   2. Carga los estados que el modelo necesita y la base no tenia.
--   3. Deja un estado inicial en remitos y documentacion: el codigo viejo lo
--      suponia "En Bolsin Enviado" en memoria. La documentacion lo necesita para
--      el patron State (delega en su estado actual); el remito, que no usa State,
--      lo guarda como dato.
--
-- No borra ni modifica datos existentes: solo completa lo que falta.

BEGIN;

-- 1. Responsable del cambio de estado (ERS CU28 paso 11). Null = cambio anterior
--    a este script, o hecho por otro caso de uso que todavia no lo registra.
ALTER TABLE cambio_estado_bolsin
    ADD COLUMN IF NOT EXISTS id_empleado int4 REFERENCES empleado(id_empleado);
ALTER TABLE cambio_estado_documentacion
    ADD COLUMN IF NOT EXISTS id_empleado int4 REFERENCES empleado(id_empleado);

-- 2. Estados del diagrama de clases. Solo "En Bolsin Enviado" existe en dos ambitos
--    (Remito y Documentacion): son dos filas de la tabla, una por ambito. En el modelo la
--    de Documentacion es la clase EnBolsinEnviado (State) y la de Remito, un Estado simple.
INSERT INTO estado (nombre, descripcion, ambito)
SELECT v.nombre, v.descripcion, v.ambito
FROM (VALUES
    ('Registrada',         'Documentacion registrada en el sistema',          'Documentacion'),
    ('En Remito',          'Documentacion incluida en un remito',             'Documentacion'),
    ('En Bolsin Saliente', 'Documentacion en bolsin saliente',                'Documentacion'),
    ('En Bolsin Enviado',  'Documentacion en bolsin enviado',                 'Documentacion'),
    ('En Bolsin Enviado',  'Remito en bolsin enviado',                        'Remito'),
    ('De Baja',            'Documentacion dada de baja',                      'Documentacion')
) AS v(nombre, descripcion, ambito)
WHERE NOT EXISTS (
    SELECT 1 FROM estado e WHERE e.nombre = v.nombre AND e.ambito = v.ambito
);

-- 3a. Remitos sin estado cuyo bolsin sigue Enviado -> "En Bolsin Enviado".
--     (Los remitos que ya pasaron por el CU28 tienen estado y no se tocan.)
UPDATE remito r
SET id_estado = (SELECT e.id_estado FROM estado e
                 WHERE e.nombre = 'En Bolsin Enviado' AND e.ambito = 'Remito')
WHERE r.id_estado IS NULL
  AND EXISTS (
      SELECT 1
      FROM cambio_estado_bolsin c
      JOIN estado eb ON eb.id_estado = c.id_estado
      WHERE c.id_bolsin = r.id_bolsin
        AND c.fecha_hora_fin IS NULL
        AND eb.nombre = 'Enviado' AND eb.ambito = 'Bolsin'
  );

-- 3b. Documentacion de esos remitos que no tiene ningun historial -> "En Bolsin
--     Enviado" vigente (sin fecha_hora_fin). La que ya tiene historial no se toca.
INSERT INTO cambio_estado_documentacion (id_documentacion, id_estado, fecha_hora_inicio)
SELECT DISTINCT d.id_documentacion,
       (SELECT e.id_estado FROM estado e
        WHERE e.nombre = 'En Bolsin Enviado' AND e.ambito = 'Documentacion'),
       now() AT TIME ZONE 'America/Argentina/Cordoba'
FROM documentacion d
JOIN detalle_remito dr ON dr.id_documentacion = d.id_documentacion
JOIN remito r          ON r.id_remito = dr.id_remito
JOIN estado er         ON er.id_estado = r.id_estado
                      AND er.nombre = 'En Bolsin Enviado' AND er.ambito = 'Remito'
WHERE NOT EXISTS (
    SELECT 1 FROM cambio_estado_documentacion c WHERE c.id_documentacion = d.id_documentacion
);

COMMIT;

-- Control: cada remito y cada documentacion deberia tener estado.
-- Despues del seed, sin haber corrido el CU28: remitos y documentacion en "En Bolsin Enviado".
SELECT 'remito' AS entidad, r.numero AS numero, COALESCE(e.nombre, '(SIN ESTADO)') AS estado
FROM remito r LEFT JOIN estado e ON e.id_estado = r.id_estado
UNION ALL
SELECT 'documentacion', d.numero, COALESCE(e.nombre, '(SIN ESTADO)')
FROM documentacion d
LEFT JOIN cambio_estado_documentacion c ON c.id_documentacion = d.id_documentacion AND c.fecha_hora_fin IS NULL
LEFT JOIN estado e ON e.id_estado = c.id_estado
ORDER BY 1, 2;
