package modelo;

public class TipoDocumentacion {

    // Clave de la tabla tipo_documentacion (solo para persistencia).
    private Integer id;
    private String nombre;
    private String descripcion;

    public TipoDocumentacion(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }
}
