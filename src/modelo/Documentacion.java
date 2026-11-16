package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Documentacion {

    // Clave de la tabla documentacion (solo para persistencia).
    private Integer id;
    private int numero;
    private String asunto;
    private LocalDate fechaPase;
    private TipoDocumentacion tipoDocumentacion;
    private EstadoDocumentacion estado;
    private CM cmOrigen;
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    public Documentacion(int numero, String asunto, LocalDate fechaPase, TipoDocumentacion tipoDocumentacion) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumentacion = tipoDocumentacion;
    }

    public CM getCMOrigen() { return cmOrigen; }

    public void setCMOrigen(CM cmOrigen) { this.cmOrigen = cmOrigen; }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Lo usa la capa de persistencia para guardar los cambios de estado.
    public List<CambioEstadoDocumentacion> getCambiosEstado() {
        return cambiosEstado;
    }

    public String getAsunto() {
        return asunto;
    }

    public TipoDocumentacion getTipo() {
        return tipoDocumentacion;
    }

    public void setEstado(EstadoDocumentacion estado) {
        this.estado = estado;
    }

    public EstadoDocumentacion getEstado() {
        return estado;
    }

    public CambioEstadoDocumentacion getCambioEstadoActual() {
        for (CambioEstadoDocumentacion c : cambiosEstado) {
            if (c.esActual()) {
                return c;
            }
        }
        return null;
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambio) {
        cambiosEstado.add(cambio);
    }

    // --- Transiciones: la documentacion delega en su estado actual ---

    public void marcarRecibidayAceptada(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().marcarRecibidayAceptada(this, fechaActual, empleado, cambiosEstado);
    }

    public void marcarRecibidayRechazada(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().marcarRecibidayRechazada(this, fechaActual, empleado, cambiosEstado);
    }

    public void marcarNoRecibida(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().marcarNoRecibida(this, fechaActual, empleado, cambiosEstado);
    }

    public void redirigir(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().redirigir(this, fechaActual, empleado, cambiosEstado);
    }

    public void reenviar(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().reenviar(this, fechaActual, empleado, cambiosEstado);
    }

    public void darDeBaja(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().darDeBaja(this, fechaActual, empleado, cambiosEstado);
    }

    // Usadas por otros casos de uso (generar/modificar remito y bolsin, registrar retiro).
    public void incluirEnRemito(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().incluirEnRemito(this, fechaActual, empleado, cambiosEstado);
    }

    public void quitarDeRemito(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().quitarDeRemito(this, fechaActual, empleado, cambiosEstado);
    }

    public void incluirEnBolsin(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().incluirEnBolsin(this, fechaActual, empleado, cambiosEstado);
    }

    public void quitarDeBolsin(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().quitarDeBolsin(this, fechaActual, empleado, cambiosEstado);
    }

    public void retirar(LocalDateTime fechaActual, Empleado empleado) {
        estadoActual().retirar(this, fechaActual, empleado, cambiosEstado);
    }

    public void validarHistorial(LocalDateTime fecha) {
        if (fecha == null || cambiosEstado.stream().filter(CambioEstadoDocumentacion::esActual).count() != 1) {
            throw new IllegalStateException("La documentación debe tener exactamente un historial vigente.");
        }
        CambioEstadoDocumentacion actual = getCambioEstadoActual();
        if (actual.getEstado() != estado || fecha.isBefore(actual.getFechaHoraInicio())) {
            throw new IllegalStateException("El estado y el historial de la documentación son inconsistentes.");
        }
    }

    private EstadoDocumentacion estadoActual() {
        if (estado == null) {
            throw new IllegalStateException("La documentacion '" + asunto
                    + "' no tiene un estado vigente en la base. Revisá su historial de estados.");
        }
        return estado;
    }
}
