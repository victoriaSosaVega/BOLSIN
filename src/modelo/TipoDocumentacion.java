package modelo;

public class TipoDocumentacion {

    private String nombre;
    private String descripcion;

    public TipoDocumentacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }
}
