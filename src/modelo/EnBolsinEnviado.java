package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Estado previo a la recepción, que coordina remitos y documentación. */
public final class EnBolsinEnviado extends Estado {

    public EnBolsinEnviado() {
        super("En Bolsin Enviado", "Estado de la documentación en el bolsín", AMBITO_DOCUMENTACION, false);
    }

    public void marcarRecibidoyAceptado(Remito remito, LocalDateTime fechaActual) {
        for (DetalleRemito detalle : remito.obtenerDatosDocumentacion()) {
            detalle.marcarRecibidayAceptadaDocumentacion(fechaActual);
        }
        remito.setEstado(new RecibidoyAceptado());
    }

    public void marcarRecibidayAceptada(Documentacion documentacion, LocalDateTime fechaActual,
                                         Empleado empleado,
                                         List<CambioEstadoDocumentacion> historiales) {
        CambioEstadoDocumentacion actual = documentacion.buscarHistorial(fechaActual, historiales);
        if (actual != null && actual.esActual()) {
            actual.setFechaHoraFin(fechaActual);
        }
    }
}
