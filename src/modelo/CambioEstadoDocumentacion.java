package modelo;

import java.time.LocalDateTime;

public class CambioEstadoDocumentacion {

    // Clave de la tabla cambio_estado_documentacion (solo para persistencia).
    private Integer id;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private Estado estado;

    // new() del diagrama de secuencia.
    public CambioEstadoDocumentacion(LocalDateTime fechaActual, Estado estado) {
        this.fechaHoraInicio = fechaActual;
        this.estado = estado;
    }

    // Solo para reconstruir un cambio de estado ya guardado en la base. La
    // tabla no guarda el responsable, por eso queda en null.
    public CambioEstadoDocumentacion(Estado estado, LocalDateTime fechaHoraInicio, LocalDateTime fechaHoraFin) {
        this.estado = estado;
        this.fechaHoraInicio = fechaHoraInicio;
        this.fechaHoraFin = fechaHoraFin;
    }

    public boolean esActual() {
        return fechaHoraFin == null;
    }

    public void setFechaHoraFin(LocalDateTime fechaActual) {
        this.fechaHoraFin = fechaActual;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
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
