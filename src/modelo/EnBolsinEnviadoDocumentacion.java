package modelo;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Estado "En Bolsin Enviado" de una DOCUMENTACION. Es distinto del del remito
 * (EnBolsinEnviadoRemito): en el diagrama las dos clases se llaman
 * EnBolsinEnviado, pero cada una resuelve transiciones propias de su ambito.
 */
public final class EnBolsinEnviadoDocumentacion extends Estado {

    public EnBolsinEnviadoDocumentacion() {
        super("En Bolsin Enviado", "Documentacion en bolsin enviado", AMBITO_DOCUMENTACION);
    }

    @Override
    public void marcarRecibidayAceptada(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
                                        List<CambioEstadoDocumentacion> historiales) {
        CambioEstadoDocumentacion actual = buscarHistorial(fechaActual, historiales);
        if (actual != null && actual.esActual()) {
            actual.setFechaHoraFin(fechaActual);
        }
        Estado recibidayAceptada = crearEstado();
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

    public Estado crearEstado() {
        return new RecibidayAceptada();
    }

    public CambioEstadoDocumentacion crearCambioEstado(LocalDateTime fechaActual, Estado estado, Empleado empleado) {
        return new CambioEstadoDocumentacion(fechaActual, estado, empleado);
    }
}
