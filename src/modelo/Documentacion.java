package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Documentacion {

    // Clave de la tabla documentacion (solo para persistencia).
    private Integer id;
    private int numero;
    private String asunto;
    private LocalDate fechaPase;
    private TipoDocumentacion tipoDocumentacion;
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();
    private final EnBolsinEnviado estadoEnBolsinEnviado;

    public Documentacion(int numero, String asunto, LocalDate fechaPase, TipoDocumentacion tipoDocumentacion) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumentacion = tipoDocumentacion;
        this.estadoEnBolsinEnviado = new EnBolsinEnviado();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Lo usa la capa de persistencia para guardar los cambios de estado.
    public List<CambioEstadoDocumentacion> getCambiosEstado() {
        return cambiosEstado;
    }

    public String getAsunto() {
        return asunto;
    }

    public TipoDocumentacion getTipo() {
        return tipoDocumentacion;
    }

    public CambioEstadoDocumentacion getCambioEstadoActual() {
        for (CambioEstadoDocumentacion c : cambiosEstado) {
            if (c.esActual()) {
                return c;
            }
        }
        return null;
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambio) {
        cambiosEstado.add(cambio);
    }

    CambioEstadoDocumentacion buscarHistorial(LocalDateTime fechaActual,
                                                       List<CambioEstadoDocumentacion> historial) {
        for (CambioEstadoDocumentacion cambio : historial) {
            if (cambio.esActual()) {
                return cambio;
            }
        }
        return null;
    }

    private Estado crearEstado() {
        return new RecibidayAceptada();
    }

    private CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fechaActual, Estado estado) {
        return new CambioEstadoDocumentacion(fechaActual, estado);
    }

    private void cambiarEstado(Estado nuevoEstado, Empleado responsable, LocalDateTime fechaActual) {
        CambioEstadoDocumentacion actual = buscarHistorial(fechaActual, cambiosEstado);
        if (actual != null) {
            actual.setFechaHoraFin(fechaActual);
        }
        CambioEstadoDocumentacion nuevo = crearCambioEstado(fechaActual, nuevoEstado);
        nuevo.setEstado(nuevoEstado);
        agregarCambioEstado(nuevo);
    }

    public void marcarRecibidayAceptada(LocalDateTime fechaActual) {
        estadoEnBolsinEnviado.marcarRecibidayAceptada(
                this, fechaActual, null, cambiosEstado);
        Estado estadoActualizado = crearEstado();
        CambioEstadoDocumentacion nuevo = crearCambioEstado(fechaActual, estadoActualizado);
        nuevo.setEstado(estadoActualizado);
        agregarCambioEstado(nuevo);
    }

    // --- CU31 Registrar Revisión de Documentación: vuelve a estado Registrada
    // para poder vincularse a un nuevo remito ---
    public void reenviar(Estado estadoRegistrada, Empleado responsable) {
        cambiarEstado(estadoRegistrada, responsable, LocalDateTime.now());
    }

    // --- CU31: se considera que ya no es relevante su envío ---
    public void darDeBaja(Estado estadoDeBaja, Empleado responsable) {
        cambiarEstado(estadoDeBaja, responsable, LocalDateTime.now());
    }

    // --- CU15/CU16 Generar/Modificar Remito: pertenecen a otro caso de uso,
    // no ejercitados por CU28; quedan como puntos de extensión. ---
    public void incluirEnRemito() {
        // TODO: usado por CU15 Generar Remito / CU16 Modificar Remito
    }

    public void quitarDeRemito() {
        // TODO: usado por CU16 Modificar Remito / CU17 Cancelar Remito
    }

    // --- CU19/CU20 Generar/Modificar Bolsín: idem, otro caso de uso. ---
    public void incluirEnBolsin() {
        // TODO: usado por CU19 Generar Bolsín / CU20 Modificar Bolsín
    }

    public void quitarDeBolsin() {
        // TODO: usado por CU20 Modificar Bolsín / CU21 Eliminar Bolsín
    }

    // --- CU27 Registrar el retiro de bolsines: pasa a "en bolsín enviado" ---
    public void retirar(Estado estadoEnBolsinEnviado, Empleado responsable) {
        cambiarEstado(estadoEnBolsinEnviado, responsable, LocalDateTime.now());
    }
}
