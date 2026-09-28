package boundary;

import modelo.Bolsin;
import modelo.Documentacion;
import modelo.Remito;

// Nota de la revision del diagrama: conceptualmente esto representa el
// envio de un mail (CU29 tiene como Actor Secundario al Servidor de Correo,
// segun el ERS), no una pantalla vista por el usuario. Se mantiene el nombre
// y la forma tal como esta en los diagramas ya entregados/corregidos, y se
// simula el envio por consola.
public class PantallaNotificacionRecepcionBolsin {

    public void notificar(Bolsin bolsin) {
        System.out.println("[CU29 - Notificacion] Email a CM origen: " + bolsin.getCMOrigen().getNombre());
        System.out.println("  Bolsin #" + bolsin.getNumeroBolsin() + " recibido en CM destino.");
        for (Remito r : bolsin.buscarRemitos()) {
            for (var detalle : r.obtenerDatosDocumentacion()) {
                Documentacion doc = detalle.obtenerDatosDocumentacion();
                System.out.println("  - Documentacion '" + doc.getAsunto() + "': " + doc.getCambioEstadoActual().getEstado().getNombre());
            }
        }
    }
}
