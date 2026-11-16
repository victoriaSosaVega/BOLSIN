package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Documentacion incluida en un bolsin que todavia no fue retirado. */
public final class EnBolsinSaliente extends EstadoDocumentacion {

    public EnBolsinSaliente() {
        super("En Bolsin Saliente", "Documentacion en bolsin saliente");
    }

    @Override
    public void quitarDeBolsin(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new EnRemito(), fechaActual, empleado, historiales);
    }

    @Override
    public void retirar(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new EnBolsinEnviado(), fechaActual, empleado, historiales);
    }
}
