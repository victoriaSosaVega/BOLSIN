package modelo;

import java.time.LocalDateTime;

public class CambioEstadoBolsin {

    // Clave de la tabla cambio_estado_bolsin (solo para persistencia).
    private Integer id;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Estado estado;
    private Empleado responsable;

    // new() del diagrama de secuencia.
    public CambioEstadoBolsin(Estado estado, Empleado responsable) {
        this.estado = estado;
        this.responsable = responsable;
        this.fechaHoraInicio = LocalDateTime.now();
    }

    // Solo para reconstruir un cambio de estado ya guardado en la base. La
    // tabla no guarda el responsable, por eso queda en null.
    public CambioEstadoBolsin(Estado estado, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        this.estado = estado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
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

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public LocalDateTime getFechaHoraInicio() {
        return fechaHoraInicio;
    }

    public LocalDateTime getFechaHoraFin() {
        return fechaHoraFin;
    }
}
