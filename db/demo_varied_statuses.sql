-- Datos de demostración CU28: deja el bolsín 500 Enviado y el 501
-- Recibido en CM Destino, para que la búsqueda liste solo el 500.
-- Ejecutar después de seed_cu28.sql. Repetible: no duplica el estado actual.
BEGIN;

WITH objetivo AS (
    SELECT b.id_bolsin, e.id_estado AS id_estado_recibido,
           now() AT TIME ZONE 'America/Argentina/Cordoba' AS momento
    FROM bolsin b
    JOIN estado e ON e.nombre = 'Recibido en CM Destino' AND e.ambito = 'Bolsin'
    WHERE b.numero_bolsin = 501
), cerrar_estado_actual AS (
    UPDATE cambio_estado_bolsin c
    SET fecha_hora_fin = o.momento
    FROM objetivo o
    WHERE c.id_bolsin = o.id_bolsin
      AND c.fecha_hora_fin IS NULL
      AND c.id_estado <> o.id_estado_recibido
    RETURNING c.id_bolsin
)
INSERT INTO cambio_estado_bolsin (id_bolsin, id_estado, fecha_hora_inicio)
SELECT o.id_bolsin, o.id_estado_recibido, o.momento
FROM objetivo o
WHERE NOT EXISTS (
    SELECT 1 FROM cambio_estado_bolsin c
    WHERE c.id_bolsin = o.id_bolsin
      AND c.id_estado = o.id_estado_recibido
      AND c.fecha_hora_fin IS NULL
);

COMMIT;
