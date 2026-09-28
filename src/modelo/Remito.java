package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Remito {

    private int numero;
    private LocalDate fecha;
    private List<DetalleRemito> detalleRemito = new ArrayList<>();
    private Estado estado;

    public Remito(int numero, LocalDate fecha) {
        this.numero = numero;
        this.fecha = fecha;
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

    public void marcarRecibidoyAceptado(Estado estadoRemito, Estado estadoDocumentacion, Empleado responsable) {
        setEstado(estadoRemito);
        for (DetalleRemito detalle : detalleRemito) {
            marcarRecibidayAceptadaDocumentacion(detalle, estadoDocumentacion, responsable);
        }
    }

    public void marcarRecibidayAceptadaDocumentacion(DetalleRemito detalle, Estado estadoDocumentacion, Empleado responsable) {
        detalle.marcarRecibidayAceptadaDocumentacion(estadoDocumentacion, responsable);
    }
}
