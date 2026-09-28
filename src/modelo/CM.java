package modelo;

public class CM {

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
