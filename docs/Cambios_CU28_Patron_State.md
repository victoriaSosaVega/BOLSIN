# CU28 – Cambios para adecuar el proyecto al patrón State

Documento de trabajo (15/11/2026). Explica **qué se cambió exactamente** en el
proyecto y **en qué concuerda** con el diagrama de secuencia y con el diagrama
de clases, y en qué no.

Archivos de referencia:

| Qué | Archivo |
|---|---|
| Diagrama de clases (el tuyo, con State) | `CU_28_Diagrama_de_clases.png` (código fuente en `message.txt`) |
| Diagrama de secuencia nuevo (2 páginas) | `PPAI Realización CU 28 - Codigo.drawio` |
| Diagrama de clases actualizado (v2) | `CU_28_Diagrama_de_clases_v2.png` / `.drawio` / `.puml` |
| ERS | `2026 DSI Bolsines ERS V1-2.pdf` (CU28, flujo paso 11) |

En este documento, **"diagrama original"** es `CU_28_Diagrama_de_clases.png` y
**"v2"** es el que quedó después de este trabajo.

---

## 1. Resumen

- El diagrama de clases nuevo es una **refactorización al patrón State**: `Estado`
  es abstracto y cada estado concreto es una subclase que sabe qué transiciones
  permite.
- El código anterior no hacía State de verdad: `Estado` era una clase concreta con
  métodos `esEnviado()`, `esRecibidoyAceptado()`… que comparaban el nombre, solo
  había 5 subclases y `Bolsin`, `Remito` y `Documentacion` tenían el estado
  **fijo** (`new Enviado(this)`, `new EnBolsinEnviado()`).
- Ahora `Bolsin`, `Remito` y `Documentacion` tienen un atributo `estado` y
  **delegan** en él. Las 12 subclases del diagrama existen (13 clases, porque
  `EnBolsinEnviado` son dos).
- Se agregó el **empleado responsable** a toda la cadena (lo pide el ERS, paso 11).
- Se agregaron un script de migración y uno de reinicio para la base.

---

## 2. Qué se cambió, por capa

### 2.1 Modelo (`src/modelo/`)

**`Estado`** pasó a ser abstracta.
- Atributos `nombre`, `descripcion`, `ambito` (como el diagrama).
- Las 13 operaciones del diagrama, con implementación por defecto que lanza
  `IllegalStateException("La operacion X no esta permitida en el estado Y")`.
- Se **sacaron** los `esRecibidoyAceptado()`, `esAmbitoBolsin()`, etc. y la lista
  estática de estados.
- Se **agregó** `esEnviado()`: devuelve `false` en `Estado` y `true` en `Enviado`.
  La secuencia lo necesita (`CambioEstadoBolsin.esEnviado()` → `Estado.esEnviado()`)
  y ahora es polimórfico en vez de comparar un texto.
- Se **agregó** un método protegido `cambiarEstadoDocumentacion(...)` que cierra el
  historial vigente, crea el nuevo, y actualiza el estado de la documentación. Lo
  usan las transiciones que **no** son del CU28 para no repetir esos pasos.

**Subclases de `Estado`** (cada una crea los estados a los que transiciona):

| Clase | Ámbito | Transiciones que implementa | Pasa a |
|---|---|---|---|
| `Registrada` *(nueva)* | Documentación | `incluirEnRemito` | `EnRemito` |
| `EnRemito` *(nueva)* | Documentación | `incluirEnBolsin` / `quitarDeRemito` | `EnBolsinSaliente` / `Registrada` |
| `EnBolsinSaliente` *(nueva)* | Documentación | `quitarDeBolsin` / `retirar` | `EnRemito` / `EnBolsinEnviadoDocumentacion` |
| `NoRecibida` *(nueva)* | Documentación | `reenviar` / `darDeBaja` | `Registrada` / `DeBaja` |
| `RecibidaYRechazada` *(nueva)* | Documentación | `reenviar` | `Registrada` |
| `ParaRedirigir` *(nueva)* | Documentación | `marcarRecibidayRechazada` / `marcarRecibidayAceptada` | `RecibidaYRechazada` / `RecibidayAceptada` |
| `DeBaja` *(nueva)* | Documentación | ninguna (final) | — |
| `Enviado` | Bolsín | `marcarRecibidoEnCMDestino` | `RecibidoEnCMDestino` |
| `EnBolsinEnviadoRemito` | Remito | `marcarRecibidoyAceptado` | `RecibidoyAceptado` |
| `EnBolsinEnviadoDocumentacion` | Documentación | `marcarRecibidayAceptada` / `marcarNoRecibida` / `marcarRecibidayRechazada` / `redirigir` | `RecibidayAceptada` / `NoRecibida` / `RecibidaYRechazada` / `ParaRedirigir` |
| `RecibidoEnCMDestino`, `RecibidoyAceptado`, `RecibidayAceptada` | — | ninguna (finales en el CU28) | — |

