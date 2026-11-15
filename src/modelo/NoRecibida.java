package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Documentacion que figuraba en el remito pero no llego en el bolsin. */
public final class NoRecibida extends Estado {

    public NoRecibida() {
        super("No Recibida", "Documentacion que no llego en el bolsin", AMBITO_DOCUMENTACION);
    }

    @Override
    public void reenviar(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new Registrada(), fechaActual, empleado, historiales);
    }

    @Override
    public void darDeBaja(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new DeBaja(), fechaActual, empleado, historiales);
    }
}
