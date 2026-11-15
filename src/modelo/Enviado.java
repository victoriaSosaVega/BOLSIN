package modelo;

import java.time.LocalDateTime;

/** Estado del bolsin que ejecuta el evento de recepcion del CU28. */
public final class Enviado extends Estado {

    public Enviado() {
        super("Enviado", "Bolsin enviado por CM origen", AMBITO_BOLSIN);
    }

    @Override
    public boolean esEnviado() {
        return true;
    }

    @Override
    public void marcarRecibidoEnCMDestino(Bolsin bolsin, LocalDateTime fechaActual, Empleado empleado) {
        // El cambio de estado vigente es el que se encontro al buscar los bolsines en estado Enviado.
        CambioEstadoBolsin actual = bolsin.getCambioEstadoActual();
        actual.setFechaHoraFin(fechaActual);

        marcarRecibidoyAceptadoRemito(bolsin, fechaActual, empleado);

        Estado recibidoEnCMDestino = crearEstado();
        CambioEstadoBolsin nuevo = crearCambioEstado(fechaActual, recibidoEnCMDestino, empleado);
        bolsin.setEstado(recibidoEnCMDestino);
        bolsin.agregarCambioEstado(nuevo);
    }

    public void marcarRecibidoyAceptadoRemito(Bolsin bolsin, LocalDateTime fechaActual, Empleado empleado) {
        for (Remito remito : bolsin.buscarRemitos()) {
            remito.marcarRecibidoyAceptado(fechaActual, empleado);
        }
    }

    public Estado crearEstado() {
        return new RecibidoEnCMDestino();
    }

    public CambioEstadoBolsin crearCambioEstado(LocalDateTime fechaActual, Estado estado, Empleado empleado) {
        return new CambioEstadoBolsin(fechaActual, estado, empleado);
    }
}
