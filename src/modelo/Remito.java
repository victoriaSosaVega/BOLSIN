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
    private final EnBolsinEnviado estadoEnBolsinEnviado;

    public Remito(int numero, LocalDate fecha) {
        this.numero = numero;
        this.fecha = fecha;
        this.estadoEnBolsinEnviado = new EnBolsinEnviado();
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

    public void marcarRecibidoyAceptado(LocalDateTime fechaActual) {
        estadoEnBolsinEnviado.marcarRecibidoyAceptado(this, fechaActual);
    }
}
