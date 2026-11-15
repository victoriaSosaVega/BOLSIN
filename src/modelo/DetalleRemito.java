package modelo;

import java.time.LocalDateTime;

public class DetalleRemito {

    private String areaCMDestino;
    private Documentacion documentacion;

    public DetalleRemito(String areaCMDestino, Documentacion documentacion) {
        this.areaCMDestino = areaCMDestino;
        this.documentacion = documentacion;
    }

    public Documentacion obtenerDatosDocumentacion() {
        return documentacion;
    }

    public void marcarRecibidayAceptadaDocumentacion(LocalDateTime fechaActual, Empleado empleado) {
        documentacion.marcarRecibidayAceptada(fechaActual, empleado);
    }
}
