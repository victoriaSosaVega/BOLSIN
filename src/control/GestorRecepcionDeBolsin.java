package control;

import boundary.PantallaNotificacionRecepcionBolsin;
import boundary.PantallaRecepcionDeBolsin;
import modelo.Bolsin;
import modelo.CM;
import modelo.DetalleRemito;
import modelo.Documentacion;
import modelo.Empleado;
import modelo.Estado;
import modelo.Remito;
import modelo.Sesion;

import java.util.ArrayList;
import java.util.List;

public class GestorRecepcionDeBolsin {

    private Sesion sesionActiva;
    private PantallaRecepcionDeBolsin pantalla;

    // Atributos tal como figuran en el diagrama de clases.
    private Empleado empleadoLogueado;
    private CM cmEmpleado;
    private List<Bolsin> bolsines;
    private Bolsin bolsinSeleccionado;
    private List<Remito> remitos;
    private int opcionSeleccionada;
    private Estado estadoRecibidoEnCMDestino;
    private Estado estadoRecibidoyAceptado;
    private Estado estadoRecibidayAceptada;

    // Estado adicional para las opciones 2, 3 y 4 (flujos A3/A4/A5), que no
    // formaban parte del diagrama original de CU28 porque la secuencia
    // entregada/corregida solo modelaba el flujo principal (opcion 1).
    private List<Documentacion> documentacionSeleccionada = new ArrayList<>();

    public GestorRecepcionDeBolsin(Sesion sesionActiva, PantallaRecepcionDeBolsin pantalla) {
        this.sesionActiva = sesionActiva;
        this.pantalla = pantalla;
    }

    public void registrarRecepcionBolsin() {
        obtenerEmpleadoLogueado();
        pantalla.mostrarCMEmpleadoLogueado(cmEmpleado);
        buscarBolsinesEnEstadoEnviadoParaCMEmpleado();
        pantalla.mostrarCMSeleccion(bolsines);
    }

    private void obtenerEmpleadoLogueado() {
        empleadoLogueado = sesionActiva.obtenerEmpleadoEnSesion();
        cmEmpleado = empleadoLogueado.getCM();
    }

    private void buscarBolsinesEnEstadoEnviadoParaCMEmpleado() {
        bolsines = Bolsin.buscarBolsinesEnEstadoEnviadoParaCMEmpleado(cmEmpleado);
    }

    public void tomarBolsinSeleccionado(Bolsin seleccionado) {
        this.bolsinSeleccionado = seleccionado;
        buscarRemitosAsociadosBolsin();
        pantalla.mostrarOpcionesRecepcionBolsin(remitos);
    }

    private void buscarRemitosAsociadosBolsin() {
        remitos = bolsinSeleccionado.buscarRemitos();
    }

    public void tomarSeleccionRecepcionBolsin(int opcion) {
        this.opcionSeleccionada = opcion;
        if (opcion == 2 || opcion == 3 || opcion == 4) {
            // A3/A4/A5: hay que preguntarle al EB sobre que documentacion
            // aplica la situacion elegida, antes de pedir la confirmacion final.
            pantalla.mostrarDocumentacionParaSeleccion(opcion, obtenerTodaLaDocumentacion());
        } else {
            pantalla.solicitarConfirmacion();
        }
    }

    private List<Documentacion> obtenerTodaLaDocumentacion() {
        List<Documentacion> documentos = new ArrayList<>();
        for (Remito r : remitos) {
            for (DetalleRemito detalle : r.obtenerDatosDocumentacion()) {
                documentos.add(detalle.obtenerDatosDocumentacion());
            }
        }
        return documentos;
    }

    public void tomarDocumentacionSeleccionada(List<Documentacion> seleccionadas) {
        this.documentacionSeleccionada = seleccionadas;
        pantalla.solicitarConfirmacion();
    }

    public void tomarConfirmacion(boolean confirma) {
        if (!confirma) {
            // A6: el EB no confirma la selección de la opción de recepción a registrar.
            pantalla.habilitarPantalla();
            return;
        }

        switch (opcionSeleccionada) {
            case 1 -> actualizarDatosRecepcionBolsin();
            case 2 -> actualizarDatosRecepcionBolsinParcial();
            case 3 -> actualizarDatosRecepcionBolsinConRechazo();
            case 4 -> actualizarDatosRecepcionBolsinConRedireccion();
            default -> throw new IllegalStateException("Opcion de recepcion invalida: " + opcionSeleccionada);
        }

        llamarCU29();
        pantalla.finCU();
    }

