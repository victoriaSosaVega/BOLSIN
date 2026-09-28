package modelo;

public class Empleado {

    private String nombre;
    private String apellido;
    private String email;
    private CM cm;

    public Empleado(String nombre, String apellido, String email, CM cm) {
        this.nombre = nombre;
        this.apellido = apellido;
        this.email = email;
        this.cm = cm;
    }

    public CM getCM() {
        return cm;
    }

    public String getNombre() {
        return nombre;
    }

    public String getApellido() {
        return apellido;
    }

    public String getEmail() {
        return email;
    }
}
