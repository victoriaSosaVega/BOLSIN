package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Documentacion que se debe redirigir a otra area de la CM destino. */
public final class ParaRedirigir extends Estado {

    public ParaRedirigir() {
        super("Para Redirigir", "Documentacion a redirigir a otra area de la CMC", AMBITO_DOCUMENTACION);
    }

    @Override
    public void marcarRecibidayRechazada(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new RecibidaYRechazada(), fechaActual, empleado, historiales);
    }

    @Override
    public void marcarRecibidayAceptada(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new RecibidayAceptada(), fechaActual, empleado, historiales);
    }
}