    // Observacion 1: el EB puede cancelar el caso de uso cuando lo decida.
    public void cancelarCU() {
        pantalla.mostrarCancelacion();
    }

    // --- Opcion 1: el contenido del bolsin es igual al registrado.
    // "Los remitos y la documentacion asociada pasan al estado recibida y
    // aceptada." ---
    private void actualizarDatosRecepcionBolsin() {
        estadoRecibidoEnCMDestino = buscarEstadoRecibidoEnCMDestinoBolsin();
        estadoRecibidoyAceptado = buscarEstadoRecibidoyAceptadoRemito();
        estadoRecibidayAceptada = buscarEstadoRecibidayAceptadaDocumentacion();

        bolsinSeleccionado.marcarRecibidoEnCMDestino(estadoRecibidoEnCMDestino, empleadoLogueado);
        bolsinSeleccionado.marcarRecibidoyAceptadoRemito(estadoRecibidoyAceptado, estadoRecibidayAceptada, empleadoLogueado);
    }

    // --- Opcion 2 (A3): no se recibe toda la documentacion asociada a los
    // remitos que contiene el bolsin. "La documentacion que no llego queda
    // como no recibida (...) el remito queda como recibido y aceptado
    // parcial." La devolucion de esa documentacion a Registrada/De Baja la
    // hace despues el Encargado de Documentacion en CU31, no aqui. ---
    private void actualizarDatosRecepcionBolsinParcial() {
        estadoRecibidoEnCMDestino = buscarEstadoRecibidoEnCMDestinoBolsin();
        Estado estadoParcial = buscarEstadoRecibidoyAceptadoParcialRemito();
        estadoRecibidayAceptada = buscarEstadoRecibidayAceptadaDocumentacion();
        Estado estadoNoRecibida = buscarEstadoNoRecibidaDocumentacion();

        bolsinSeleccionado.marcarRecibidoEnCMDestino(estadoRecibidoEnCMDestino, empleadoLogueado);

        for (Remito r : remitos) {
            r.setEstado(estadoParcial);
            for (DetalleRemito detalle : r.obtenerDatosDocumentacion()) {
                Documentacion doc = detalle.obtenerDatosDocumentacion();
                if (documentacionSeleccionada.contains(doc)) {
                    doc.marcarNoRecibida(estadoNoRecibida, empleadoLogueado);
                } else {
                    doc.marcarRecibidayAceptada(estadoRecibidayAceptada, empleadoLogueado);
                }
            }
        }
    }

    // --- Opcion 3 (A4): existe documentacion que no corresponde al destino.
    // El "destino" contra el que se compara es, por definicion, el CM del
    // usuario logueado (cmEmpleado): es la CM que esta recibiendo el bolsin.
    // "La documentacion se marca como recibida y rechazada (...) debe ser
    // enviada de vuelta al origen, para asociarse a un nuevo remito." ---
    private void actualizarDatosRecepcionBolsinConRechazo() {
        estadoRecibidoEnCMDestino = buscarEstadoRecibidoEnCMDestinoBolsin();
        estadoRecibidoyAceptado = buscarEstadoRecibidoyAceptadoRemito();
        estadoRecibidayAceptada = buscarEstadoRecibidayAceptadaDocumentacion();
        Estado estadoRechazada = buscarEstadoRecibidayRechazadaDocumentacion();

        bolsinSeleccionado.marcarRecibidoEnCMDestino(estadoRecibidoEnCMDestino, empleadoLogueado);

        for (Remito r : remitos) {
            r.setEstado(estadoRecibidoyAceptado);
            for (DetalleRemito detalle : r.obtenerDatosDocumentacion()) {
                Documentacion doc = detalle.obtenerDatosDocumentacion();
                if (documentacionSeleccionada.contains(doc)) {
                    doc.marcarRecibidayRechazada(estadoRechazada, empleadoLogueado);
                } else {
                    doc.marcarRecibidayAceptada(estadoRecibidayAceptada, empleadoLogueado);
                }
            }
        }
    }

