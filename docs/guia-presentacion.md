# Guía de ejecución y presentación — Bolsines CU28

Esta guía permite preparar el equipo, iniciar la aplicación y demostrar la resolución del **CU28: Registrar recepción de bolsín**. Los comandos se ejecutan desde la raíz del proyecto, donde están `pom.xml`, `src/`, `web/` y `scripts/`.

## 1. Qué necesitamos

| Elemento | Para qué se utiliza |
|---|---|
| JDK 17 o posterior | Compilar y ejecutar el backend Java. Debe incluir `javac`; no alcanza un JRE. |
| Maven | Descargar los drivers de base de datos en una instalación nueva. |
| Bash 4 o posterior | Ejecutar los scripts de compilación y pruebas. En Linux suele estar disponible. |
| Navegador | Utilizar la interfaz web en `localhost`. |

Para la presentación local no necesitamos Node.js, una cuenta de Supabase ni conexión a Internet **una vez descargadas las dependencias**. El mismo programa Java sirve la web y accede a la base local: no hay que iniciar frontend y backend por separado.

## 2. Preparar el equipo antes de la presentación

### 2.1. Abrir una terminal en el proyecto

En el equipo actual:

```bash
cd /home/salvador/Universidad/DSI/PPAI/BOLSIN
```

En otra computadora, reemplazar esa ruta por la ubicación donde se clonó o copió el repositorio.

### 2.2. Comprobar Java y Maven

```bash
java -version
javac -version
mvn -version
```

Java y `javac` deben ser versión 17 o posterior. Maven también debe utilizar un JDK compatible.

Si el JDK está instalado pero no aparece en la terminal, configurar su ubicación. Por ejemplo, en **este equipo**:

```bash
export JAVA_HOME=/home/salvador/.jdks/openjdk-27.0.1.0.3
export PATH="$JAVA_HOME/bin:$PATH"
```

En otra computadora usar la ruta de su propio JDK. Los scripts intentan detectar instalaciones habituales, pero configurar `JAVA_HOME` también permite ejecutar Maven y los comandos Java manuales.

### 2.3. Descargar las dependencias

Hacerlo con Internet, antes del día de la defensa:

```bash
mvn dependency:copy-dependencies -DoutputDirectory=lib
```

Esto descarga los drivers H2 y PostgreSQL en `lib/`. Esa carpeta no se sube a Git, por lo que una copia recién clonada necesita este paso. Si los drivers ya están en `lib/`, no es necesario descargarlos en cada ejecución.

### 2.4. Verificar el proyecto

```bash
bash scripts/test.sh
```

El script compila el código en `target/classes`, las pruebas en `target/test-classes` y ejecuta las suites. Al finalizar deben aparecer estas tres líneas, además de los mensajes de notificación simulada:

```text
OK: 47 verificaciones de CU28, State y persistencia JDBC.
OK: 23 verificaciones HTTP de la aplicación web local.
OK: 11 verificaciones de datos de ejemplo e idempotencia.
```

**Total: 81 verificaciones.** Las pruebas usan bases aisladas y no consumen los bolsines de la demostración. No dejan el servidor de la aplicación iniciado; hay que arrancarlo en el paso siguiente. El comando de pruebas es este script, no `mvn test`.

## 3. Iniciar la aplicación

### Opción A: continuar con los datos existentes

```bash
bash scripts/run.sh
```

El comando compila e inicia el servidor. Esperar el mensaje:

```text
Bolsines web: http://localhost:8080
Ctrl+C para detener. Los datos quedan guardados en la base.
```

