package modelo;

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

    public void marcarRecibidayAceptadaDocumentacion(Estado estadoDocumentacion, Empleado responsable) {
        documentacion.marcarRecibidayAceptada(estadoDocumentacion, responsable);
    }
}
