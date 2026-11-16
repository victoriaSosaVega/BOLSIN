# Diseño del CU28 — Entrega 2

## Tecnología y estructura

Java 17+, interfaz web local en HTML/CSS/JavaScript y servidor HTTP del JDK. La persistencia utiliza JDBC sin ORM: H2 en disco para la ejecución local y PostgreSQL como conexión opcional. El servidor escucha en `127.0.0.1`; no requiere despliegue externo.

La interfaz conserva el recorrido de cinco pasos: identificar destino, seleccionar bolsín, revisar recepción, confirmar y mostrar el resultado. La boundary `PantallaRecepcionDeBolsin` recibe los mensajes del gestor y prepara los datos de la pantalla; JavaScript presenta esos datos y transmite acciones. La lógica de negocio se ejecuta en Java.

## Referencias vigentes

| Archivo | Contenido |
|---|---|
| [clases-state.drawio](diagramas/clases-state.drawio) | Diagrama del grupo. **La segunda página es la referencia principal**; la primera complementa información. |
| [secuencia-cu28.png](diagramas/secuencia-cu28.png) | Búsqueda, selección, confirmación y finalización del CU28. |
| [secuencia-state.png](diagramas/secuencia-state.png) | Registro de la recepción con State. |
| [maquina-documentacion.png](diagramas/maquina-documentacion.png) | Transiciones permitidas de Documentación. |
| [clases-implementacion.puml](diagramas/clases-implementacion.puml) | Vista de implementación Java con tipos, firmas, visibilidad y relaciones, generada desde las clases compiladas. |
| [secuencia-state.puml](diagramas/secuencia-state.puml) | Vista textual del registro de recepción. |
| [confirmacion-persistencia.puml](diagramas/confirmacion-persistencia.puml) | Complemento técnico: transacción, rollback y notificación. |

Los cuatro diagramas del grupo se conservan sin modificar su contenido. Los PlantUML son complementos de implementación, no reemplazos silenciosos de las referencias.

Para regenerar la vista de clases después de compilar, con Python 3 y un JDK:

```bash
python3 scripts/diagramas.py
```

Ejecutar desde la raíz del repositorio o usar la ruta al script. La salida es `docs/diagramas/clases-implementacion.puml`.

## Patrón State

El problema es que las operaciones válidas de Documentación dependen de su estado actual. State distribuye las transiciones entre clases concretas y evita que el gestor decida el comportamiento de cada estado.

- **Contexto:** `Documentacion` mantiene su `EstadoDocumentacion` y delega las operaciones, pasando contexto, fecha, empleado e historial.
- **Estado abstracto:** `EstadoDocumentacion` declara las operaciones y rechaza aquellas que el estado concreto no permite.
- **Estados concretos:** `Registrada`, `EnRemito`, `EnBolsinSaliente`, `EnBolsinEnviado`, `NoRecibida`, `RecibidaYRechazada`, `ParaRedirigir`, `RecibidayAceptada` y `DeBaja`.
- **Historial:** `CambioEstadoDocumentacion` conserva estado, período y empleado responsable.

Bolsín y Remito usan instancias simples de `Estado`; State se aplica únicamente a Documentación.

## Orden de la recepción

1. El gestor busca los estados simples de bolsín y remito y obtiene una única fecha.
2. Bolsín valida el agregado antes de modificarlo y cierra su historial vigente.
3. Bolsín recorre sus remitos; cada remito recorre sus detalles.
4. Cada detalle delega en Documentación y esta en `EnBolsinEnviado`.
5. `EnBolsinEnviado` busca y cierra el historial, crea `RecibidayAceptada`, crea el nuevo cambio con el responsable y lo asocia a Documentación.
6. Remito cambia su estado después de procesar su documentación.
7. Bolsín crea y asocia su nuevo historial después de procesar todos los remitos.
8. El repositorio guarda todo en una transacción. Solo tras el commit se simula CU29 y se informa el éxito.

Si la operación o la transacción falla, `RespaldoRecepcion` restaura el grafo en memoria. Los bloqueos y la verificación del historial vigente rechazan confirmaciones con datos obsoletos. La protección HTTP impide saltar pasos y repetir escrituras por doble confirmación.

## Correspondencia y diferencias de notación

Las referencias originales no son literalmente idénticas entre sí. Estas decisiones se hacen explícitas:

- `Date` → `LocalDate`, `DateTime` → `LocalDateTime`, `Int` → `int`, `Boolean` → `boolean` y colecciones UML → `List<T>`. `new` representa un constructor Java.
- La página 2 declara `Registrada.darDeBaja()`, pero la máquina no permite esa transición. La operación existe y se rechaza desde Registrada; desde NoRecibida conduce a DeBaja.
- Las operaciones de estados abreviadas con `()` se implementan con contexto, fecha, empleado e historial, igual que la recepción aceptada, para conservar trazabilidad.
- El conector con rombo de ParaRedirigir se interpreta como generalización de EstadoDocumentacion. Se conserva la herencia EstadoDocumentacion → Estado, presente en la primera página, para compartir metadatos.
- En la primera secuencia hay retornos inconsistentes con las clases: se usan `String` para precinto, `CM` para origen y `TipoDocumentacion` para tipo. Las consultas de documentación devuelven objetos tipados; la boundary prepara su representación y realiza las consultas de presentación.
- Los eventos de captura se transmiten al gestor mediante parámetros de selección y confirmación. El servidor HTTP y `VistaRecepcion` adaptan la interacción de pantalla sin trasladar State al navegador.
- Asociar el historial recién creado materializa la colección del modelo, aunque la segunda secuencia no dibuje explícitamente esa agregación para Bolsín.

Estas diferencias deben tenerse presentes al presentar conjuntamente las referencias y la vista de implementación; no se afirma identidad literal del 100% entre los originales.

## Alcance y validación

La aplicación implementa el principal y A1/A2/A6, más cancelación. Los alternativos A3–A5 necesitan ampliar la selección por documento y no se ofrecen en pantalla. CU29 es una simulación identificada como tal. El login está fuera del alcance: la demostración requiere una única sesión abierta.

Se verifican 81 condiciones mediante `scripts/test.sh`, incluidas integración HTTP y persistencia en dos JVM distintas. La interfaz se comprobó en Firefox, incluida selección, A6, cancelación y formato móvil. PostgreSQL requiere comprobar el entorno y credenciales de la instalación correspondiente.

La entrega académica debe acompañarse de la presentación y defensa que solicita la cátedra, incluyendo la documentación de UX realizada por el grupo.
