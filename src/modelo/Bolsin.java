package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bolsin {

    // "Persistencia" simulada: repositorio estático en memoria (sin BD real).
    private static List<Bolsin> bolsines = new ArrayList<>();

    // Clave de la tabla bolsin (solo para persistencia).
    private Integer id;
    private int numeroBolsin;
    private String numeroPrecinto;
    private LocalDate fecha;
    private double peso;
    private CM cmOrigen;
    private CM cmDestino;
    private final Enviado estadoEnviado; //luego cambiar por la clase padre
    private List<Remito> remitos = new ArrayList<>();
    private List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();

    public Bolsin(int numeroBolsin, String numeroPrecinto, LocalDate fecha, double peso, CM cmOrigen, CM cmDestino) {
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.fecha = fecha;
        this.peso = peso;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
        this.estadoEnviado = new Enviado(this);
        bolsines.add(this);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    // Lo usa la capa de persistencia para guardar los cambios de estado.
    public List<CambioEstadoBolsin> getCambiosEstado() {
        return cambiosEstado;
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

    CambioEstadoBolsin buscarHistorial(LocalDateTime fechaActual, List<CambioEstadoBolsin> historial) {
        for (CambioEstadoBolsin cambio : historial) {
            if (cambio.esActual()) {
                return cambio;
            }
        }
        return null;
    }

    public void marcarRecibidoEnCMDestino(LocalDateTime fechaActual) {
        estadoEnviado.marcarRecibidoEnCMDestino(this, fechaActual, null, cambiosEstado);
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
