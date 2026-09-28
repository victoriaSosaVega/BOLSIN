package modelo;

import java.time.LocalDateTime;

public class Sesion {

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Usuario usuario;

    public Sesion(Usuario usuario) {
        this.usuario = usuario;
        this.fechaHoraInicio = LocalDateTime.now();
    }

    public Empleado obtenerEmpleadoEnSesion() {
        return usuario.obtenerEmpleado();
    }

    public void cerrar() {
        this.fechaHoraFin = LocalDateTime.now();
    }
}
