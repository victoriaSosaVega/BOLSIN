package boundary;

import modelo.Bolsin;
import modelo.CM;
import modelo.Documentacion;
import java.util.LinkedHashMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/** Adaptador de CU29: simulación explícita, sin enviar correos reales. */
public class PantallaNotificacionRecepcionBolsin {
    public void notificar(Bolsin bolsin) {
        Map<CM, List<Documentacion>> porOrigen = new LinkedHashMap<>();
        for (var remito : bolsin.buscarRemitos()) {
            for (var detalle : remito.obtenerDatosDocumentacion()) {
                var doc = detalle.obtenerDatosDocumentacion();
                if (doc.getCMOrigen() == null) throw new IllegalStateException("Falta la CM origen de la documentación.");
                porOrigen.computeIfAbsent(doc.getCMOrigen(), cm -> new ArrayList<>()).add(doc);
            }
        }
        porOrigen.forEach((cm, docs) -> {
            System.out.println("[CU29 SIMULADO] Para: " + cm.getNombre() + " <" + cm.getEmail() + ">");
            System.out.println("Bolsín #" + bolsin.getNumeroBolsin() + " recibido en CM destino.");
            for (var doc : docs) System.out.println("  " + doc.getAsunto() + ": " + doc.getEstado().getNombre());
        });
    }
}
