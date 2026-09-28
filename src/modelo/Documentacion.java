package modelo;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class Documentacion {

    private String numero;
    private String asunto;
    private LocalDate fechaPase;
    private TipoDocumentacion tipoDocumentacion;
    private List<CambioEstadoDocumentacion> cambiosEstado = new ArrayList<>();

    public Documentacion(String numero, String asunto, LocalDate fechaPase, TipoDocumentacion tipoDocumentacion) {
        this.numero = numero;
        this.asunto = asunto;
        this.fechaPase = fechaPase;
        this.tipoDocumentacion = tipoDocumentacion;
    }

    public String getAsunto() {
        return asunto;
    }

    public TipoDocumentacion getTipo() {
        return tipoDocumentacion;
    }

    public CambioEstadoDocumentacion getCambioEstadoActual() {
        for (CambioEstadoDocumentacion c : cambiosEstado) {
            if (c.esActual()) {
                return c;
            }
        }
        return null;
    }

    public void agregarCambioEstado(CambioEstadoDocumentacion cambio) {
        cambiosEstado.add(cambio);
    }

    private CambioEstadoDocumentacion crearCambioEstado(Estado estado, Empleado responsable) {
        return new CambioEstadoDocumentacion(estado, responsable);
    }

    private void cambiarEstado(Estado nuevoEstado, Empleado responsable) {
        CambioEstadoDocumentacion actual = getCambioEstadoActual();
        if (actual != null) {
            actual.setFechaFin();
        }
        agregarCambioEstado(crearCambioEstado(nuevoEstado, responsable));
    }

    // --- CU28, opción 1: contenido del bolsín igual al registrado ---
    public void marcarRecibidayAceptada(Estado nuevoEstado, Empleado responsable) {
        cambiarEstado(nuevoEstado, responsable);
    }

    // --- CU28, alt. A3: no se recibió toda la documentación del bolsín ---
    public void marcarNoRecibida(Estado nuevoEstado, Empleado responsable) {
        cambiarEstado(nuevoEstado, responsable);
    }

    // --- CU28, alt. A4: documentación que no corresponde a la CM destino ---
    public void marcarRecibidayRechazada(Estado nuevoEstado, Empleado responsable) {
        cambiarEstado(nuevoEstado, responsable);
    }

    // --- CU28, alt. A5: documentación a redirigir a otra área (CMC) ---
    public void redirigir(Estado nuevoEstado, Empleado responsable) {
        cambiarEstado(nuevoEstado, responsable);
    }

    // --- CU31 Registrar Revisión de Documentación: vuelve a estado Registrada
    // para poder vincularse a un nuevo remito ---
    public void reenviar(Estado estadoRegistrada, Empleado responsable) {
        cambiarEstado(estadoRegistrada, responsable);
    }

    // --- CU31: se considera que ya no es relevante su envío ---
    public void darDeBaja(Estado estadoDeBaja, Empleado responsable) {
        cambiarEstado(estadoDeBaja, responsable);
    }

    // --- CU15/CU16 Generar/Modificar Remito: pertenecen a otro caso de uso,
    // no ejercitados por CU28; quedan como puntos de extensión. ---
    public void incluirEnRemito() {
        // TODO: usado por CU15 Generar Remito / CU16 Modificar Remito
    }

    public void quitarDeRemito() {
        // TODO: usado por CU16 Modificar Remito / CU17 Cancelar Remito
    }

    // --- CU19/CU20 Generar/Modificar Bolsín: idem, otro caso de uso. ---
    public void incluirEnBolsin() {
        // TODO: usado por CU19 Generar Bolsín / CU20 Modificar Bolsín
    }

    public void quitarDeBolsin() {
        // TODO: usado por CU20 Modificar Bolsín / CU21 Eliminar Bolsín
    }

    // --- CU27 Registrar el retiro de bolsines: pasa a "en bolsín enviado" ---
    public void retirar(Estado estadoEnBolsinEnviado, Empleado responsable) {
        cambiarEstado(estadoEnBolsinEnviado, responsable);
    }
}
