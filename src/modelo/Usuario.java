package modelo;

public class Usuario {

    private String nombre;
    private String contrasenia;
    private Empleado empleado;

    public Usuario(String nombre, String contrasenia, Empleado empleado) {
        this.nombre = nombre;
        this.contrasenia = contrasenia;
        this.empleado = empleado;
    }

    public Empleado obtenerEmpleado() {
        return empleado;
    }
}
