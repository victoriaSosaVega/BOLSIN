# Bolsines — CU28: Registrar recepción de bolsín

Resolución de la entrega 2 del PPAI de Diseño de Sistemas de Información. Aplicación web local con backend Java, patrón **State en Documentación** y persistencia JDBC.

La [guía de ejecución y presentación](docs/guia-presentacion.md) detalla la preparación del equipo, el arranque y el recorrido para la defensa.

## Requisitos

- JDK 17 o posterior.
- Maven para descargar las dependencias en una instalación nueva.
- Bash para los scripts (Linux, macOS o Git Bash en Windows).
- Un navegador web.

No requiere Node.js, instalar un frontend ni servicios externos para el modo local.

## Ejecutar

Desde la raíz del repositorio:

```bash
mvn dependency:copy-dependencies -DoutputDirectory=lib
bash scripts/run.sh
```

Abrir **[http://localhost:8080](http://localhost:8080)**. Para detener el servidor, presionar `Ctrl+C`.

La descarga de dependencias se realiza una sola vez. El script compila a Java 17 y usa `JAVA_HOME`, el JDK del `PATH` o uno instalado en las ubicaciones habituales de IntelliJ/Linux. Si no lo encuentra, configurar `JAVA_HOME` con la ruta al JDK.

La base local H2 se crea automáticamente en `data/cu28.mv.db`. Los ejemplos se agregan una sola vez; las recepciones realizadas se conservan al reiniciar. Tanto los datos como las dependencias descargadas y los archivos compilados están excluidos de Git.

### IntelliJ IDEA

Abrir `pom.xml` como proyecto Maven, elegir un JDK 17 o posterior y ejecutar `app.MainCU28` con la raíz del repositorio como directorio de trabajo. Luego abrir la URL local. La configuración personal del IDE no se versiona.

### Puerto o base de demostración alternativos

Después de compilar:

```bash
java -Dbolsin.port=8081 -cp 'target/classes:lib/*' app.MainCU28
```

Para usar otra base sin modificar la existente:

```bash
java -Dbolsin.demo.url='jdbc:h2:./data/otra-demo;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE' -cp 'target/classes:lib/*' app.MainCU28
```

En Windows, el separador del classpath es `;` en lugar de `:`.

## Alcance funcional

- Recepción correcta: muestra los bolsines enviados a la CM del empleado, sus remitos y documentación; solicita confirmación y guarda los estados, la fecha y el responsable.
- **A1:** búsqueda por precinto sin resultados.
- **A2:** búsqueda por CM de origen sin resultados.
- **A6:** regreso sin confirmar, sin modificar los datos.
- Cancelación, reintento tras errores y protección contra confirmaciones duplicadas o datos desactualizados.

El flujo principal y tres alternativos cubren el mínimo solicitado por la consigna de entrega 2. **A3, A4 y A5 no están conectados a la interfaz**; las transiciones correspondientes existen en el modelo. **CU29 se simula en consola**, sin enviar correo real. Los detalles de diseño y las diferencias de notación entre referencias están en [docs/diseno.md](docs/diseno.md).

## Datos para probar

La sesión de demostración corresponde a **Ana Pérez, CM Villa María**. En una base nueva hay dos bolsines originales (500 y 501) y estos doce ejemplos adicionales:

| Bolsín | Origen | Precinto | Remitos | Documentos |
|---|---|---|---:|---:|
| 600 | Córdoba | PRE-2026-600 | 1 | 1 |
| 601 | Río Cuarto | PRE-2026-601 | 1 | 3 |
| 602 | San Francisco | PRE-2026-602 | 2 | 4 |
| 603 | Rosario | PRE-2026-603 | 1 | 2 |
| 604 | Mendoza | PRE-2026-604 | 2 | 3 |
| 605 | Paraná | PRE-2026-605 | 1 | 4 |
| 606 | Santa Fe | PRE-2026-606 | 3 | 6 |
| 607 | Tucumán | PRE-2026-607 | 1 | 2 |
| 608 | Córdoba | PRE-2026-608 | 2 | 5 |
| 609 | Río Cuarto | PRE-2026-609 | 1 | 1 |
| 610 | Rosario | PRE-2026-610 | 2 | 3 |
| 611 | Mendoza | PRE-2026-611 | 3 | 7 |

Los tipos incluyen expediente, dictamen, estudio médico, carta documento, informe y certificado. Todos los ejemplos son ficticios.

El bolsín **612** ya está recibido y el **613** tiene otra CM destino: no aparecen en la selección de Ana. Para A1 buscar `PRE-INEXISTENTE`; para A2 buscar `CM Inexistente`. Los recibidos no vuelven a aparecer tras reiniciar.

## Pruebas

```bash
bash scripts/test.sh
```

Ejecuta **81 verificaciones** en bases aisladas: flujo principal y alternativos, transiciones State, orden de llamadas, persistencia entre procesos, rollback y reintento, sesiones desactualizadas, API HTTP y carga de ejemplos sin duplicados. No modifica la base de demostración. Las pruebas HTTP abren un puerto local temporal.

Las pruebas son programas Java ejecutables; el comando de verificación del proyecto es el script anterior, no `mvn test`.

## PostgreSQL / Supabase (opcional)

Copiar `.env.example` a `.env` y completar las tres variables de conexión. Las variables del entorno tienen prioridad. `.env` está excluido de Git.

```bash
bash scripts/run.sh --postgres
```

En una **base nueva de pruebas**, ejecutar `db/schema.sql` y luego `db/demo.sql` una sola vez. No ejecutar los datos de demostración sobre una base con información existente. El esquema corresponde al recorte de CU28; una base externa previa debe ser compatible con esas tablas y columnas. Los ejemplos 600–613 se cargan automáticamente solo en H2 local.

La conexión a Supabase no está verificada con credenciales reales en este repositorio.

## Estructura

```text
src/
  app/           Inicio de la aplicación
  boundary/      Pantalla web y adaptación de CU29
  control/       Gestor del CU28
  modelo/        Entidades y patrón State
  persistencia/  JDBC y datos de demostración
  web/           Servidor HTTP local y serialización
web/             HTML, CSS y JavaScript
db/             Esquema y datos iniciales
tests/          Pruebas Java reproducibles
scripts/        Ejecución, pruebas y generación de diagramas
docs/
  diagramas/     Diagramas vigentes y vistas de implementación
  diseno.md      Justificación del patrón y correspondencia con el código
pom.xml          Versión de Java y dependencias
.env.example     Plantilla de conexión PostgreSQL
```

`target/`, `lib/`, `data/` y los archivos del IDE son locales y no se suben al repositorio. Los scripts funcionan desde cualquier directorio y sitúan la ejecución en la raíz del proyecto.
