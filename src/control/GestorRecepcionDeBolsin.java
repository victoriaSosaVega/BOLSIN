package control;

import boundary.PantallaNotificacionRecepcionBolsin;
import boundary.VistaRecepcion;
import persistencia.RespaldoRecepcion;
import modelo.Bolsin;
import modelo.CM;
import modelo.Empleado;
import modelo.Estado;
import modelo.Remito;
import modelo.Sesion;
import persistencia.RepositorioRecepcion;

import java.time.LocalDateTime;
import java.util.List;

public class GestorRecepcionDeBolsin {

    private final Sesion sesionActiva;
    private final VistaRecepcion pantalla;
    private final RepositorioRecepcion repositorio;
    private final java.util.function.Consumer<Bolsin> notificador;
    private boolean finalizado;

    private Empleado empleadoLogueado;
    private CM cmEmpleado;
    private List<Bolsin> bolsines;
    private Bolsin bolsinSeleccionado;
    private List<Remito> remitos;
    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoyAceptado;

    public GestorRecepcionDeBolsin(Sesion sesionActiva, VistaRecepcion pantalla) {
        this(sesionActiva, pantalla, new RepositorioRecepcion(),
                new PantallaNotificacionRecepcionBolsin()::notificar);
    }

    public GestorRecepcionDeBolsin(Sesion sesionActiva, VistaRecepcion pantalla,
            RepositorioRecepcion repositorio, java.util.function.Consumer<Bolsin> notificador) {
        this.sesionActiva = sesionActiva;
        this.pantalla = pantalla;
        this.repositorio = repositorio;
        this.notificador = notificador;
    }

    public void iniciarCU28() {
        obtenerEmpleadoLogueado();
        pantalla.mostrarCMEmpleadoLogueado(empleadoLogueado);
        buscarBolsinesEnEstadoEnviadoParaCMEmpleado();
        pantalla.mostrarCMSeleccion(bolsines);
    }

    private Empleado obtenerEmpleadoLogueado() {
        empleadoLogueado = sesionActiva.obtenerEmpleadoEnSesion();
        cmEmpleado = empleadoLogueado.getCM();
        return empleadoLogueado;
    }

    private List<Bolsin> buscarBolsinesEnEstadoEnviadoParaCMEmpleado() {
        bolsines = new java.util.ArrayList<>();
        for (Bolsin bolsin : Bolsin.getBolsines()) {
            if (bolsin.esEstadoEnviado() && bolsin.esCMDestino(cmEmpleado.getNombre())) {
                bolsines.add(bolsin);
            }
        }
        return bolsines;
    }

    public void tomarBolsinSeleccionado(Bolsin seleccionado) {
        if (finalizado || seleccionado == null || !bolsines.contains(seleccionado)) {
            pantalla.mostrarAviso("Seleccione un bolsín enviado a su CM.");
            return;
        }
        bolsinSeleccionado = seleccionado;
        buscarRemitosAsociadosBolsin(seleccionado);
        pantalla.mostrarOpcionesRecepcionBolsin(remitos);
    }

    private void buscarRemitosAsociadosBolsin(Bolsin seleccionado) {
        remitos = seleccionado.buscarRemitos();
    }

    public void tomarSeleccionRecepcionBolsin() {
        pantalla.solicitarConfirmacion();
    }

    public void tomarConfirmacion(boolean confirma) {
        if (finalizado || !confirma || bolsinSeleccionado == null) {
            return; // A6: no cambia objetos, base de datos ni notificaciones.
        }
        RespaldoRecepcion respaldo = new RespaldoRecepcion(bolsinSeleccionado);
        try {
            registrarRecepcionDeBolsin();
            guardarCambios();
        } catch (RuntimeException e) {
            respaldo.restaurar();
            pantalla.mostrarErrorGuardado(e.getMessage());
            return;
        }
        finalizado = true;
        try {
            llamarCU29();
        } catch (RuntimeException e) {
            pantalla.mostrarAviso("La recepción quedó guardada. No se pudo completar la notificación: " + e.getMessage());
        }
        finCU();
    }

    private void finCU() { pantalla.finCU(); }

    public void filtrarBolsines(String precinto, String origen) {
        if (finalizado) return;
        String p = precinto.strip();
        String o = origen.strip();
        List<Bolsin> encontrados = bolsines.stream()
                .filter(b -> p.isEmpty() || p.equalsIgnoreCase(b.getNumeroPrecinto()))
                .filter(b -> o.isEmpty() || b.getCMOrigen().getNombre().toLowerCase(java.util.Locale.ROOT)
                        .contains(o.toLowerCase(java.util.Locale.ROOT)))
                .toList();
        bolsinSeleccionado = null;
        pantalla.mostrarCMSeleccion(encontrados);
        if (encontrados.isEmpty() && (!p.isEmpty() || !o.isEmpty())) {
            pantalla.mostrarAviso(!p.isEmpty() && !o.isEmpty()
                    ? "No se encontraron bolsines para ese precinto y CM de origen."
                    : !p.isEmpty() ? "No se encuentra bolsín con el número de precinto ingresado (A1)."
                    : "No se encuentra bolsín con la CM de origen ingresada (A2).");
        }
    }

    public void cancelarCU() {
        finalizado = true;
        pantalla.cancelarCU();
    }

    public void registrarRecepcionDeBolsin() {
        buscarEstadoRecibidoEnCMDestinoBolsin();
        buscarEstadoRecibidoyAceptadoRemito();
        LocalDateTime fechaActual = obtenerFechaActual();
        bolsinSeleccionado.marcarRecibidoEnCMDestino(fechaActual, empleadoLogueado,
                estadoRecibidoEnCMDestino, estadoRecibidoyAceptado);
    }

    // El estado de la documentacion no se busca: lo crea el propio estado (patron State).
    private void buscarEstadoRecibidoEnCMDestinoBolsin() {
        for (Estado estado : Estado.getEstados()) {
            if (estado.esAmbitoBolsin() && estado.esRecibidoEnCMDestino()) {
                estadoRecibidoEnCMDestino = estado;
                return;
            }
        }
        throw new IllegalStateException("No existe el estado 'Recibido en CM Destino' (Bolsin)");
    }

    private void buscarEstadoRecibidoyAceptadoRemito() {
        for (Estado estado : Estado.getEstados()) {
            if (estado.esAmbitoRemito() && estado.esRecibidoyAceptado()) {
                estadoRecibidoyAceptado = estado;
                return;
            }
        }
        throw new IllegalStateException("No existe el estado 'Recibido y Aceptado' (Remito)");
    }

    public LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now(java.time.ZoneId.of("America/Argentina/Cordoba"));
    }

    // Persistencia de infraestructura: no agrega una interacción del CU28 al diagrama.
    private void guardarCambios() {
        repositorio.guardar(bolsinSeleccionado, remitos);
    }

    private void llamarCU29() {
        notificarCMOrigen();
    }

    private void notificarCMOrigen() {
        notificador.accept(bolsinSeleccionado);
    }
}
