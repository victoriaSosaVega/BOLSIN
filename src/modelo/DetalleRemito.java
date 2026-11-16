package modelo;

import java.time.LocalDateTime;

public class DetalleRemito {

    private String areaCMCDestino;
    private Documentacion documentacion;

    public DetalleRemito(String areaCMCDestino, Documentacion documentacion) {
        this.areaCMCDestino = areaCMCDestino;
        this.documentacion = documentacion;
    }

    public Documentacion obtenerDatosDocumentacion() {
        return documentacion;
    }

    public void marcarRecibidayAceptadaDocumentacion(LocalDateTime fechaActual, Empleado empleado) {
        documentacion.marcarRecibidayAceptada(fechaActual, empleado);
    }
}
