# CU28 - Registrar Recepción de Bolsín

Implementación en Java del CU28 del PPAI "Bolsines" (UTN DSI), siguiendo el
diagrama de clases (con el **patrón State aplicado solo a la Documentación**) y
el diagrama de secuencia de dos páginas (`PPAI Realización CU 28 - Codigo.drawio`), con una interfaz gráfica
(Swing) para que el Encargado de Bolsines (EB) lo pruebe interactivamente.

Dónde está cada cosa:

| Documento | Para qué |
|---|---|
| `CU_28_Diagrama_de_clases_v2.png` (`.drawio`, `.puml`) | Diagrama de clases vigente |
| `PPAI Realización CU 28 - Codigo.drawio` | Diagrama de secuencia (2 páginas) |
| [`docs/Cambios_CU28_Patron_State.pdf`](docs/Cambios_CU28_Patron_State.pdf) | Qué se cambió y en qué concuerda con cada diagrama |
| `2026 DSI Bolsines ERS V1-2.pdf` | ERS (CU28: Registrar recepción de bolsín) |

## Cómo correrlo

Necesita un **JDK 17 o superior** (el código usa sintaxis moderna de Java; con
Java 8 no corre) y el driver JDBC de Postgres en `lib/postgresql.jar`
(`postgresql-42.7.4`, de Maven Central; no se versiona en git).

1. **Datos de prueba:** correr una sola vez `db/seed_cu28.sql` en el SQL Editor
   de Supabase, sobre las tablas vacías, y después `db/migracion_state.sql`
   (agrega el responsable a los historiales de estado, los estados que faltaban
   y deja un estado inicial en remitos y documentación). La migración es
   repetible y **hay que correrla antes de abrir la app**: sin ella, la documentación
   no tiene estado (el State delega en él) y el CU28 corta con un mensaje que lo
   indica. Para volver a probar el CU28 sobre los mismos bolsines, correr
   `db/reiniciar_demo_cu28.sql` (una documentación ya recibida no admite otra
   recepción; `db/restore_demo_statuses.sql` solo restaura el bolsín y ya no
   alcanza).
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
  modelo/    Entidades: CM, Empleado, Usuario, Sesion, CambioEstadoBolsin,
             CambioEstadoDocumentacion, TipoDocumentacion, Documentacion,
             DetalleRemito, Remito, Bolsin
             Estado (clase simple: estados de Bolsin y Remito)
             Patron State (solo Documentacion): EstadoDocumentacion
             (abstracta, hereda de Estado) y sus subclases Registrada,
             EnRemito, EnBolsinSaliente, EnBolsinEnviado, RecibidayAceptada,
             RecibidaYRechazada, NoRecibida, ParaRedirigir, DeBaja
  control/   GestorRecepcionDeBolsin (el "cerebro" del CU28)
  boundary/  PantallaPrincipalEncargadoBolsines, PantallaRecepcionDeBolsin,
             PantallaNotificacionRecepcionBolsin
  persistencia/  Conexion, CargadorDeDatos, FabricaDeEstados, RepositorioRecepcion (JDBC)
  app/       MainCU28 (carga los datos de la base y arranca la ventana)
db/          seed_cu28.sql (datos de prueba), migracion_state.sql,
             reiniciar_demo_cu28.sql
