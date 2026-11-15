package control;

import boundary.PantallaNotificacionRecepcionBolsin;
import boundary.PantallaRecepcionDeBolsin;
import modelo.Bolsin;
import modelo.CM;
import modelo.Empleado;
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
        LocalDateTime fechaActual = obtenerFechaActual();
        bolsinSeleccionado.marcarRecibidoEnCMDestino(fechaActual, empleadoLogueado);
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
