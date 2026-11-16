package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Documentacion recibida que no corresponde a la CM destino. */
public final class RecibidaYRechazada extends EstadoDocumentacion {

    public RecibidaYRechazada() {
        super("Recibida y Rechazada", "Documentacion que no corresponde a la CM destino");
    }

    @Override
    public void reenviar(Documentacion documentacion, LocalDateTime fechaActual, Empleado empleado,
            List<CambioEstadoDocumentacion> historiales) {
        cambiarEstadoDocumentacion(documentacion, new Registrada(), fechaActual, empleado, historiales);
    }
}
