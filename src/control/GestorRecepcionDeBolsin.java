package control;

import boundary.PantallaNotificacionRecepcionBolsin;
import boundary.PantallaRecepcionDeBolsin;
import modelo.Bolsin;
import modelo.CM;
import modelo.Empleado;
import modelo.Estado;
import modelo.Remito;
import modelo.Sesion;
import persistencia.PersistenciaException;
import persistencia.RepositorioRecepcion;

import java.time.LocalDateTime;
import java.util.List;

public class GestorRecepcionDeBolsin {

    private final Sesion sesionActiva;
    private final PantallaRecepcionDeBolsin pantalla;

    private Empleado empleadoLogueado;
    private CM cmEmpleado;
    private List<Bolsin> bolsines;
    private Bolsin bolsinSeleccionado;
    private List<Remito> remitos;
    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoyAceptado;

    public GestorRecepcionDeBolsin(Sesion sesionActiva, PantallaRecepcionDeBolsin pantalla) {
        this.sesionActiva = sesionActiva;
        this.pantalla = pantalla;
    }

    public void iniciarCU28() {
        obtenerEmpleadoLogueado();
        pantalla.mostrarCMEmpleadoLogueado(empleadoLogueado);
        buscarBolsinesEnEstadoEnviadoParaCMEmpleado();
        pantalla.mostrarCMSeleccion(bolsines);
    }

    private void obtenerEmpleadoLogueado() {
        empleadoLogueado = sesionActiva.obtenerEmpleadoEnSesion();
        cmEmpleado = empleadoLogueado.getCM();
    }

    private void buscarBolsinesEnEstadoEnviadoParaCMEmpleado() {
        bolsines = Bolsin.buscarBolsinesEnEstadoEnviadoParaCMEmpleado(cmEmpleado.getNombre());
    }

    public void tomarBolsinSeleccionado(Bolsin seleccionado) {
        bolsinSeleccionado = seleccionado;
        buscarRemitosAsociadosBolsin();
        pantalla.mostrarOpcionesRecepcionBolsin(remitos);
    }

    private void buscarRemitosAsociadosBolsin() {
        remitos = bolsinSeleccionado.buscarRemitos();
    }

    public void tomarSeleccionRecepcionBolsin() {
        pantalla.solicitarConfirmacion();
    }

    public void tomarConfirmacion(boolean confirma) {
        if (!confirma) {
            return;
        }

        registrarRecepcionDeBolsin();

        if (!guardarCambios()) {
            return;
        }

        llamarCU29();
        pantalla.finCU();
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

    private LocalDateTime obtenerFechaActual() {
        return LocalDateTime.now();
    }

    // Persistencia de infraestructura: no agrega una interacción del CU28 al diagrama.
    private boolean guardarCambios() {
        try {
            new RepositorioRecepcion().guardar(bolsinSeleccionado, remitos);
            return true;
        } catch (PersistenciaException e) {
            pantalla.mostrarErrorGuardado(e.getMessage());
            return false;
        }
    }

    private void llamarCU29() {
        notificarCMOrigen();
    }

    private void notificarCMOrigen() {
        new PantallaNotificacionRecepcionBolsin().notificar(bolsinSeleccionado);
    }
}
