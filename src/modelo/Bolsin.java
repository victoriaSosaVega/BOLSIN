package modelo;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class Bolsin {

    // "Persistencia" simulada: repositorio estatico en memoria (sin BD real).
    private static List<Bolsin> bolsines = new ArrayList<>();

    // Clave de la tabla bolsin (solo para persistencia).
    private Integer id;
    private int numeroBolsin;
    private String numeroPrecinto;
    private LocalDate fecha;
    private double peso;
    private CM cmOrigen;
    private CM cmDestino;
    private List<Remito> remitos = new ArrayList<>();
    private List<CambioEstadoBolsin> cambiosEstado = new ArrayList<>();
    // Cambio de estado vigente: se guarda al encontrarlo en la busqueda (esEstadoEnviado).
    private CambioEstadoBolsin cambioEstadoActual;

    public Bolsin(int numeroBolsin, String numeroPrecinto, LocalDate fecha, double peso, CM cmOrigen, CM cmDestino) {
        this.numeroBolsin = numeroBolsin;
        this.numeroPrecinto = numeroPrecinto;
        this.fecha = fecha;
        this.peso = peso;
        this.cmOrigen = cmOrigen;
        this.cmDestino = cmDestino;
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
        if (cambio.esActual()) {
            cambioEstadoActual = cambio;
        }
    }

    public boolean esCMDestino(String nombreCMEmpleado) {
        return cmDestino.getNombre().equals(nombreCMEmpleado);
    }

    // Devuelve el cambio de estado vigente. Si ya se encontro en la busqueda no
    // se vuelve a buscar; si dejo de ser el vigente (o no se busco), se recalcula.
    public CambioEstadoBolsin getCambioEstadoActual() {
        if (cambioEstadoActual != null && cambioEstadoActual.esActual()) {
            return cambioEstadoActual;
        }
        cambioEstadoActual = null;
        for (CambioEstadoBolsin c : cambiosEstado) {
            if (c.esActual()) {
                cambioEstadoActual = c;
                break;
            }
        }
        return cambioEstadoActual;
    }

    public boolean esEstadoEnviado() {
        CambioEstadoBolsin actual = getCambioEstadoActual();
        return actual != null && actual.esEnviado();
    }

    public List<Remito> buscarRemitos() {
        return remitos;
    }

    // Bolsin cambia de estado sin patron State: cierra el cambio vigente (el que se
    // encontro al buscar los bolsines Enviado), registra el nuevo con el estado que le
    // pasa el Gestor y despues marca recibido y aceptado cada remito.
    public void marcarRecibidoEnCMDestino(LocalDateTime fechaActual, Empleado empleado,
                                          Estado estadoRecibidoEnCMDestino, Estado estadoRecibidoyAceptado) {
        if (!esEstadoEnviado()) {
            throw new IllegalStateException("El bolsin " + numeroBolsin + " no esta en estado Enviado");
        }
        CambioEstadoBolsin actual = getCambioEstadoActual();
        actual.setFechaHoraFin(fechaActual);

        CambioEstadoBolsin nuevo = crearCambioEstado(fechaActual, estadoRecibidoEnCMDestino, empleado);
        agregarCambioEstado(nuevo);

        marcarRecibidoyAceptadoRemito(fechaActual, empleado, estadoRecibidoyAceptado);
    }

    private CambioEstadoBolsin crearCambioEstado(LocalDateTime fechaActual, Estado estado, Empleado empleado) {
        return new CambioEstadoBolsin(fechaActual, estado, empleado);
    }

    private void marcarRecibidoyAceptadoRemito(LocalDateTime fechaActual, Empleado empleado, Estado estadoRemito) {
        for (Remito remito : remitos) {
            remito.marcarRecibidoyAceptado(fechaActual, empleado, estadoRemito);
        }
    }

    // Experto: Bolsin es quien tiene la CM destino y el estado, asi que es
    // quien resuelve la busqueda para el Gestor (persistencia simulada).
    public static List<Bolsin> buscarBolsinesEnEstadoEnviadoParaCMEmpleado(String nombreCMEmpleado) {
        List<Bolsin> resultado = new ArrayList<>();
        for (Bolsin b : bolsines) {
            if (b.esEstadoEnviado() && b.esCMDestino(nombreCMEmpleado)) {
                resultado.add(b);
            }
        }
        return resultado;
    }
}
