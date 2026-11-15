package modelo;

import java.time.LocalDateTime;

/**
 * Estado "En Bolsin Enviado" de un REMITO. Es distinto del de la documentacion
 * (EnBolsinEnviadoDocumentacion): en el diagrama las dos clases se llaman
 * EnBolsinEnviado, pero cada una resuelve transiciones propias de su ambito.
 */
public final class EnBolsinEnviadoRemito extends Estado {

    public EnBolsinEnviadoRemito() {
        super("En Bolsin Enviado", "Remito en bolsin enviado", AMBITO_REMITO);
    }

    @Override
    public void marcarRecibidoyAceptado(Remito remito, LocalDateTime fechaActual, Empleado empleado) {
        marcarRecibidayAceptadaDocumentacion(remito, fechaActual, empleado);
        Estado recibidoyAceptado = crearEstado();
        remito.setEstado(recibidoyAceptado);
    }

    public void marcarRecibidayAceptadaDocumentacion(Remito remito, LocalDateTime fechaActual, Empleado empleado) {
        for (DetalleRemito detalle : remito.obtenerDatosDocumentacion()) {
            detalle.marcarRecibidayAceptadaDocumentacion(fechaActual, empleado);
        }
    }

    public Estado crearEstado() {
        return new RecibidoyAceptado();
    }
}