Abrir **[http://localhost:8080](http://localhost:8080)** en el navegador. Mantener la terminal abierta durante toda la demostración.

La base utilizada es `data/cu28.mv.db`. Si ya recibimos algunos bolsines en ensayos anteriores, no volverán a aparecer como enviados. Es el comportamiento esperado de la persistencia.

### Opción B: comenzar la presentación con ejemplos sin usar

Esta opción permite empezar con una base nueva **sin borrar las recepciones anteriores**. Antes de usarla, haber compilado mediante `bash scripts/test.sh` y detener cualquier servidor previo con `Ctrl+C`.

En Bash, desde la raíz del proyecto:

```bash
source scripts/java-env.sh
"$java_bin" '-Dbolsin.demo.url=jdbc:h2:./data/presentacion-01;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE' -cp 'target/classes:lib/*' app.MainCU28
```

Abrir la misma dirección: **http://localhost:8080**.

- La primera vez se crea `data/presentacion-01.mv.db` con todos los ejemplos iniciales.
- Si volvemos a usar `presentacion-01`, se conservan las recepciones realizadas sobre esa base.
- Para otro ensayo desde cero, cambiar el nombre a `presentacion-02`, `presentacion-03`, etc., eligiendo uno que no hayamos usado.
- No es necesario ejecutar SQL manualmente ni borrar archivos de datos.

Para demostrar que la información sobrevive al reinicio, volver a ejecutar **exactamente el mismo comando y nombre de base**.

## 4. Recorrido sugerido para la defensa

### 4.1. Mostrar la sesión y la selección inicial

1. Mostrar el paso **“Comisión médica de destino”**.
2. Señalar que la sesión corresponde a **Ana Pérez** y a **CM Villa María**; no hay que iniciar sesión con una contraseña en esta demo.
3. Presionar **“Ver bolsines enviados”**.
4. Explicar que solo se muestran bolsines enviados a la CM del empleado.

En una base nueva aparecen **14 bolsines disponibles**: los originales 500/501 y los ejemplos 600–611. El 612 ya está recibido y el 613 pertenece a otra CM destino; por eso no aparecen.

### 4.2. Demostrar A1: precinto inexistente

1. Escribir `PRE-INEXISTENTE` en **“Número de precinto”**.
2. Dejar vacío el filtro de origen y presionar **“Buscar”**.
3. Mostrar el mensaje correspondiente a A1 y la lista vacía.
4. Presionar **“Mostrar todos”** para recuperar el listado.

### 4.3. Demostrar A2: CM de origen inexistente

1. Escribir `CM Inexistente` en **“CM de origen”**.
2. Dejar vacío el filtro de precinto y presionar **“Buscar”**.
3. Mostrar el mensaje correspondiente a A2.
4. Presionar **“Mostrar todos”**.

El precinto se busca por coincidencia exacta, sin distinguir mayúsculas. Para el origen se permite una parte del nombre: `Mendoza` encuentra los bolsines de esa CM. Usar los nombres tal como se muestran si contienen acentos.

### 4.4. Seleccionar un ejemplo con varios remitos

Para mostrar los recorridos de remitos y detalles del diagrama:

1. Buscar el precinto **`PRE-2026-611`**.
2. Marcar el bolsín **611** y presionar **“Seleccionar bolsín”**.
3. Revisar sus **tres remitos y siete documentos**.
4. Señalar el asunto y tipo de cada documento.
5. Presionar **“El contenido coincide”**.

### 4.5. Demostrar A6: no confirmar

1. En **“Confirmar recepción”**, presionar **“No confirmar, volver”**.
2. Mostrar que se regresa a la revisión de documentación.
3. Explicar que no se guardaron cambios ni se generó la notificación.
4. Presionar nuevamente **“El contenido coincide”** para continuar con el flujo principal.

### 4.6. Completar la recepción correcta

1. Mostrar los estados que se van a registrar.
2. Presionar **“Confirmar recepción”** una vez.
3. Mostrar el resultado **“Recepción registrada”**.
4. Cambiar a la terminal y mostrar la salida **`[CU29 SIMULADO]`**, con la CM origen y la situación de la documentación.
5. Presionar **“Iniciar otra recepción”** y volver al listado.
6. Buscar `PRE-2026-611`: ya no debe aparecer entre los enviados.

Estados finales esperados:

| Objeto | Estado |
|---|---|
| Bolsín | Recibido en CM Destino |
| Remitos | Recibido y Aceptado |
| Documentación | Recibida y Aceptada |

La fecha de recepción es la misma para los cambios de bolsín y documentación, y se registra el empleado responsable.

### 4.7. Demostrar persistencia

1. Detener la aplicación con `Ctrl+C` en su terminal.
2. Volver a iniciarla con el **mismo comando y la misma base** utilizados antes.
3. Recargar el navegador.
4. Volver al listado y comprobar que el bolsín 611 continúa sin aparecer.

Los datos no se perdieron al reiniciar el programa. Recargar solamente la página no sustituye esta demostración de reinicio del servidor.

### 4.8. Mostrar cancelación (opcional)

1. Iniciar otra recepción y seleccionar, por ejemplo, el bolsín **600**.
2. Antes de confirmar, presionar **“Cancelar recepción”**.
3. Mostrar que el caso finaliza como cancelado.
4. Iniciar otra recepción y verificar que el bolsín 600 sigue disponible.

## 5. Otros ejemplos útiles

| Bolsín | Precinto | Uso sugerido |
|---|---|---|
| 600 | PRE-2026-600 | Recorrido corto: un remito y un documento. |
| 602 | PRE-2026-602 | Dos remitos y cuatro documentos. |
| 606 | PRE-2026-606 | Tres remitos y seis documentos. |
| 611 | PRE-2026-611 | Recorrido completo: tres remitos y siete documentos. |

Si un ejemplo ya se recibió, elegir otro disponible o usar una nueva base de presentación como se explica en la opción B.

## 6. Qué abrir para explicar el diseño

Tener preparados en el IDE y el visor de diagramas:

| Archivo | Qué mostrar |
|---|---|
| [clases-state.drawio](diagramas/clases-state.drawio) | Abrir **la segunda página**, referencia principal del patrón. |
| [secuencia-cu28.png](diagramas/secuencia-cu28.png) | Inicio, búsqueda, selección y confirmación. |
| [secuencia-state.png](diagramas/secuencia-state.png) | Orden de llamadas del registro con State. |
| [maquina-documentacion.png](diagramas/maquina-documentacion.png) | Transiciones permitidas de Documentación. |
| [GestorRecepcionDeBolsin.java](../src/control/GestorRecepcionDeBolsin.java) | Coordinación del CU y búsqueda de estados de bolsín/remito. |
| [Documentacion.java](../src/modelo/Documentacion.java) | Contexto: delegación de la operación en el estado actual. |
| [EstadoDocumentacion.java](../src/modelo/EstadoDocumentacion.java) | Contrato del patrón y operaciones no permitidas. |
| [EnBolsinEnviado.java](../src/modelo/EnBolsinEnviado.java) | Cierre del historial y creación del estado e historial nuevos. |
| [RecibidayAceptada.java](../src/modelo/RecibidayAceptada.java) | Estado final de la recepción correcta. |
| [RepositorioRecepcion.java](../src/persistencia/RepositorioRecepcion.java) | Transacción JDBC y verificación de vigencia. |

Explicación breve del patrón:

> Documentación delega en su estado actual. EnBolsinEnviado conoce cómo recibirla: cierra el historial vigente, crea RecibidayAceptada y registra el nuevo cambio con fecha y responsable. El gestor coordina el CU, pero no decide el estado concreto de la documentación. Bolsín y Remito utilizan estados simples.

Para las decisiones de implementación y diferencias de notación entre los diagramas originales, consultar [diseno.md](diseno.md).

## 7. Ejecutar desde IntelliJ IDEA

Como alternativa a la terminal:

1. Abrir `pom.xml` como proyecto Maven.
2. Seleccionar un JDK 17 o posterior y cargar las dependencias Maven.
3. Abrir `src/app/MainCU28.java`.
4. Crear o ejecutar la configuración de `app.MainCU28`.
5. Usar la raíz del proyecto como **Working directory**.
6. Dejar vacíos los argumentos del programa para utilizar la base local.
7. Abrir http://localhost:8080 en el navegador.
8. Detener con el botón **Stop** del IDE.

Para una base nueva, agregar en **VM options**, no en los argumentos del programa:

```text
-Dbolsin.demo.url=jdbc:h2:./data/presentacion-01;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE
```

Esta alternativa también permite ejecutar desde Windows sin utilizar los scripts Bash. Si se ejecuta Java manualmente en Windows, usar `;` como separador del classpath: `target/classes;lib/*`.

## 8. PostgreSQL / Supabase: solo si se presenta esa configuración

El modo local H2 alcanza para ejecutar el proyecto con persistencia real. No cambiar a PostgreSQL durante la defensa sin haber probado antes la conexión de ese equipo.

Para usar una base PostgreSQL preparada:

1. Descargar los drivers con el comando Maven del paso 2.3.
2. Copiar `.env.example` a `.env` y completar URL JDBC, usuario y contraseña.
3. Verificar que la base tenga el esquema compatible y una única sesión abierta válida.
4. Iniciar con:

```bash
bash scripts/run.sh --postgres
```

Para preparar una **base nueva y vacía de pruebas**, ejecutar primero `db/schema.sql` y luego `db/demo.sql` una sola vez. No ejecutar el seed sobre datos existentes. La carga automática de los ejemplos 600–613 corresponde al modo local H2.

No mostrar ni subir `.env`, porque contiene la configuración privada de conexión. La instalación real de Supabase requiere una comprobación propia; no forma parte de las pruebas locales automatizadas.

## 9. Problemas habituales

| Situación | Qué hacer |
|---|---|
| `java`, `javac` o `mvn` no se encuentran | Comprobar la instalación y configurar `JAVA_HOME`/`PATH`. Para ejecutar, se puede usar el JDK del IDE; Maven se necesita para descargar dependencias en un equipo nuevo. |
| `release version 17 not supported` | El compilador es anterior a Java 17. Seleccionar un JDK compatible. |
| `No suitable driver` | Descargar las dependencias en `lib/`; en IntelliJ, cargar el proyecto Maven. |
| `mapfile: command not found` | El script se ejecuta con un Bash antiguo. Usar Bash 4+ o ejecutar desde IntelliJ. |
| El puerto 8080 está ocupado | Detener una instancia anterior o usar el puerto 8081 con el comando de abajo. |
| No se encuentra `db/schema.sql` o la web no carga sus archivos | Ejecutar desde la raíz del proyecto. En IntelliJ, corregir el Working directory. |
| El navegador no conecta | Confirmar que la terminal siga abierta y haya mostrado la URL de inicio. Usar el puerto indicado en esa URL. |
| No aparecen bolsines | Presionar “Mostrar todos”. Si ya se recibieron, elegir una nueva base de presentación; los recibidos no se restauran al reiniciar. |
| La sesión del navegador venció o el servidor fue reiniciado | Recargar la página para obtener un nuevo flujo. |
| La base está bloqueada | Detener otra instancia que use el mismo archivo H2 o cerrar una conexión externa a ese archivo. No borrar la base. |
| Las pruebas HTTP no pueden abrir un puerto | Ejecutarlas en una terminal local que permita conexiones a localhost; el servidor de pruebas usa un puerto temporal. |

Cambiar de puerto, después de compilar:

```bash
source scripts/java-env.sh
"$java_bin" -Dbolsin.port=8081 -cp 'target/classes:lib/*' app.MainCU28
```

En ese caso abrir **http://localhost:8081**. Este comando usa la base local predeterminada; si se venía usando otra base, agregar también su opción `-Dbolsin.demo.url=...`.

## 10. Lista de control del día de la presentación

- [ ] Tener el repositorio completo y las dependencias descargadas en `lib/`.
- [ ] Haber ejecutado las 81 verificaciones en la computadora que se utilizará.
- [ ] Elegir qué base se usará y anotar el comando de inicio para repetirlo al demostrar persistencia.
- [ ] Tener disponible el bolsín 611 o elegir otro equivalente antes de comenzar.
- [ ] Abrir el navegador, la terminal del servidor, el código y los diagramas.
- [ ] Mostrar A1, A2 y A6, además del flujo principal.
- [ ] Explicar que CU29 está **simulado en consola**, no envía correo real.
- [ ] Aclarar que A3–A5 no están implementados en la interfaz; no ofrecerlos como parte de la demostración.
- [ ] Al terminar, detener el servidor con `Ctrl+C` o con Stop en IntelliJ.

**Comando habitual:** `bash scripts/run.sh` · **Dirección:** http://localhost:8080 · **Pruebas:** `bash scripts/test.sh`.
