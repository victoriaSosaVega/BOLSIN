package app;

import boundary.PantallaPrincipalEncargadoBolsines;
import modelo.Bolsin;
import modelo.CM;
import modelo.CambioEstadoBolsin;
import modelo.DetalleRemito;
import modelo.Documentacion;
import modelo.Empleado;
import modelo.Estado;
import modelo.Remito;
import modelo.Sesion;
import modelo.TipoDocumentacion;
import modelo.Usuario;

import javax.swing.SwingUtilities;
import java.time.LocalDate;

public class MainCU28 {

    public static void main(String[] args) {
        // --- Seed de Estados (persistencia simulada) ---
        new Estado("Enviado", "Bolsin enviado por CM origen", Estado.AMBITO_BOLSIN);
        new Estado("Recibido en CM Destino", "Bolsin recibido en CM destino", Estado.AMBITO_BOLSIN);
        new Estado("Recibido y Aceptado", "Remito recibido y aceptado", Estado.AMBITO_REMITO);
        new Estado("Recibido y Aceptado Parcial", "Remito con documentacion faltante", Estado.AMBITO_REMITO);
        new Estado("Recibida y Aceptada", "Documentacion recibida y aceptada", Estado.AMBITO_DOCUMENTACION);
        new Estado("Recibida y Rechazada", "Documentacion que no corresponde a la CM destino", Estado.AMBITO_DOCUMENTACION);
        new Estado("No Recibida", "Documentacion que no llego en el bolsin", Estado.AMBITO_DOCUMENTACION);
        new Estado("Para Redirigir", "Documentacion a redirigir a otra area de la CMC", Estado.AMBITO_DOCUMENTACION);

        // --- CMs ---
        CM cmCordoba = new CM("CBA", "CM Cordoba", "Bv. San Juan 1000", "cba@organismo.gob.ar", "351-0000000");
        CM cmVillaMaria = new CM("VM", "CM Villa Maria", "Av. Libertad 500", "vm@organismo.gob.ar", "353-1111111");

        // --- Empleado logueado y su sesion ---
        Empleado empleado = new Empleado("Ana", "Perez", "ana.perez@organismo.gob.ar", cmVillaMaria);
        Usuario usuario = new Usuario("aperez", "****", empleado);
        Sesion sesion = new Sesion(usuario);

        // --- Documentacion y tipo ---
        TipoDocumentacion expediente = new TipoDocumentacion("Expediente", "Expediente administrativo");
        Documentacion doc1 = new Documentacion("D-001", "Solicitud de estudio medico", LocalDate.now().minusDays(5), expediente);
        Documentacion doc2 = new Documentacion("D-002", "Dictamen preliminar", LocalDate.now().minusDays(5), expediente);

        // --- Remito con su detalle ---
        Remito remito1 = new Remito(1001, LocalDate.now().minusDays(2));
        remito1.agregarDetalle(new DetalleRemito("Recepcion", doc1));
        remito1.agregarDetalle(new DetalleRemito("Recepcion", doc2));

        // --- Bolsines en estado Enviado, con CM destino Villa Maria (para tener
        // mas de una opcion real para elegir en la pantalla) ---
        Estado estadoEnviado = buscarEstado("Enviado", Estado.AMBITO_BOLSIN);

        Bolsin bolsin1 = new Bolsin(500, "PRE-2026-01", LocalDate.now().minusDays(1), 350.0, cmCordoba, cmVillaMaria);
        bolsin1.agregarRemito(remito1);
        bolsin1.agregarCambioEstado(new CambioEstadoBolsin(estadoEnviado, empleado));

        Remito remito2 = new Remito(1002, LocalDate.now().minusDays(1));
        Documentacion doc3 = new Documentacion("D-003", "Historia clinica", LocalDate.now().minusDays(3), expediente);
        remito2.agregarDetalle(new DetalleRemito("Recepcion", doc3));

        Bolsin bolsin2 = new Bolsin(501, "PRE-2026-02", LocalDate.now(), 120.0, cmCordoba, cmVillaMaria);
        bolsin2.agregarRemito(remito2);
        bolsin2.agregarCambioEstado(new CambioEstadoBolsin(estadoEnviado, empleado));

        // --- Se abre el menu del Encargado de Bolsines; CU28 es la opcion 1 ---
        SwingUtilities.invokeLater(() -> new PantallaPrincipalEncargadoBolsines(sesion));
    }

    private static Estado buscarEstado(String nombre, String ambito) {
        for (Estado e : Estado.getEstados()) {
            boolean mismoAmbito = ambito.equals(Estado.AMBITO_BOLSIN) && e.esAmbitoBolsin();
            if (e.getNombre().equals(nombre) && mismoAmbito) {
                return e;
            }
        }
        throw new IllegalStateException("Estado no encontrado: " + nombre);
    }
}
