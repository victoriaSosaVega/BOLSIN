# CU28 - Registrar Recepción de Bolsín

Implementación en Java del CU28 del PPAI "Bolsines" (UTN DSI), siguiendo el
diagrama de clases y el diagrama de secuencia ya corregidos, con una interfaz
gráfica (Swing) para que el Encargado de Bolsines (EB) lo pruebe interactivamente.

## Cómo correrlo

Necesita un **JDK 17 o superior** (el código usa sintaxis moderna de Java; con
Java 8 no corre) y el driver JDBC de Postgres en `lib/postgresql.jar`
(`postgresql-42.7.4`, de Maven Central; no se versiona en git).

1. **Datos de prueba:** correr una sola vez `db/seed_cu28.sql` en el SQL Editor
   de Supabase, sobre las tablas vacías.
2. **Conexión:** copiar `.env.example` como `.env` en la raíz del proyecto y
   completar los tres valores con los datos de Supabase (botón *Connect* del
   proyecto, método *Session pooler*). El `.env` está en `.gitignore`: no se
   sube a git.

```
SUPABASE_DB_URL=jdbc:postgresql://HOST:5432/postgres?sslmode=require
SUPABASE_DB_USER=postgres.<ref-del-proyecto>
SUPABASE_DB_PASSWORD=<contraseña de la base>
```

   La URL tiene que ser formato JDBC (`jdbc:postgresql://...`), sin usuario ni
   contraseña adentro. Las variables de entorno del sistema, si existen, tienen
   prioridad sobre el `.env`. La contraseña es la de la base de datos (no es
   ninguna de las API keys); se restablece en *Project Settings → Database*.
3. **Compilar y correr**, siempre parado en la raíz del proyecto (ahí se busca
   el `.env`). En Windows el classpath se separa con `;`:

```bash
javac -d out -encoding UTF-8 $(find src -name "*.java")
java -cp "out;lib/postgresql.jar" app.MainCU28
```

   (En Linux/Mac: `-cp "out:lib/postgresql.jar"`.) Si el `java` del PATH es
   anterior a 17, usar la ruta completa de un JDK nuevo.

Si falla algo al arrancar (falta una variable, no hay conexión o no hay una
sesión abierta en la tabla `sesion`), se muestra un cartel con el motivo.

Se abre un menú del Encargado de Bolsines. La única opción habilitada es
**"1. Registrar recepción de bolsín"** (las otras tres son de otros casos de
uso del ERS que no se implementaron acá). Al elegirla arranca el CU28.

## Estructura del código

```
src/
  modelo/    Entidades: CM, Empleado, Usuario, Sesion, Estado,
             CambioEstadoBolsin, CambioEstadoDocumentacion, TipoDocumentacion,
             Documentacion, DetalleRemito, Remito, Bolsin
  control/   GestorRecepcionDeBolsin (el "cerebro" del CU28)
  boundary/  PantallaPrincipalEncargadoBolsines, PantallaRecepcionDeBolsin,
             PantallaNotificacionRecepcionBolsin
  persistencia/  Conexion, CargadorDeDatos, RepositorioRecepcion (JDBC)
  app/       MainCU28 (carga los datos de la base y arranca la ventana)
db/          seed_cu28.sql (datos de prueba)
```

Los datos viven en una base Postgres (Supabase), accedida con JDBC puro. El
diagrama de secuencia del CU28 no cambia: al arrancar, `CargadorDeDatos` lee
las tablas y arma los objetos del modelo (las listas estáticas de `Bolsin` y
`Estado` se llenan desde la base), y el flujo sigue operando en memoria tal
cual el diagrama. Al confirmar, `GestorRecepcionDeBolsin.guardarCambios()`
guarda en una sola transacción los cambios que dejó el CU (nuevos
`CambioEstado`, cierre de los anteriores y estado de cada remito). Esa llamada
es infraestructura: no es un mensaje del diagrama de secuencia. Si el guardado
falla, el CU se corta (no se notifica a la CM origen) y la pantalla muestra el
error; como los objetos en memoria ya habían cambiado, hay que reiniciar la app.

La base ya tiene el campo `remito.id_estado` (Remito → Estado del diagrama de
clases). Las columnas `id_cm_origen` / `id_cm_destino` de `remito` y
`id_cm_origen` de `documentacion` vienen de otros casos de uso del ERS (cada
documentación sale de una CM; un remito va dirigido a una CM destino): el CU28
no las usa y el seed las completa.

Las columnas `timestamp` no guardan zona horaria. Java escribe la hora local,
por eso el seed también usa hora argentina.

## Flujo implementado según el diagrama de secuencia

1. El EB inicia “Registrar recepción de bolsín”.
2. El gestor obtiene el empleado de la sesión y la pantalla muestra su CM.
3. Se buscan los bolsines en estado **Enviado** cuyo destino sea esa CM.
4. El EB selecciona un bolsín y la pantalla muestra sus remitos, asuntos y tipos
   de documentación.
5. Para el flujo dibujado, el contenido coincide con lo registrado; el EB
   confirma la recepción.
6. El sistema obtiene una única fecha y hora y actualiza el bolsín a
   **Recibido en CM Destino**, cada remito a **Recibido y Aceptado** y cada
   documento a **Recibida y Aceptada**.
7. Se llama al CU29 para notificar a la CM de origen y finaliza el CU28.

La implementación sigue este recorrido principal del diagrama. Las variantes
A3/A4/A5 y la cancelación no forman parte de esta secuencia y no se ofrecen en
esta pantalla.

## Qué NO está implementado (a propósito)

El CU28 original también menciona estos dos casos, que decidimos **no**
agregar por ahora (quedan pendientes si los necesitás más adelante):

- **Buscar un bolsín por número de precinto** y que avise si no lo encuentra.
- **Buscar un bolsín por CM de origen** y que avise si no lo encuentra.

Hoy el sistema solo lista todos los bolsines en estado Enviado para la CM del
usuario logueado, sin filtro adicional por precinto o CM origen.

## Notas de diseño

- `PantallaNotificacionRecepcionBolsin` simula el envío de un mail a la CM
  origen (en la vida real sería una integración con un servidor de correo,
  no una pantalla que ve un usuario — así lo dice el ERS para el CU29).
