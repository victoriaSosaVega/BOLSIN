package modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estado abstracto del patron State. Cada subclase concreta implementa solo las
 * transiciones que permite; el resto lanza IllegalStateException.
 *
 * Los contextos (Bolsin, Remito, Documentacion) delegan en su estado actual y
 * se pasan a si mismos por parametro, de modo que los estados no guardan
 * referencias a ellos.
 */
public abstract class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";
    public static final String AMBITO_DOCUMENTACION = "Documentacion";

    // Clave de la tabla estado (solo para persistencia; no forma parte del diagrama).
    // Los estados creados por crearEstado() no la tienen: se resuelve por nombre y ambito al guardar.
    private Integer id;
    private final String nombre;
    private final String descripcion;
    private final String ambito;

    protected Estado(String nombre, String descripcion, String ambito) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ambito = ambito;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAmbito() {
        return ambito;
    }

    // Consultas polimorficas (reemplazan a las comparaciones por nombre).
    public boolean esEnviado() {
        return false;
    }

    // --- Transiciones de la documentacion ---

    public void incluirEnRemito(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                List<CambioEstadoDocumentacion> historiales) {
        noPermitida("incluirEnRemito");
    }

    public void quitarDeRemito(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                               List<CambioEstadoDocumentacion> historiales) {
        noPermitida("quitarDeRemito");
    }

    public void reenviar(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                         List<CambioEstadoDocumentacion> historiales) {
        noPermitida("reenviar");
    }

    public void incluirEnBolsin(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                List<CambioEstadoDocumentacion> historiales) {
        noPermitida("incluirEnBolsin");
    }

    public void quitarDeBolsin(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                               List<CambioEstadoDocumentacion> historiales) {
        noPermitida("quitarDeBolsin");
    }

    public void retirar(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                        List<CambioEstadoDocumentacion> historiales) {
        noPermitida("retirar");
    }

    public void marcarRecibidayAceptada(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                        List<CambioEstadoDocumentacion> historiales) {
        noPermitida("marcarRecibidayAceptada");
    }

    public void marcarRecibidayRechazada(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                         List<CambioEstadoDocumentacion> historiales) {
        noPermitida("marcarRecibidayRechazada");
    }

    public void marcarNoRecibida(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                 List<CambioEstadoDocumentacion> historiales) {
        noPermitida("marcarNoRecibida");
    }

    public void redirigir(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                          List<CambioEstadoDocumentacion> historiales) {
        noPermitida("redirigir");
    }

    public void darDeBaja(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                          List<CambioEstadoDocumentacion> historiales) {
        noPermitida("darDeBaja");
    }

    // --- Transiciones del bolsin y del remito ---

    public void marcarRecibidoEnCMDestino(Bolsin bolsin, LocalDateTime fechaActual, Empleado empleado) {
        noPermitida("marcarRecibidoEnCMDestino");
    }

    public void marcarRecibidoyAceptado(Remito remito, LocalDateTime fechaActual, Empleado empleado) {
        noPermitida("marcarRecibidoyAceptado");
    }

    // Cierra el cambio de estado vigente de la documentacion, registra el nuevo y
    // actualiza el estado de la documentacion. Lo comparten las transiciones que no
    // son las del CU28 (estas ultimas, en EnBolsinEnviadoDocumentacion, muestran cada paso).
    protected void cambiarEstadoDocumentacion(Documentacion documentacion, Estado nuevoEstado,
                                              LocalDateTime fechaActual, Empleado empleado,
                                              List<CambioEstadoDocumentacion> historiales) {
        for (CambioEstadoDocumentacion cambio : historiales) {
            if (cambio.esActual()) {
                cambio.setFechaHoraFin(fechaActual);
            }
        }
        documentacion.setEstado(nuevoEstado);
        documentacion.agregarCambioEstado(new CambioEstadoDocumentacion(fechaActual, nuevoEstado, empleado));
    }

    private void noPermitida(String operacion) {
        throw new IllegalStateException(
                "La operacion " + operacion + " no esta permitida en el estado " + nombre + " (" + ambito + ")");
    }
}
