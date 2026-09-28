package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Bolsin {

    // "Persistencia" simulada: repositorio estático en memoria (sin BD real).
    private static List<Bolsin> bolsines = new ArrayList<>();

    private int numeroBolsin;
    private String numeroPrecinto;
    private LocalDate fecha;
    private double peso;
    private CM cmOrigen;
    private CM cmDestino;
    private List<Remito> remitos = new ArrayList<>();
    private List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();

    public Bolsin(int numeroBolsin, String numeroPrecinto, LocalDate fecha, double peso, CM cmOrigen, CM cmDestino) {
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.fecha = fecha;
        this.peso = peso;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        bolsines.add(this);
    }

    public int getNumeroBolsin() {
        return numeroBolsin;
    }

    public CM getCMOrigen() {
        return cmOrigen;
    }

    public String getNumeroPrecinto() {
        return numeroPrecinto;
    }

    public void agregarRemito(Remito remito) {
        remitos.add(remito);
    }

    public void agregarCambioEstado(CambioEstadoBolsin cambio) {
        cambiosEstado.add(cambio);
    }

    public boolean esCMEmpleado(CM cmEmpleado) {
        return cmDestino.esCMEmpleado(cmEmpleado);
    }

    public CambioEstadoBolsin getCambioEstadoActual() {
        for (CambioEstadoBolsin c : cambiosEstado) {
            if (c.esActual()) {
                return c;
            }
        }
        return null;
    }

    public boolean esEstadoEnviado() {
        CambioEstadoBolsin actual = getCambioEstadoActual();
        return actual != null && actual.esEnviado();
    }

    public List<Remito> buscarRemitos() {
        return remitos;
    }

    private CambioEstadoBolsin crearCambioEstado(Estado estado, Empleado responsable) {
        return new CambioEstadoBolsin(estado, responsable);
    }

    public void marcarRecibidoEnCMDestino(Estado nuevoEstado, Empleado responsable) {
        CambioEstadoBolsin actual = getCambioEstadoActual();
        if (actual != null) {
            actual.setFechaFin();
        }
        agregarCambioEstado(crearCambioEstado(nuevoEstado, responsable));
    }

    public void marcarRecibidoyAceptadoRemito(Estado estadoRemito, Estado estadoDocumentacion, Empleado responsable) {
        for (Remito remito : remitos) {
            remito.marcarRecibidoyAceptado(estadoRemito, estadoDocumentacion, responsable);
        }
    }

    // Experto: Bolsin es quien tiene la CM destino y el estado, así que es
    // quien resuelve la búsqueda para el Gestor (persistencia simulada).
    public static List<Bolsin> buscarBolsinesEnEstadoEnviadoParaCMEmpleado(CM cmEmpleado) {
        List<Bolsin> resultado = new ArrayList<>();
        for (Bolsin b : bolsines) {
            if (b.esEstadoEnviado() && b.esCMEmpleado(cmEmpleado)) {
                resultado.add(b);
            }
        }
        return resultado;
    }
}
