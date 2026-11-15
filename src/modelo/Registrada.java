package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Documentacion registrada, todavia sin remito. */
public final class Registrada extends Estado {

    public Registrada() {
        super("Registrada", "Documentacion registrada en el sistema", AMBITO_DOCUMENTACION);
    }

    @Override
    public void incluirEnRemito(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new EnRemito(), fechaActual, empleado, historiales);
    }
}
