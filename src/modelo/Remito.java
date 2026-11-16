package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Remito {

    // Clave de la tabla remito (solo para persistencia).
    private Integer id;
    private int numero;
    private LocalDate fecha;
    private List<DetalleRemito> detalleRemito = new ArrayList<>();
    private Estado estado;

    public Remito(int numero, LocalDate fecha) {
        this.numero = numero;
        this.fecha = fecha;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public int getNumero() {
        return numero;
    }

    public void agregarDetalle(DetalleRemito detalle) {
        detalleRemito.add(detalle);
    }

    public List<DetalleRemito> obtenerDatosDocumentacion() {
        return detalleRemito;
    }

    public void setEstado(Estado estado) {
        this.estado = estado;
    }

    public Estado getEstado() {
        return estado;
    }

    // Remito cambia de estado sin patron State: se asigna el estado que le pasa el
    // Gestor y despues marca la documentacion de cada detalle.
    public void marcarRecibidoyAceptado(LocalDateTime fechaActual, Empleado empleado, Estado estadoRecibidoyAceptado) {
        setEstado(estadoRecibidoyAceptado);
        marcarRecibidayAceptadaDocumentacion(fechaActual, empleado);
    }

    private void marcarRecibidayAceptadaDocumentacion(LocalDateTime fechaActual, Empleado empleado) {
        for (DetalleRemito detalle : detalleRemito) {
            detalle.marcarRecibidayAceptadaDocumentacion(fechaActual, empleado);
        }
    }
}
