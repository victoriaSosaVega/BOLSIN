package persistencia;

import modelo.*;
import java.util.ArrayList;
import java.util.List;

/** Restaura el grafo cargado si la operación o la transacción falla. No es un estado de negocio. */
public final class RespaldoRecepcion {
    private final List<Runnable> restauraciones = new ArrayList<>();

    public RespaldoRecepcion(Bolsin bolsin) {
        var cambios = new ArrayList<>(bolsin.getCambiosEstado());
        for (var cambio : cambios) {
            var fin = cambio.getFechaHoraFin();
            restauraciones.add(() -> cambio.setFechaHoraFin(fin));
        }
        restauraciones.add(() -> {
            bolsin.getCambiosEstado().clear();
            bolsin.getCambiosEstado().addAll(cambios);
        });
        for (var remito : bolsin.buscarRemitos()) {
            var estado = remito.getEstado();
            restauraciones.add(() -> remito.setEstado(estado));
            for (var detalle : remito.obtenerDatosDocumentacion()) {
                var doc = detalle.obtenerDatosDocumentacion();
                var estadoDoc = doc.getEstado();
                var historial = new ArrayList<>(doc.getCambiosEstado());
                for (var cambio : historial) {
                    var fin = cambio.getFechaHoraFin();
                    restauraciones.add(() -> cambio.setFechaHoraFin(fin));
                }
                restauraciones.add(() -> {
                    doc.setEstado(estadoDoc);
                    doc.getCambiosEstado().clear();
                    doc.getCambiosEstado().addAll(historial);
                });
            }
        }
    }

    public void restaurar() { restauraciones.forEach(Runnable::run); }
}
