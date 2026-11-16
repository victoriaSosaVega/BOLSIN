-- Deja los bolsines 500 y 501 como recien cargados por el seed, para volver a
-- probar el CU28: el bolsin en "Enviado", sus remitos en "En Bolsin Enviado" y
-- su documentacion en "En Bolsin Enviado".
--
-- Con el patron State hace falta reiniciar los tres niveles: un remito que ya esta
-- "Recibido y Aceptado" no admite otra recepcion. Por eso restore_demo_statuses.sql
-- (que solo restaura el bolsin 501) ya no alcanza si se corrio el CU28.
--
-- Requiere haber corrido migracion_state.sql. Conserva el historial: cierra el
-- estado vigente y abre uno nuevo, no borra filas. Repetible.
BEGIN;

-- 1. Bolsines -> Enviado
UPDATE cambio_estado_bolsin c
SET fecha_hora_fin = now() AT TIME ZONE 'America/Argentina/Cordoba'
WHERE c.fecha_hora_fin IS NULL
  AND c.id_bolsin IN (SELECT id_bolsin FROM bolsin WHERE numero_bolsin IN (500, 501))
  AND c.id_estado <> (SELECT id_estado FROM estado WHERE nombre = 'Enviado' AND ambito = 'Bolsin');

INSERT INTO cambio_estado_bolsin (id_bolsin, id_estado, fecha_hora_inicio)
SELECT b.id_bolsin,
       (SELECT id_estado FROM estado WHERE nombre = 'Enviado' AND ambito = 'Bolsin'),
       now() AT TIME ZONE 'America/Argentina/Cordoba'
FROM bolsin b
WHERE b.numero_bolsin IN (500, 501)
  AND NOT EXISTS (
      SELECT 1 FROM cambio_estado_bolsin c
      WHERE c.id_bolsin = b.id_bolsin AND c.fecha_hora_fin IS NULL
  );

-- 2. Remitos -> En Bolsin Enviado
UPDATE remito r
SET id_estado = (SELECT id_estado FROM estado WHERE nombre = 'En Bolsin Enviado' AND ambito = 'Remito')
WHERE r.id_bolsin IN (SELECT id_bolsin FROM bolsin WHERE numero_bolsin IN (500, 501));

-- 3. Documentacion de esos remitos -> En Bolsin Enviado
UPDATE cambio_estado_documentacion c
SET fecha_hora_fin = now() AT TIME ZONE 'America/Argentina/Cordoba'
WHERE c.fecha_hora_fin IS NULL
  AND c.id_estado <> (SELECT id_estado FROM estado WHERE nombre = 'En Bolsin Enviado' AND ambito = 'Documentacion')
  AND c.id_documentacion IN (
      SELECT dr.id_documentacion
      FROM detalle_remito dr
      JOIN remito r ON r.id_remito = dr.id_remito
      JOIN bolsin b ON b.id_bolsin = r.id_bolsin
      WHERE b.numero_bolsin IN (500, 501)
  );

INSERT INTO cambio_estado_documentacion (id_documentacion, id_estado, fecha_hora_inicio)
SELECT DISTINCT dr.id_documentacion,
       (SELECT id_estado FROM estado WHERE nombre = 'En Bolsin Enviado' AND ambito = 'Documentacion'),
       now() AT TIME ZONE 'America/Argentina/Cordoba'
FROM detalle_remito dr
JOIN remito r ON r.id_remito = dr.id_remito
JOIN bolsin b ON b.id_bolsin = r.id_bolsin
WHERE b.numero_bolsin IN (500, 501)
  AND NOT EXISTS (
      SELECT 1 FROM cambio_estado_documentacion c
      WHERE c.id_documentacion = dr.id_documentacion AND c.fecha_hora_fin IS NULL
  );

COMMIT;

-- Control
SELECT b.numero_bolsin AS bolsin, eb.nombre AS estado_bolsin, r.numero AS remito,
       er.nombre AS estado_remito, d.numero AS documentacion, ed.nombre AS estado_documentacion
FROM bolsin b
JOIN cambio_estado_bolsin cb ON cb.id_bolsin = b.id_bolsin AND cb.fecha_hora_fin IS NULL
JOIN estado eb ON eb.id_estado = cb.id_estado
JOIN remito r ON r.id_bolsin = b.id_bolsin
LEFT JOIN estado er ON er.id_estado = r.id_estado
JOIN detalle_remito dr ON dr.id_remito = r.id_remito
JOIN documentacion d ON d.id_documentacion = dr.id_documentacion
LEFT JOIN cambio_estado_documentacion cd ON cd.id_documentacion = d.id_documentacion AND cd.fecha_hora_fin IS NULL
LEFT JOIN estado ed ON ed.id_estado = cd.id_estado
WHERE b.numero_bolsin IN (500, 501)
ORDER BY b.numero_bolsin, r.numero, d.numero;
