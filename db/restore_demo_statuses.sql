-- Restaura el bolsín 501 a Enviado si se aplicó demo_varied_statuses.sql.
-- Conserva el historial: cierra el estado actual e inserta el nuevo solo si hace falta.
BEGIN;

WITH objetivo AS (
    SELECT b.id_bolsin, e.id_estado AS id_estado_enviado,
           now() AT TIME ZONE 'America/Argentina/Cordoba' AS momento
    FROM bolsin b
    JOIN estado e ON e.nombre = 'Enviado' AND e.ambito = 'Bolsin'
    WHERE b.numero_bolsin = 501
), cerrar_estado_actual AS (
    UPDATE cambio_estado_bolsin c
    SET fecha_hora_fin = o.momento
    FROM objetivo o
    WHERE c.id_bolsin = o.id_bolsin
      AND c.fecha_hora_fin IS NULL
      AND c.id_estado <> o.id_estado_enviado
    RETURNING c.id_bolsin
)
INSERT INTO cambio_estado_bolsin (id_bolsin, id_estado, fecha_hora_inicio)
SELECT o.id_bolsin, o.id_estado_enviado, o.momento
FROM objetivo o
WHERE NOT EXISTS (
    SELECT 1 FROM cambio_estado_bolsin c
    WHERE c.id_bolsin = o.id_bolsin
      AND c.id_estado = o.id_estado_enviado
      AND c.fecha_hora_fin IS NULL
);

COMMIT;