Las clases ya existentes (`Enviado`, `EnBolsinEnviado…`, `RecibidoEnCMDestino`,
`RecibidoyAceptado`, `RecibidayAceptada`) se reescribieron: **ya no guardan** una
referencia al bolsín/remito (los reciben por parámetro, como `this: Bolsin` en el
diagrama) y **no buscan** su estado en una lista; cada una crea su propio nombre
y ámbito.

**`EnBolsinEnviado` son dos clases.** En el diagrama hay dos cajas con ese nombre;
son estados distintos (cada una hace cosas distintas según sea de un remito o de
una documentación). En Java no puede haber dos clases con el mismo nombre en un
paquete, así que se llaman `EnBolsinEnviadoRemito` y `EnBolsinEnviadoDocumentacion`.
En el diagrama v2 siguen llamándose las dos `EnBolsinEnviado`, con una línea que
aclara cuál es cuál.

**`Bolsin`, `Remito`, `Documentacion`**
- Reemplazaron el estado fijo por `Estado estado` + `setEstado(...)`.
- Delegan: `estado.marcarRecibidoEnCMDestino(this, …)`,
  `estado.marcarRecibidoyAceptado(this, …)`, `estado.marcarRecibidayAceptada(this, …)`.
- `Documentacion` perdió su lógica propia (`buscarHistorial`, `crearEstado`,
  `crearCambioEstado`, `cambiarEstado`): ahora está en el estado. Los métodos que
  eran *stubs* (`reenviar`, `darDeBaja`, `retirar`, `incluirEnRemito`, …) ahora
  delegan en el estado.
- `Bolsin` guarda el `CambioEstadoBolsin` vigente cuando lo encuentra al buscar
  (`esEstadoEnviado()`); `Enviado` lo reutiliza para cerrarlo, sin volver a buscarlo
  (es lo que dibuja la secuencia: el `actual` de la página 2 es el que se encontró
  en la página 1). Si ese dato no está o ya no es el vigente, se recalcula.
- `Bolsin.esCMDestino(String)` reemplazó a `esCMEmpleado(CM)`; compara por el
  **nombre** de la CM, que es lo que muestra la secuencia
  (`esCMDestino(CMEmpleado: String)`).
- Si un remito o documentación no tiene estado cargado, se lanza un error que
  indica correr `db/migracion_state.sql`.

**`CambioEstadoBolsin`, `CambioEstadoDocumentacion`**
- Nuevo atributo `responsable: Empleado`; `new(fechaActual, estado, empleado)`.
- Se sacó `setEstado()` (el constructor ya lo recibe).
- `CambioEstadoBolsin` conserva `esActual()` y `esEnviado()`.

**Cadena del empleado responsable.** `Gestor` → `Bolsin.marcarRecibidoEnCMDestino(fecha, empleado)`
→ `Enviado` → `Remito.marcarRecibidoyAceptado(fecha, empleado)` →
`EnBolsinEnviadoRemito` → `DetalleRemito.marcarRecibidayAceptadaDocumentacion(fecha, empleado)`
→ `Documentacion.marcarRecibidayAceptada(fecha, empleado)` → `EnBolsinEnviadoDocumentacion`.
El empleado queda guardado en los `CambioEstado` nuevos del bolsín y de la
documentación (los remitos no tienen historial).

### 2.2 Control (`GestorRecepcionDeBolsin`)

- `registrarRecepcionBolsin()` → `registrarRecepcionDeBolsin()` (nombre del diagrama).
- Pasa `empleadoLogueado` a `Bolsin.marcarRecibidoEnCMDestino`.
- Busca los bolsines con `cmEmpleado.getNombre()` (por `esCMDestino(String)`).
- No tiene los atributos `estadoRecibidoEnCMDestino`, `estadoRecibidoyAceptado` y
  `estadoRecibidayAceptada`: en la secuencia nueva los estados los crean los
  propios estados (`crearEstado()`), así que el Gestor ya no los usa.

