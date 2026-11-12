package modelo;

public class CM {

    // Clave de la tabla cm (solo para persistencia; no forma parte del diagrama).
    private Integer id;
    private String codigo;
    private String nombre;
    private String direccion;
    private String email;
    private String telefono;

    public CM(String codigo, String nombre, String direccion, String email, String telefono) {
        this.codigo = codigo;
        this.nombre = nombre;
        this.direccion = direccion;
        this.email = email;
        this.telefono = telefono;
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

    public String getCodigo() {
        return codigo;
    }

    public String getEmail() {
        return email;
    }

    public boolean esCMEmpleado(CM otra) {
        return this.codigo.equals(otra.codigo);
    }
}
