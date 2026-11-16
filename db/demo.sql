-- Solo para una base de demostración vacía.
-- Estados
INSERT INTO estado (nombre, descripcion, ambito) VALUES
  ('Enviado',                     'Bolsin enviado por CM origen',                         'Bolsin'),
  ('Recibido en CM Destino',      'Bolsin recibido en CM destino',                        'Bolsin'),
  ('Recibido y Aceptado',         'Remito recibido y aceptado',                           'Remito'),
  ('Recibido y Aceptado Parcial', 'Remito con documentacion faltante',                    'Remito'),
  ('Recibida y Aceptada',         'Documentacion recibida y aceptada',                    'Documentacion'),
  ('Recibida y Rechazada',        'Documentacion que no corresponde a la CM destino',     'Documentacion'),
  ('No Recibida',                 'Documentacion que no llego en el bolsin',              'Documentacion'),
  ('Para Redirigir',              'Documentacion a redirigir a otra area de la CMC',      'Documentacion');

-- CMs
INSERT INTO cm (codigo, nombre, direccion, email, telefono) VALUES
  ('CBA', 'CM Cordoba',     'Bv. San Juan 1000', 'cba@organismo.gob.ar', '351-0000000'),
  ('VM',  'CM Villa Maria', 'Av. Libertad 500',  'vm@organismo.gob.ar',  '353-1111111');

-- Empleado logueado (CM Villa Maria), su usuario y su sesion abierta
INSERT INTO empleado (nombre, apellido, email, id_cm) VALUES
  ('Ana', 'Perez', 'ana.perez@organismo.gob.ar', (SELECT id_cm FROM cm WHERE codigo = 'VM'));

INSERT INTO usuario (nombre, contrasena, id_empleado) VALUES
  ('aperez', '****', (SELECT id_empleado FROM empleado WHERE email = 'ana.perez@organismo.gob.ar'));

-- Las columnas timestamp no guardan zona horaria y Java escribe la hora local, asi
-- que el seed tambien usa hora argentina (now() de Postgres seria UTC, 3 h menos).
INSERT INTO sesion (fecha_hora_inicio, id_usuario) VALUES
  (CURRENT_TIMESTAMP, (SELECT id_usuario FROM usuario WHERE nombre = 'aperez'));

-- Tipo de documentacion y documentacion (origen: CM Cordoba)
INSERT INTO tipo_documentacion (nombre, descripcion) VALUES
  ('Expediente', 'Expediente administrativo');

INSERT INTO documentacion (numero, asunto, fecha_pase, id_tipo_doc, id_cm_origen) VALUES
  (1, 'Solicitud de estudio medico', current_date - 5,
      (SELECT id_tipo_doc FROM tipo_documentacion WHERE nombre = 'Expediente'),
      (SELECT id_cm FROM cm WHERE codigo = 'CBA')),
  (2, 'Dictamen preliminar',         current_date - 5,
      (SELECT id_tipo_doc FROM tipo_documentacion WHERE nombre = 'Expediente'),
      (SELECT id_cm FROM cm WHERE codigo = 'CBA')),
  (3, 'Historia clinica',            current_date - 3,
      (SELECT id_tipo_doc FROM tipo_documentacion WHERE nombre = 'Expediente'),
      (SELECT id_cm FROM cm WHERE codigo = 'CBA'));

-- Bolsines (de CM Cordoba a CM Villa Maria)
INSERT INTO bolsin (numero_bolsin, numero_precinto, fecha, peso, id_cm_origen, id_cm_destino) VALUES
  (500, 'PRE-2026-01', current_date - 1, 350.0,
      (SELECT id_cm FROM cm WHERE codigo = 'CBA'), (SELECT id_cm FROM cm WHERE codigo = 'VM')),
  (501, 'PRE-2026-02', current_date,     120.0,
      (SELECT id_cm FROM cm WHERE codigo = 'CBA'), (SELECT id_cm FROM cm WHERE codigo = 'VM'));

-- Remitos (sin estado: lo asigna el CU28 al registrar la recepcion)
INSERT INTO remito (numero, fecha, id_bolsin, id_cm_origen, id_cm_destino) VALUES
  (1001, current_date - 2, (SELECT id_bolsin FROM bolsin WHERE numero_bolsin = 500),
      (SELECT id_cm FROM cm WHERE codigo = 'CBA'), (SELECT id_cm FROM cm WHERE codigo = 'VM')),
  (1002, current_date - 1, (SELECT id_bolsin FROM bolsin WHERE numero_bolsin = 501),
      (SELECT id_cm FROM cm WHERE codigo = 'CBA'), (SELECT id_cm FROM cm WHERE codigo = 'VM'));

INSERT INTO detalle_remito (id_remito, id_documentacion, area_cmc_destino) VALUES
  ((SELECT id_remito FROM remito WHERE numero = 1001), (SELECT id_documentacion FROM documentacion WHERE numero = 1), 'Recepcion'),
  ((SELECT id_remito FROM remito WHERE numero = 1001), (SELECT id_documentacion FROM documentacion WHERE numero = 2), 'Recepcion'),
  ((SELECT id_remito FROM remito WHERE numero = 1002), (SELECT id_documentacion FROM documentacion WHERE numero = 3), 'Recepcion');

-- Los dos bolsines arrancan en estado Enviado (cambio de estado actual: sin fecha_hora_fin)
INSERT INTO cambio_estado_bolsin (id_bolsin, id_estado, fecha_hora_inicio)
SELECT b.id_bolsin, e.id_estado, CURRENT_TIMESTAMP
FROM bolsin b, estado e
WHERE b.numero_bolsin IN (500, 501) AND e.nombre = 'Enviado' AND e.ambito = 'Bolsin';

INSERT INTO estado (nombre, descripcion, ambito) VALUES
('Registrada', 'Registrada', 'Documentacion'), ('En Remito', 'En Remito', 'Documentacion'),
('En Bolsin Saliente', 'En Bolsin Saliente', 'Documentacion'), ('En Bolsin Enviado', 'En Bolsin Enviado', 'Documentacion'),
('De Baja', 'De Baja', 'Documentacion'), ('En Bolsin Enviado', 'En Bolsin Enviado', 'Remito');
UPDATE remito SET id_estado = (SELECT id_estado FROM estado WHERE nombre = 'En Bolsin Enviado' AND ambito = 'Remito');
INSERT INTO cambio_estado_documentacion (id_documentacion, id_estado, fecha_hora_inicio, id_empleado)
SELECT d.id_documentacion, e.id_estado, CURRENT_TIMESTAMP, (SELECT id_empleado FROM empleado WHERE email = 'ana.perez@organismo.gob.ar')
FROM documentacion d CROSS JOIN estado e WHERE e.nombre = 'En Bolsin Enviado' AND e.ambito = 'Documentacion';
UPDATE cambio_estado_bolsin SET id_empleado = (SELECT id_empleado FROM empleado WHERE email = 'ana.perez@organismo.gob.ar');