### 2.3 Persistencia (`src/persistencia/`)

- **`FabricaDeEstados`** (nueva): arma la subclase concreta de cada fila de la
  tabla `estado` según ámbito y nombre. Si el estado de la base no tiene clase
  (por ejemplo "Recibido y Aceptado Parcial") se ignora, salvo que algo lo use.
- **`CargadorDeDatos`**: usa la fábrica; carga `id_empleado` en los historiales; y deja
  cargado el **estado actual** de bolsín y documentación (el del cambio de estado
  vigente) y el de cada remito (`remito.id_estado`).
- **`RepositorioRecepcion`**: guarda `id_empleado` en los cambios de estado nuevos. Los
  estados creados por `crearEstado()` no traen el `id` de la base: se resuelve
  por nombre y ámbito al guardar (con un error claro si no existe).

### 2.4 Base de datos (`db/`)

- **`migracion_state.sql`** (correr una vez, es repetible):
  1. agrega `id_empleado` (con FK a `empleado`) a `cambio_estado_bolsin` y
     `cambio_estado_documentacion`;
  2. carga los estados que faltaban: *Registrada*, *En Remito*, *En Bolsin Saliente*,
     *De Baja* (todos de Documentación) y *En Bolsin Enviado* (una fila para
     Documentación y otra para Remito);
  3. deja un estado inicial: remitos sin estado cuyo bolsín está *Enviado* →
     *En Bolsin Enviado*, y documentación sin historial → *En Bolsin Enviado*
     vigente. Esto hace falta porque el código viejo **suponía** ese estado en
     memoria y ahora tiene que estar en la base.
  - Termina con una consulta de control (remitos y documentación con su estado).
- **`reiniciar_demo_cu28.sql`**: devuelve los bolsines 500 y 501 a *Enviado*, con sus
  remitos y documentación en *En Bolsin Enviado*. Es necesario porque con State un
  remito ya *Recibido y Aceptado* no admite otra recepción, y
  `restore_demo_statuses.sql` solo restaura el bolsín.

### 2.5 Diagramas y documentación

- `CU_28_Diagrama_de_clases_v2.*`: diagrama de clases actualizado (sección 4).
- `README.md` y este documento.

---

## 3. Correspondencia con el diagrama de secuencia

Leyenda: **✔** coincide; **✔ +empleado** coincide, pero el código agrega el parámetro
`empleado`; **≈** hace lo mismo pero lo llama otra clase.

### Página 1 – búsqueda, selección y confirmación

| Mensaje del diagrama | En el código | |
|---|---|---|
| `inIciarCU28()` | `Gestor.iniciarCU28()` | ✔ |
| `obtenerEmpleadoLogueado()` → `obtenerEmpleadoEnSesion()` → `obtenerEmpleado()` → `getCM()` | `Gestor.obtenerEmpleadoLogueado()` → `Sesion` → `Usuario` → `Empleado.getCM()` | ✔ |
| `getNombre()` de la CM y `mostrarCMEmpleadoLogueado(logueado)` | `PantallaRecepcionDeBolsin.mostrarCMEmpleadoLogueado` | ✔ |
| `buscarBolsinesEnEstadoEnviadoParaCMEmpleado()` | `Gestor` → `Bolsin.buscarBolsinesEnEstadoEnviadoParaCMEmpleado(nombreCM)` | ✔ |
| loop: `esEstadoEnviado()` → `*esActual()` → `esEnviado()` → `Estado.esEnviado()` | `Bolsin.esEstadoEnviado()` → `CambioEstadoBolsin.esActual()` → `esEnviado()` → `Estado.esEnviado()` | ✔ |
| `esCMDestino(CMEmpleado: String)` | `Bolsin.esCMDestino(String)` | ✔ (lo llama la búsqueda estática de `Bolsin`, no el Gestor: ≈) |
| `getCMOrigen()`, `getNombre()`, `getNumeroPrecinto()` | los llama la pantalla al armar la lista | ≈ |
| `mostrarCMSelección(Bolsin[])`, `tomarBolsinSeleccionado()` | pantalla y Gestor | ✔ |
| `buscarRemitosAsociadosBolsin()` → `buscarRemitos()` | `Gestor` → `Bolsin.buscarRemitos()` | ✔ |
| loop `getNumero()`, `obtenerDatosDocumentacion()`, `getAsunto()`, `getTipo()`, `getNombre()` | los recorre `PantallaRecepcionDeBolsin.mostrarOpcionesRecepcionBolsin` | ≈ |
| `mostrarOpcionesRecepcionBolsin`, `tomarSeleccionRecepcionBolsin`, `solicitarConfirmacion`, `tomarConfirmacion` | pantalla y Gestor | ✔ |

