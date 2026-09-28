package modelo;

import java.time.LocalDateTime;

public class CambioEstadoBolsin {

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Estado estado;
    private Empleado responsable;

    public CambioEstadoBolsin(Estado estado, Empleado responsable) {
        this.estado = estado;
        this.responsable = responsable;
        this.fechaHoraInicio = LocalDateTime.now();
    }

    public boolean esActual() {
        return fechaHoraFin == null;
    }

    public boolean esEnviado() {
        return estado.esEnviado();
    }

    public void setFechaFin() {
        this.fechaHoraFin = LocalDateTime.now();
    }

    public Estado getEstado() {
        return estado;
    }
}
