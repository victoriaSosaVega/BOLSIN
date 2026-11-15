package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Documentacion incluida en un remito que todavia no esta en un bolsin. */
public final class EnRemito extends Estado {

    public EnRemito() {
        super("En Remito", "Documentacion incluida en un remito", AMBITO_DOCUMENTACION);
    }

    @Override
    public void incluirEnBolsin(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new EnBolsinSaliente(), fechaActual, empleado, historiales);
    }

    @Override
    public void quitarDeRemito(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new Registrada(), fechaActual, empleado, historiales);
    }
}