### Página 2 – registro

| Mensaje del diagrama | En el código | |
|---|---|---|
| `registarRecepcionDeBolsin()` (con errata) | `Gestor.registrarRecepcionDeBolsin()` | ✔ |
| `obtenerFechaActual()` | `Gestor.obtenerFechaActual()` | ✔ |
| `Bolsin.marcarRecibidoEnCMDestino(fechaActual)` | `marcarRecibidoEnCMDestino(fechaActual, empleado)` | ✔ +empleado |
| `Enviado.marcarRecibidoEnCMDestino(this, fecha, empleado)` | igual | ✔ |
| `actual.setFechaHoraFin(fecha)` (sin buscar el vigente) | usa el vigente guardado en `Bolsin` | ✔ |
| `marcarRecibidoyAceptadoRemito(fecha)` | `(bolsin, fecha, empleado)` | ✔ +empleado, +bolsin |
| loop remitos: `Remito.marcarRecibidoyAceptado(fecha)` | `(fecha, empleado)` | ✔ +empleado |
| `EnBolsinEnviado.marcarRecibidoyAceptado(this: Remito, fecha)` | `EnBolsinEnviadoRemito`, `(remito, fecha, empleado)` | ✔ +empleado |
| `marcarRecibidayAceptadaDocumentacion(fecha)` (autollamada) | `(remito, fecha, empleado)` | ✔ +empleado, +remito |
| loop detalles: `DetalleRemito.marcarRecibidayAceptadaDocumentacion(fecha)` | `(fecha, empleado)` | ✔ +empleado |
| `Documentacion.marcarRecibidayAceptada(fecha)` | `(fecha, empleado)` | ✔ +empleado |
| `EnBolsinEnviado.marcarRecibidayAceptada(this: Documentacion, fecha, empleado, historiales)` | `EnBolsinEnviadoDocumentacion`, misma firma | ✔ |
| `buscarHistorial`, `*esActual()`, `setFechaHoraFin` | iguales | ✔ |
| `crearEstado()` → `new(): RecibidayAceptada` | igual | ✔ |
| `crearCambioEstado(fecha, estado)` → `new(fecha, estado)` | `(fecha, estado, empleado)` | ✔ +empleado |
| `Documentacion.setEstado(estado)`, `agregarCambioEstado(cambio)` | iguales | ✔ |
| (tras el loop) `crearEstado()` → `new(): RecibidoyAceptado`, `Remito.setEstado(estado)` | igual | ✔ |
| (tras el loop de remitos) `crearEstado()` → `new(): RecibidoEnCMDestino` | igual | ✔ |
| `crearCambioEstado` → `new CambioEstadoBolsin`, `Bolsin.setEstado`, `agregarCambioEstado` | igual (+empleado) | ✔ |
| `llamarCU29()` → `notificarCMOrigen()` → `*notificar()`, `finCU()` | sin cambios | ✔ |

**No está en la secuencia:** `Gestor.guardarCambios()` (infraestructura: guarda en la
base en una transacción antes de llamar al CU29) y la carga inicial de la base.

---

## 4. Correspondencia con el diagrama de clases

Por clase: qué decía el diagrama original, qué hay en el código y qué cambió en la v2.

