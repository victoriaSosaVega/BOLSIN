package modelo;

import java.time.LocalDateTime;
import java.util.List;

/** Estado del bolsín que ejecuta el evento de recepción indicado por CU28. */
public final class Enviado extends Estado {

    private final Bolsin bolsin;

    public Enviado(Bolsin bolsin) {
        this(bolsin, buscarEstadoExistente());
    }

    private Enviado(Bolsin bolsin, Estado estado) {
        super(estado.getNombre(), estado.getDescripcion(), AMBITO_BOLSIN, false);
        this.bolsin = bolsin;
        setId(estado.getId());
    }

    private static Estado buscarEstadoExistente() {
        return Estado.getEstados().stream()
                .filter(estado -> estado.esAmbitoBolsin() && estado.esEnviado())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el estado Enviado"));
    }

    public void marcarRecibidoEnCMDestino(Bolsin bolsin, LocalDateTime fechaActual,
                                           Empleado empleado, List<CambioEstadoBolsin> historiales) {
        CambioEstadoBolsin actual = bolsin.buscarHistorial(fechaActual, historiales);
        if (actual != null && actual.esActual()) {
            actual.setFechaHoraFin(fechaActual);
        }
        marcarRecibidoyAceptadoRemito(fechaActual);
    }

    private void marcarRecibidoyAceptadoRemito(LocalDateTime fechaActual) {
        for (Remito remito : bolsin.buscarRemitos()) {
            remito.marcarRecibidoyAceptado(fechaActual);
        }
        Estado estadoActualizado = new RecibidoEnCMDestino();
        CambioEstadoBolsin nuevo = new CambioEstadoBolsin(fechaActual, estadoActualizado);
        nuevo.setEstado(estadoActualizado);
        bolsin.agregarCambioEstado(nuevo);
    }
}
