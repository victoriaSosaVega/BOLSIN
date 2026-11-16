package modelo;

import java.time.LocalDateTime;

/** Historial de estados del bolsin: cada registro es un estado con su periodo y el responsable. */
public class CambioEstadoBolsin {

    // Clave de la tabla cambio_estado_bolsin (solo para persistencia).
    private Integer id;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Estado estado;
    private Empleado responsable;

    // new() del diagrama de secuencia.
    public CambioEstadoBolsin(LocalDateTime fechaActual, Estado estado, Empleado responsable) {
        this.fechaHoraInicio = fechaActual;
        this.estado = estado;
        this.responsable = responsable;
    }

    // Solo para reconstruir un cambio de estado ya guardado en la base.
    public CambioEstadoBolsin(Estado estado, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin,
                              Empleado responsable) {
        this.estado = estado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
        this.responsable = responsable;
    }

    // Es el cambio de estado vigente: todavia no tiene fecha de fin.
    public boolean esActual() {
        return fechaHoraFin == null;
    }

    public boolean esEnviado() {
        return estado.esEnviado();
    }

    public void setFechaHoraFin(LocalDateTime fechaActual) {
        this.fechaHoraFin = fechaActual;
    }

    public Estado getEstado() {
        return estado;
    }

    public Empleado getResponsable() {
        return responsable;
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