| Clase | Diagrama original | Código | v2 |
|---|---|---|---|
| `Gestor` | 8 atributos (incl. `estado…` ×3); `registrarRecepcionDeBolsin`, `obtenerFechaActual` | sin los `estado…`; además tiene `iniciarCU28`, `tomarBolsinSeleccionado`, `tomarSeleccionRecepcionBolsin`, `tomarConfirmacion` (de la secuencia) y `guardarCambios` (infra) | quita los `estado…`; `remitos: Remito[]`. Sigue mostrando solo 2 operaciones |
| `Bolsin` | `marcarRecibidoEnCMDestino(fecha)`, `setEstado`, `agregarCambioEstado`; `numeroPrecinto: Int` | las 3 (la primera con `empleado`) + atributo `estado`; `numeroPrecinto: String`; `cmOrigen`, `cmDestino`; operaciones de la secuencia | agrega todo eso y las asociaciones `estado`, `cmOrigen`, `cmDestino` |
| `Remito` | `marcarRecibidoyAceptado(fecha)`, `setEstado` | + `empleado`; atributo `estado`; `getNumero`, `obtenerDatosDocumentacion` | agrega asociación `estado` y operaciones |
| `DetalleRemito` | `marcarRecibidayAceptadaDocumentacion(fecha)` | + `empleado`; `obtenerDatosDocumentacion` | idem |
| `Documentacion` | `getAsunto`, `getTipo`, `marcarRecibidayAceptada(fecha)`, `setEstado`, `agregarCambioEstado` | + `empleado`; atributo `estado`; 10 operaciones más que delegan en el estado | agrega asociaciones `estado` y `tipoDocumentacion`, y las operaciones |
| `CambioEstadoBolsin` | `setFechaHoraFin`, `new(fecha, estado)` | + `responsable`; `new(fecha, estado, empleado)`; `esActual`, `esEnviado` | agrega todo + asociación `responsable` |
| `CambioEstadoDocumentacion` | `esActual`, `setFechaHoraFin`, `new(fecha, estado)` | + `responsable`; `new(fecha, estado, empleado)` | idem |
| `Estado` | abstracta; 13 operaciones **sin parámetros** | abstracta; 13 operaciones **con parámetros** + `esEnviado()` + `cambiarEstadoDocumentacion` | igual que el código |
| `Registrada`, `NoRecibida`, `ParaRedirigir`, `EnRemito`, `RecibidaYRechazada`, `EnBolsinSaliente`, `DeBaja` | cajas con sus operaciones | clases implementadas (`new()` + las operaciones de cada caja) | operaciones como `+nombre(...)` |
| `Enviado` | `marcarRecibidoEnCMDestino(this, fecha, empleado)`, `marcarRecibidoyAceptadoRemito(fecha)`, `crearEstado()`, `crearCambioEstado(fecha, estado)` | igual, con `bolsin` y `empleado` donde hacía falta | idem |
| `EnBolsinEnviado` (remito) | `marcarRecibidoyAceptado(this: Remito, fecha)`, `marcarRecibidayAceptadaDocumentacion(fecha)`, `crearEstado()` | `EnBolsinEnviadoRemito`, con `empleado` y `remito` | caja propia |
| `EnBolsinEnviado` (documentación) | `marcarRecibidayAceptada(…)`, `buscarHistorial`, **`esActual()`**, `crearEstado`, `crearCambioEstado`, `marcarNoRecibida()`, `marcarRecibidayRechazada()`, `marcarRecibidayAceptada()`, `redirigir()` | `EnBolsinEnviadoDocumentacion`; **sin** `esActual()` propio (se usa el de `CambioEstadoDocumentacion`) | caja propia, sin `esActual()` |
| `RecibidayEnCMDestino` (typo) | `new(): RecibidoEnCMDestino` | `RecibidoEnCMDestino` | `RecibidoEnCMDestino` |
| `CM`, `Empleado`, `TipoDocumentacion` | no aparecían | existen | agregadas |

**Asociaciones nuevas en la v2:** `Bolsin` → `Estado`, `Remito` → `Estado` y
`Documentacion` → `Estado` (las pediste); `CambioEstado*` → `Empleado` (`responsable`);
`Bolsin` → `CM` (`cmOrigen`, `cmDestino`); `Empleado` → `CM`; `Documentacion` →
`TipoDocumentacion`.

---

## 5. Diferencias respecto de los diagramas, y por qué

1. **`empleado` en `Bolsin`, `Remito`, `DetalleRemito`, `Documentacion` y `CambioEstado*`.**
   Ni el diagrama de clases ni el de secuencia lo tenían en esos lugares, pero sí en
   `Enviado` y `EnBolsinEnviado` (documentación), y el ERS pide registrar el
   responsable. Sin esos parámetros el dato no puede llegar al estado.
