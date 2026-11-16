import modelo.*;
import persistencia.*;
import java.time.LocalDateTime;

public class DatosEjemploTest {
    static int checks;
    static void check(boolean ok, String mensaje) { checks++; if (!ok) throw new AssertionError(mensaje); }
    public static void main(String[] args) throws Exception {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Argentina/Cordoba"));
        System.setProperty("bolsin.demo.url", "jdbc:h2:mem:ejemplos;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        InicializadorDemo.iniciar();
        check(DatosEjemploCU28.cargar() == 14, "Agrega catorce ejemplos");
        check(DatosEjemploCU28.cargar() == 0, "Segunda carga no duplica");
        var sesion = CargadorDeDatos.cargar();
        check(Bolsin.getBolsines().size() == 16, "Conserva los dos bolsines originales");
        var enviados = Bolsin.buscarBolsinesEnEstadoEnviadoParaCMEmpleado("CM Villa Maria");
        check(enviados.size() == 14, "Dos originales más doce ejemplos disponibles");
        check(enviados.stream().noneMatch(b -> b.getNumeroBolsin() == 612 || b.getNumeroBolsin() == 613), "Excluye recibido y otra CM");
        var recibido = Bolsin.getBolsines().stream().filter(b -> b.getNumeroBolsin() == 612).findFirst().orElseThrow();
        check(recibido.buscarRemitos().get(0).getEstado().esRecibidoyAceptado(), "Remito recibido coherente");
        check(recibido.buscarRemitos().get(0).obtenerDatosDocumentacion().stream().allMatch(d -> d.obtenerDatosDocumentacion().getEstado() instanceof RecibidayAceptada), "Documentación recibida coherente");
        var grande = enviados.stream().filter(b -> b.getNumeroBolsin() == 611).findFirst().orElseThrow();
        check(grande.buscarRemitos().size() == 3, "Tres remitos");
        check(grande.buscarRemitos().stream().mapToInt(r -> r.obtenerDatosDocumentacion().size()).sum() == 7, "Siete documentos");
        Estado eb = Estado.getEstados().stream().filter(e -> e.esAmbitoBolsin() && e.esRecibidoEnCMDestino()).findFirst().orElseThrow();
        Estado er = Estado.getEstados().stream().filter(e -> e.esAmbitoRemito() && e.esRecibidoyAceptado()).findFirst().orElseThrow();
        grande.marcarRecibidoEnCMDestino(LocalDateTime.now(), sesion.obtenerEmpleadoEnSesion(), eb, er);
        new RepositorioRecepcion().guardar(grande, grande.buscarRemitos());
        check(DatosEjemploCU28.cargar() == 0, "Reiniciar no recrea el recibido");
        CargadorDeDatos.cargar();
        check(Bolsin.buscarBolsinesEnEstadoEnviadoParaCMEmpleado("CM Villa Maria").size() == 13, "Se preserva la recepción ya realizada");
        System.out.println("OK: " + checks + " verificaciones de datos de ejemplo e idempotencia.");
    }
}
