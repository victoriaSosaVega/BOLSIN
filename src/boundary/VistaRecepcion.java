package boundary;

import modelo.*;
import java.util.List;

/** Contrato de la pantalla; permite verificar el gestor sin abrir ventanas. */
public interface VistaRecepcion {
    void mostrarCMEmpleadoLogueado(Empleado empleado);
    void mostrarCMSeleccion(List<Bolsin> bolsines);
    void mostrarOpcionesRecepcionBolsin(List<Remito> remitos);
    void solicitarConfirmacion();
    void mostrarErrorGuardado(String detalle);
    void mostrarAviso(String mensaje);
    void finCU();
    void cancelarCU();
}