2. **Operaciones de `Estado` con parámetros.** El diagrama las muestra sin
   parámetros, pero cada estado concreto las recibe (`this: Bolsin`, `fecha`,
   `empleado`…); para sobrescribirlas en Java la firma tiene que coincidir.
3. **`Estado.esEnviado()`.** La secuencia lo llama; el diagrama original no lo tenía.
4. **Parámetro `bolsin`/`remito` en `marcarRecibidoyAceptadoRemito` y en
   `marcarRecibidayAceptadaDocumentacion`.** El estado no guarda referencias, así que
   necesita saber sobre quién actúa.
5. **`EnBolsinEnviado` dividido en dos clases** (sección 2.1).
6. **El Gestor sin `estadoRecibidoEnCMDestino`, `estadoRecibidoyAceptado`,
   `estadoRecibidayAceptada`.** La secuencia nueva ya no los usa.
7. **`numeroPrecinto` es `String`** (en el diagrama original era `Int`; en la base es texto).
8. **`esCMDestino` compara por nombre de CM** (antes por código), como indica la secuencia.
9. **`Gestor.guardarCambios()`** y las clases de persistencia no están en ningún diagrama:
   son infraestructura.

---

## 6. Qué hay que actualizar en el diagrama de secuencia

Para que coincida con el código y con el diagrama de clases v2:

- Agregar el parámetro `empleado` a: `Bolsin.marcarRecibidoEnCMDestino`,
  `marcarRecibidoyAceptadoRemito`, `Remito.marcarRecibidoyAceptado`,
  `EnBolsinEnviado.marcarRecibidoyAceptado`, `marcarRecibidayAceptadaDocumentacion`
  (la autollamada y la de `DetalleRemito`) y `Documentacion.marcarRecibidayAceptada`.
- Los dos `new(fechaActual, estado)` de los `CambioEstado*` pasan a
  `new(fechaActual, estado, empleado)`, y `crearCambioEstado` también.
- `marcarRecibidoyAceptadoRemito` pasa a recibir el bolsín, y la autollamada
  `marcarRecibidayAceptadaDocumentacion` recibe el remito.
- Corregir la errata `registarRecepcionDeBolsin()` → `registrarRecepcionDeBolsin()`.
- Opcional: dibujar las dos `EnBolsinEnviado` con una nota que diga cuál es de remito
  y cuál de documentación.

---

## 7. Verificación

**Hecho**
- Compila con `javac` (JDK 25).
- Prueba en memoria (35 verificaciones, con un programa de prueba que **no está en el
  repositorio**): flujo completo del CU28 con la fecha única, el responsable y el
  historial cerrado/abierto en bolsín y documentación; búsqueda que deja de listar el
  bolsín recibido; todas las transiciones de documentación (incluidas las de A3, A4 y A5 y las de otros CU); errores en transiciones no
  permitidas y en remito sin estado.
- `migracion_state.sql` se corrió en Supabase: la consulta de control mostró los
  remitos 1001 y 1002 y las documentaciones 1, 2 y 3 en *En Bolsin Enviado*.

**No hecho**
- No se corrió la aplicación completa contra la base (falta el driver JDBC y el
  `.env` en este entorno): `CargadorDeDatos` y `RepositorioRecepcion` están
  modificados pero **sin probar contra Supabase**.
- `reiniciar_demo_cu28.sql` no se ejecutó nunca.
- Las transiciones que no son del CU28 (A3, A4, A5 y las de otros CU) solo se
  probaron en memoria; la pantalla no las ofrece.
- El `.puml` no se renderizó con PlantUML.

---

## 8. Pendientes / decisiones abiertas

- Actualizar el diagrama de secuencia (sección 6).
- `EnBolsinSaliente` y `DeBaja` se dejaron como en el diagrama (una clase cada una,
  solo para documentación). Si otro CU necesita esos estados para remito o bolsín, se
  agregarían entonces como clases propias.
- `CM.esCMEmpleado(CM)` quedó sin uso (lo reemplazó `Bolsin.esCMDestino(String)`).
- Las variantes A1/A2 (buscar por precinto o por CM origen) y A3/A4/A5/A6 siguen sin
  ofrecerse en la pantalla.