```

## Patrón State (solo en Documentación)

`EstadoDocumentacion` es abstracta (hereda de `Estado`) y cada estado concreto de una
documentación es una subclase que implementa solo las transiciones que permite;
cualquier otra lanza `IllegalStateException`. `Documentacion` tiene un atributo
`estado` y **delega** en él (`estado.marcarRecibidayAceptada(this, …)`); el estado crea
el estado siguiente con `crearEstado()` y registra el cambio en el historial
(`CambioEstadoDocumentacion`). Los estados no guardan referencia a la documentación:
la reciben por parámetro.

`Bolsin` y `Remito` **no usan State**: su estado es un `Estado` simple (nombre,
descripción, ámbito). El bolsín cierra su `CambioEstadoBolsin` vigente y crea el
siguiente; el remito se asigna el estado *Recibido y Aceptado* y marca su
documentación. Como en el diagrama de secuencia original (sin patrón), el Gestor
busca esos dos estados recorriendo `Estado.getEstados()` y se los pasa a
`Bolsin.marcarRecibidoEnCMDestino(...)` y, de ahí, a cada `Remito`. El estado de la
documentación no se busca: lo crea el State.

| Estado de Documentación | Transiciones → siguiente estado |
|---|---|
| `Registrada` | `incluirEnRemito` → `EnRemito` |
| `EnRemito` | `incluirEnBolsin` → `EnBolsinSaliente`; `quitarDeRemito` → `Registrada` |
| `EnBolsinSaliente` | `quitarDeBolsin` → `EnRemito`; `retirar` → `EnBolsinEnviado` |
| `EnBolsinEnviado` | `marcarRecibidayAceptada` → `RecibidayAceptada` **(CU28)**; `marcarNoRecibida` → `NoRecibida`; `marcarRecibidayRechazada` → `RecibidaYRechazada`; `redirigir` → `ParaRedirigir` |
| `NoRecibida` | `reenviar` → `Registrada`; `darDeBaja` → `DeBaja` |
| `RecibidaYRechazada` | `reenviar` → `Registrada` |
| `ParaRedirigir` | `marcarRecibidayRechazada` → `RecibidaYRechazada`; `marcarRecibidayAceptada` → `RecibidayAceptada` |
| `RecibidayAceptada`, `DeBaja` | finales |

Las transiciones que no son del CU28 las usan otros casos de uso del ERS; la
pantalla del CU28 no las ofrece.

**Empleado responsable.** El ERS (CU28, paso 11) pide registrar el empleado
responsable del cambio de bolsín y de documentación. El `empleadoLogueado` del
Gestor viaja por `Bolsin` → `Remito` → `DetalleRemito` →
`Documentacion` → `EnBolsinEnviado` y queda guardado en `CambioEstadoBolsin` y
`CambioEstadoDocumentacion` (columna `id_empleado`; null en los cambios
anteriores a la migración).

## Datos y persistencia

Los datos viven en una base Postgres (Supabase), accedida con JDBC puro. Al
arrancar, `CargadorDeDatos` lee
las tablas y arma los objetos del modelo (la lista estática de `Bolsin` se llena
desde la base; cada fila de `estado` se arma con `FabricaDeEstados`: un `Estado`
simple para Bolsín y Remito, la subclase concreta de `EstadoDocumentacion` para
Documentación). El flujo sigue operando en memoria tal cual el diagrama de
secuencia. Al confirmar, `GestorRecepcionDeBolsin.guardarCambios()`
guarda en una sola transacción los cambios que dejó el CU (nuevos
`CambioEstado` con su empleado responsable, cierre de los anteriores y estado
de cada remito). Los estados creados por `crearEstado()` no traen el id de la
base: se resuelve por nombre y ámbito al guardar. Esa llamada
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
6. El Gestor busca los estados *Recibido en CM Destino* (bolsín) y *Recibido y
   Aceptado* (remito). El sistema obtiene una única fecha y hora y actualiza el bolsín a
   **Recibido en CM Destino**, cada remito a **Recibido y Aceptado** y cada
   documento a **Recibida y Aceptada**, registrando el empleado responsable en
   el bolsín y en la documentación. El bolsín cierra su cambio de estado vigente
   y crea el siguiente; la documentación lo hace su estado `EnBolsinEnviado`
   (`crearEstado`, `crearCambioEstado`, `setEstado`, `agregarCambioEstado`).
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

## Estado de la verificación

- **Verificado:** compila (JDK 17+); el modelo se probó en memoria (flujo del
  CU28 y todas las transiciones de documentación); `db/migracion_state.sql` se
  corrió en Supabase y dejó remitos y documentación en *En Bolsin Enviado*.
- **Sin verificar:** la app completa contra Supabase (`CargadorDeDatos` y
  `RepositorioRecepcion` cambiaron), `db/reiniciar_demo_cu28.sql`, y el
  `.puml` renderizado con PlantUML.
- **Pendiente:** abrir los `.drawio` en draw.io y acomodar lo que haga falta (la
  página 2 del diagrama de secuencia se regeneró; la revisé con una vista previa
  propia, no en draw.io).
