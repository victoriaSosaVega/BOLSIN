package boundary;

import modelo.*;
import java.util.*;

/** Boundary web del CU28. El gestor entrega los mismos mensajes de la realización. */
public final class PantallaRecepcionDeBolsin implements VistaRecepcion {
    public int paso = 1;
    public String aviso = "";
    public String error = "";
    public boolean finalizado;
    public boolean cancelado;
    public Bolsin seleccionado;
    private Empleado empleado;
    private List<Bolsin> bolsines = List.of();
    private List<Remito> remitos = List.of();

    public void mostrarCMEmpleadoLogueado(Empleado e) { empleado = e; }
    public void mostrarCMSeleccion(List<Bolsin> b) { bolsines = List.copyOf(b); paso = 2; }
    public void mostrarOpcionesRecepcionBolsin(List<Remito> r) { remitos = List.copyOf(r); paso = 3; }
    public void solicitarConfirmacion() { paso = 4; }
    public void mostrarErrorGuardado(String detalle) { error = detalle; }
    public void mostrarAviso(String mensaje) { aviso = mensaje; }
    public void finCU() { finalizado = true; paso = 5; }
    public void cancelarCU() { cancelado = true; finalizado = true; paso = 5; }
    public void limpiarMensajes() { aviso = ""; error = ""; }
    public List<Bolsin> getBolsinesMostrados() { return bolsines; }

    public Map<String, Object> datos() {
        Map<String, Object> datos = new LinkedHashMap<>();
        datos.put("paso", paso);
        datos.put("empleado", empleado.getNombre() + " " + empleado.getApellido());
        datos.put("cm", empleado.getCM().getNombre());
        datos.put("aviso", aviso);
        datos.put("error", error);
        datos.put("finalizado", finalizado);
        datos.put("cancelado", cancelado);
        datos.put("bolsines", bolsines.stream().map(this::datosBolsin).toList());
        datos.put("seleccionado", seleccionado == null ? null : datosBolsin(seleccionado));
        datos.put("remitos", remitos.stream().map(r -> Map.of("numero", r.getNumero(), "documentos",
                r.obtenerDatosDocumentacion().stream().map(detalle -> {
                    Documentacion d = detalle.obtenerDatosDocumentacion();
                    return Map.of("asunto", d.getAsunto(), "tipo", d.getTipo().getNombre(),
                            "estado", d.getEstado().getNombre());
                }).toList())).toList());
        return datos;
    }

    private Map<String, Object> datosBolsin(Bolsin b) {
        return Map.of("id", b.getId(), "numero", b.getNumeroBolsin(), "precinto", Objects.toString(b.getNumeroPrecinto(), ""),
                "origen", b.getCMOrigen().getNombre(), "estado", b.getCambioEstadoActual().getEstado().getNombre(),
                "cantidadRemitos", b.buscarRemitos().size(),
                "cantidadDocumentos", b.buscarRemitos().stream().mapToInt(r -> r.obtenerDatosDocumentacion().size()).sum());
    }
}
