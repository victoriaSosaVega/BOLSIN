package modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estado "En Bolsin Enviado" de una DOCUMENTACION: el bolsin que la lleva ya salio
 * hacia la CM destino. Ejecuta la recepcion del CU28 (marcarRecibidayAceptada) y
 * las demas salidas posibles (no recibida, rechazada, para redirigir).
 */
public final class EnBolsinEnviado extends EstadoDocumentacion {

    public EnBolsinEnviado() {
        super("En Bolsin Enviado", "Documentacion en bolsin enviado");
    }

    @Override
    public void marcarRecibidayAceptada(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                        List<CambioEstadoDocumentacion> historiales) {
        CambioEstadoDocumentacion actual = buscarHistorial(fechaActual, historiales);
        if (actual != null && actual.esActual()) {
            actual.setFechaHoraFin(fechaActual);
        }
        EstadoDocumentacion recibidayAceptada = crearEstado();
        CambioEstadoDocumentacion nuevo = crearCambioEstado(fechaActual, recibidayAceptada, empleado);
        documentacion.setEstado(recibidayAceptada);
        documentacion.agregarCambioEstado(nuevo);
    }

    @Override
    public void marcarNoRecibida(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                 List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new NoRecibida(), fechaActual, empleado, historiales);
    }

    @Override
    public void marcarRecibidayRechazada(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                         List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new RecibidaYRechazada(), fechaActual, empleado, historiales);
    }

    @Override
    public void redirigir(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                          List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new ParaRedirigir(), fechaActual, empleado, historiales);
    }

    public CambioEstadoDocumentacion buscarHistorial(LocalDateTime fechaActual,
                                                     List<CambioEstadoDocumentacion> historiales) {
        for (CambioEstadoDocumentacion cambio : historiales) {
            if (cambio.esActual()) {
                return cambio;
            }
        }
        return null;
    }

    public EstadoDocumentacion crearEstado() {
        return new RecibidayAceptada();
    }

    public CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fechaActual, EstadoDocumentacion estado,
                                                       Empleado empleado) {
        return new CambioEstadoDocumentacion(fechaActual, estado, empleado);
    }
}
