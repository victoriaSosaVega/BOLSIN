import persistencia.*;
import modelo.*;
import java.time.LocalDateTime;

/** Invocado en dos JVM distintas: la segunda debe recuperar la recepción de disco. */
public class CU28RestartTest {
    public static void main(String[] args) {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Argentina/Cordoba"));
        System.setProperty("bolsin.demo.url", args[1]);
        InicializadorDemo.iniciar();
        var sesion = CargadorDeDatos.cargar();
        var bolsin = Bolsin.getBolsines().get(0);
        if (args[0].equals("guardar")) {
            Estado eb = Estado.getEstados().stream().filter(e -> e.esAmbitoBolsin() && e.esRecibidoEnCMDestino()).findFirst().orElseThrow();
            Estado er = Estado.getEstados().stream().filter(e -> e.esAmbitoRemito() && e.esRecibidoyAceptado()).findFirst().orElseThrow();
            bolsin.marcarRecibidoEnCMDestino(LocalDateTime.now(),sesion.obtenerEmpleadoEnSesion(),eb,er);
            new RepositorioRecepcion().guardar(bolsin,bolsin.buscarRemitos());
        } else if (!bolsin.getCambioEstadoActual().getEstado().esRecibidoEnCMDestino()
                || !(bolsin.buscarRemitos().get(0).obtenerDatosDocumentacion().get(0).obtenerDatosDocumentacion().getEstado() instanceof RecibidayAceptada)) {
            throw new AssertionError("La recepción no sobrevivió al reinicio de la JVM");
        }
    }
}
