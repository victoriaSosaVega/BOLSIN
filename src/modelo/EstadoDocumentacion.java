package modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estado de una Documentacion: raiz del patron State. Cada subclase concreta
 * implementa solo las transiciones que permite; el resto lanza IllegalStateException.
 *
 * La Documentacion delega en su estado actual y se pasa a si misma por parametro,
 * de modo que los estados no guardan referencia a ella.
 */
public abstract class EstadoDocumentacion extends Estado {

    protected EstadoDocumentacion(String nombre, String descripcion) {
        super(nombre, descripcion, AMBITO_DOCUMENTACION, false);
    }

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

    // Cierra el cambio de estado vigente de la documentacion, registra el nuevo y
    // actualiza el estado de la documentacion. Lo comparten las transiciones que no
    // son las del CU28 (esta ultima, en EnBolsinEnviado, muestra cada paso).
    protected void cambiarEstadoDocumentacion(Documentacion documentacion, EstadoDocumentacion nuevoEstado,
            LocalDateTime fechaActual, Empleado empleado, List<CambioEstadoDocumentacion> historiales) {
        for (CambioEstadoDocumentacion cambio : historiales) {
            if (cambio.esActual()) {
                cambio.setFechaHoraFin(fechaActual);
            }
        }
        documentacion.setEstado(nuevoEstado);
        documentacion.agregarCambioEstado(new CambioEstadoDocumentacion(fechaActual, nuevoEstado, empleado));
    }

    private void noPermitida(String operacion) {
        throw new IllegalStateException("La operacion " + operacion
                + " no esta permitida en el estado " + getNombre() + " (" + getAmbito() + ")");
    }
}
