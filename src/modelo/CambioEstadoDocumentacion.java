package modelo;

import java.time.LocalDateTime;

public class CambioEstadoDocumentacion {

    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Estado estado;
    private Empleado responsable;

    public CambioEstadoDocumentacion(Estado estado, Empleado responsable) {
        this.estado = estado;
        this.responsable = responsable;
        this.fechaHoraInicio = LocalDateTime.now();
    }

    public boolean esActual() {
        return fechaHoraFin == null;
    }

    public void setFechaFin() {
        this.fechaHoraFin = LocalDateTime.now();
    }

    public Estado getEstado() {
        return estado;
    }
}