    // --- Opcion 4 (A5): la documentacion se debe redirigir a otra area.
    // "Ocurre cuando la CM destino es la CMC (...) la documentacion se
    // actualiza al estado Para Redirigir." (No se valida aqui que la CM
    // destino sea efectivamente la CMC; queda a criterio del EB.) ---
    private void actualizarDatosRecepcionBolsinConRedireccion() {
        estadoRecibidoEnCMDestino = buscarEstadoRecibidoEnCMDestinoBolsin();
        estadoRecibidoyAceptado = buscarEstadoRecibidoyAceptadoRemito();
        estadoRecibidayAceptada = buscarEstadoRecibidayAceptadaDocumentacion();
        Estado estadoParaRedirigir = buscarEstadoParaRedirigirDocumentacion();

        bolsinSeleccionado.marcarRecibidoEnCMDestino(estadoRecibidoEnCMDestino, empleadoLogueado);

        for (Remito r : remitos) {
            r.setEstado(estadoRecibidoyAceptado);
            for (DetalleRemito detalle : r.obtenerDatosDocumentacion()) {
                Documentacion doc = detalle.obtenerDatosDocumentacion();
                if (documentacionSeleccionada.contains(doc)) {
                    doc.redirigir(estadoParaRedirigir, empleadoLogueado);
                } else {
                    doc.marcarRecibidayAceptada(estadoRecibidayAceptada, empleadoLogueado);
                }
            }
        }
    }

    private Estado buscarEstadoRecibidoEnCMDestinoBolsin() {
        for (Estado e : Estado.getEstados()) {
            if (e.esAmbitoBolsin() && e.esRecibidoEnCMDestino()) {
                return e;
            }
        }
        throw new IllegalStateException("No existe el Estado 'Recibido en CM Destino' de ambito Bolsin");
    }

    private Estado buscarEstadoRecibidoyAceptadoRemito() {
        for (Estado e : Estado.getEstados()) {
            if (e.esAmbitoRemito() && e.esRecibidoyAceptado()) {
                return e;
            }
        }
        throw new IllegalStateException("No existe el Estado 'Recibido y Aceptado' de ambito Remito");
    }

    private Estado buscarEstadoRecibidoyAceptadoParcialRemito() {
        for (Estado e : Estado.getEstados()) {
            if (e.esAmbitoRemito() && e.esRecibidoyAceptadoParcial()) {
                return e;
            }
        }
        throw new IllegalStateException("No existe el Estado 'Recibido y Aceptado Parcial' de ambito Remito");
    }

    private Estado buscarEstadoRecibidayAceptadaDocumentacion() {
        for (Estado e : Estado.getEstados()) {
            if (e.esAmbitoDocumentacion() && e.esRecibidayAceptada()) {
                return e;
            }
        }
        throw new IllegalStateException("No existe el Estado 'Recibida y Aceptada' de ambito Documentacion");
    }

    private Estado buscarEstadoRecibidayRechazadaDocumentacion() {
        for (Estado e : Estado.getEstados()) {
            if (e.esAmbitoDocumentacion() && e.esRecibidayRechazada()) {
                return e;
            }
        }
        throw new IllegalStateException("No existe el Estado 'Recibida y Rechazada' de ambito Documentacion");
    }

    private Estado buscarEstadoNoRecibidaDocumentacion() {
        for (Estado e : Estado.getEstados()) {
            if (e.esAmbitoDocumentacion() && e.esNoRecibida()) {
                return e;
            }
        }
        throw new IllegalStateException("No existe el Estado 'No Recibida' de ambito Documentacion");
    }

    private Estado buscarEstadoParaRedirigirDocumentacion() {
        for (Estado e : Estado.getEstados()) {
            if (e.esAmbitoDocumentacion() && e.esParaRedirigir()) {
                return e;
            }
        }
        throw new IllegalStateException("No existe el Estado 'Para Redirigir' de ambito Documentacion");
    }

    private void llamarCU29() {
        notificarCMOrigen();
    }

    private void notificarCMOrigen() {
        PantallaNotificacionRecepcionBolsin notificador = new PantallaNotificacionRecepcionBolsin();
        notificador.notificar(bolsinSeleccionado);
    }
}
