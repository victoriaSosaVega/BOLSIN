import modelo.*;
import persistencia.*;
import boundary.VistaRecepcion;
import control.GestorRecepcionDeBolsin;
import java.time.*;
import java.util.*;

/** Pruebas ejecutables sin framework. Cada escenario usa una base H2 aislada. */
public class CU28Test {
    static int checks;
    static int db;
    static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }
    static void falla(Runnable accion) {
        boolean fallo = false;
        try { accion.run(); } catch (IllegalStateException e) { fallo = true; }
        check(fallo, "Debía rechazar la operación");
    }
    static Sesion cargarNueva() {
        System.setProperty("bolsin.demo.url", "jdbc:h2:mem:cu28_" + (++db) + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        InicializadorDemo.iniciar();
        return CargadorDeDatos.cargar();
    }
    static Bolsin primero() { return Bolsin.getBolsines().get(0); }
    static Documentacion doc(Bolsin b) { return b.buscarRemitos().get(0).obtenerDatosDocumentacion().get(0).obtenerDatosDocumentacion(); }
    static int contar(String tabla) throws Exception {
        try (var c = Conexion.abrir(); var s = c.createStatement(); var r = s.executeQuery("select count(*) from " + tabla)) {
            r.next(); return r.getInt(1);
        }
    }
    static Estado estado(String ambito, String nombre) {
        return Estado.getEstados().stream().filter(e -> e.getAmbito().equals(ambito) && e.getNombre().equals(nombre)).findFirst().orElseThrow();
    }
    static void recibir(Bolsin b, Empleado e) {
        b.marcarRecibidoEnCMDestino(LocalDateTime.now(), e,
                estado("Bolsin", "Recibido en CM Destino"), estado("Remito", "Recibido y Aceptado"));
    }
    static class Vista implements VistaRecepcion {
        List<Bolsin> lista = List.of(); String aviso = ""; String error = ""; int fin; int cancelado;
        public void mostrarCMEmpleadoLogueado(Empleado e) { }
        public void mostrarCMSeleccion(List<Bolsin> b) { lista = b; }
        public void mostrarOpcionesRecepcionBolsin(List<Remito> r) { }
        public void solicitarConfirmacion() { }
        public void mostrarErrorGuardado(String s) { error = s; }
        public void mostrarAviso(String s) { aviso = s; }
        public void finCU() { fin++; }
        public void cancelarCU() { cancelado++; }
    }
    public static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Cordoba"));
        flujoCompleto(); alternativas(); rollbackSQL(); precondiciones(); concurrencia(); transiciones(); ordenSecuencia(); reinicio();
        System.out.println("OK: " + checks + " verificaciones de CU28, State y persistencia JDBC.");
    }
    static void reinicio() throws Exception {
        var dir = java.nio.file.Files.createTempDirectory("bolsin-test-");
        String url = "jdbc:h2:" + dir.resolve("cu28") + ";MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE";
        for (String fase : List.of("guardar", "leer")) {
            var proceso = new ProcessBuilder(System.getProperty("java.home") + "/bin/java", "-cp",
                    System.getProperty("java.class.path"), "CU28RestartTest", fase, url).inheritIO().start();
            check(proceso.waitFor() == 0, "Persistencia entre JVM: " + fase);
        }
    }

    static void flujoCompleto() throws Exception {
        Sesion sesion = cargarNueva(); Vista v = new Vista(); int[] avisos = {0};
        GestorRecepcionDeBolsin g = new GestorRecepcionDeBolsin(sesion, v, new RepositorioRecepcion(), b -> avisos[0]++);
        g.iniciarCU28(); check(v.lista.size() == 2, "Lista inicial");
        Bolsin b = primero(); Documentacion d = doc(b);
        g.tomarBolsinSeleccionado(b); g.tomarConfirmacion(true);
        check(v.error.isEmpty(), "Guardado: " + v.error);
        check(v.fin == 1 && avisos[0] == 1, "Fin y notificación después del commit");
        check(b.getCambioEstadoActual().getEstado().esRecibidoEnCMDestino(), "Estado bolsín");
        check(d.getEstado() instanceof RecibidayAceptada, "State aceptado");
        check(b.buscarRemitos().get(0).getEstado().esRecibidoyAceptado(), "Estado remito");
        check(d.getCambioEstadoActual().getFechaHoraInicio().equals(b.getCambioEstadoActual().getFechaHoraInicio()), "Fecha única");
        check(d.getCambioEstadoActual().getResponsable() == sesion.obtenerEmpleadoEnSesion(), "Responsable documentación");
        check(b.getCambioEstadoActual().getResponsable() == sesion.obtenerEmpleadoEnSesion(), "Responsable bolsín");
        check(contar("cambio_estado_documentacion") == 5, "Dos historiales nuevos");
        g.tomarConfirmacion(true); check(avisos[0] == 1, "Confirmación repetida no duplica");
        CargadorDeDatos.cargar(); check(Bolsin.getBolsines().size() == 2, "Recarga sin duplicar catálogo");
        check(doc(primero()).getEstado() instanceof RecibidayAceptada, "State reconstruido desde BD");
        check(Bolsin.buscarBolsinesEnEstadoEnviadoParaCMEmpleado("CM Villa Maria").size() == 1, "Recibido no reaparece");
    }
    static void alternativas() throws Exception {
        var sesion = cargarNueva(); var v = new Vista(); int[] llamadas = {0};
        var g = new GestorRecepcionDeBolsin(sesion, v, new RepositorioRecepcion(), b -> llamadas[0]++);
        g.iniciarCU28(); g.filtrarBolsines("NO-EXISTE", "");
        check(v.lista.isEmpty() && v.aviso.contains("A1"), "A1 precinto inexistente");
        g.filtrarBolsines("", "NO-EXISTE"); check(v.lista.isEmpty() && v.aviso.contains("A2"), "A2 origen inexistente");
        g.filtrarBolsines("PRE-2026-01", "cordoba"); check(v.lista.size() == 1, "Filtros combinados");
        g.tomarBolsinSeleccionado(v.lista.get(0)); g.tomarConfirmacion(false);
        check(primero().esEstadoEnviado() && contar("cambio_estado_bolsin") == 2 && llamadas[0] == 0, "A6 sin efectos");
        g.cancelarCU(); g.tomarConfirmacion(true);
        check(v.cancelado == 1 && contar("cambio_estado_bolsin") == 2, "Cancelar impide confirmar");
    }
    static void rollbackSQL() throws Exception {
        var sesion = cargarNueva(); var v = new Vista(); int[] llamadas = {0};
        try (var c = Conexion.abrir(); var st = c.createStatement()) {
            st.executeUpdate("delete from estado where nombre = 'Recibida y Aceptada' and ambito = 'Documentacion'");
        }
        var g = new GestorRecepcionDeBolsin(sesion, v, new RepositorioRecepcion(), b -> llamadas[0]++);
        g.iniciarCU28(); var b = primero(); g.tomarBolsinSeleccionado(b); g.tomarConfirmacion(true);
        check(!v.error.isEmpty() && v.fin == 0 && llamadas[0] == 0, "Error no notifica ni finaliza con éxito");
        check(contar("cambio_estado_bolsin") == 2 && contar("cambio_estado_documentacion") == 3, "Rollback SQL completo");
        check(b.esEstadoEnviado() && doc(b).getEstado() instanceof EnBolsinEnviado, "Memoria restaurada");
        check(b.getCambiosEstado().size() == 1 && doc(b).getCambiosEstado().size() == 1, "Sin historiales huérfanos");
        try (var c = Conexion.abrir(); var st = c.createStatement()) {
            st.executeUpdate("insert into estado(nombre,descripcion,ambito) values('Recibida y Aceptada','Aceptada','Documentacion')");
        }
        g.tomarConfirmacion(true); check(v.fin == 1 && llamadas[0] == 1, "Reintento luego de rollback");
    }
    static void precondiciones() {
        var sesion = cargarNueva(); var b = primero(); var e = sesion.obtenerEmpleadoEnSesion();
        var segundo = b.buscarRemitos().get(0).obtenerDatosDocumentacion().get(1).obtenerDatosDocumentacion();
        segundo.setEstado(new RecibidayAceptada());
        falla(() -> recibir(b, e)); check(b.esEstadoEnviado() && doc(b).getEstado() instanceof EnBolsinEnviado, "Valida todo antes de mutar");
        var d = doc(b); d.agregarCambioEstado(new CambioEstadoDocumentacion(LocalDateTime.now(), d.getEstado(), e));
        falla(() -> d.marcarRecibidayAceptada(LocalDateTime.now(), e));
        check(d.getEstado() instanceof EnBolsinEnviado, "Historial duplicado rechazado");
    }
    static void concurrencia() throws Exception {
        var sesion = cargarNueva(); var viejo = primero();
        CargadorDeDatos.cargar(); var nuevo = primero();
        recibir(nuevo, nuevo.getCambioEstadoActual().getResponsable());
        new RepositorioRecepcion().guardar(nuevo, nuevo.buscarRemitos());
        recibir(viejo, sesion.obtenerEmpleadoEnSesion());
        boolean fallo = false;
        try { new RepositorioRecepcion().guardar(viejo, viejo.buscarRemitos()); } catch (PersistenciaException ex) { fallo = true; }
        check(fallo && contar("cambio_estado_bolsin") == 3, "Rechaza recepción con datos obsoletos");
    }
    static void transiciones() {
        var sesion = cargarNueva(); var e = sesion.obtenerEmpleadoEnSesion();
        Documentacion d = new Documentacion(9,"Prueba",LocalDate.now(),new TipoDocumentacion("Tipo", ""));
        d.setEstado(new Registrada()); d.agregarCambioEstado(new CambioEstadoDocumentacion(LocalDateTime.now().minusDays(1),d.getEstado(),e));
        var t = LocalDateTime.now();
        falla(() -> d.darDeBaja(t,e)); // Operación declarada, sin transición permitida en la ME.
        d.incluirEnRemito(t,e); check(d.getEstado() instanceof EnRemito,"Registrada -> EnRemito");
        d.quitarDeRemito(t,e); check(d.getEstado() instanceof Registrada,"EnRemito -> Registrada");
        d.incluirEnRemito(t,e); d.incluirEnBolsin(t,e); check(d.getEstado() instanceof EnBolsinSaliente,"EnBolsinSaliente");
        d.quitarDeBolsin(t,e); check(d.getEstado() instanceof EnRemito,"Quitar de bolsín");
        d.incluirEnBolsin(t,e); d.retirar(t,e); d.marcarNoRecibida(t,e); check(d.getEstado() instanceof NoRecibida,"NoRecibida");
        d.reenviar(t,e); check(d.getEstado() instanceof Registrada,"Reenviar");
        d.incluirEnRemito(t,e); d.incluirEnBolsin(t,e); d.retirar(t,e); d.marcarRecibidayRechazada(t,e);
        check(d.getEstado() instanceof RecibidaYRechazada,"Rechazar"); d.reenviar(t,e);
        d.incluirEnRemito(t,e); d.incluirEnBolsin(t,e); d.retirar(t,e); d.redirigir(t,e);
        check(d.getEstado() instanceof ParaRedirigir,"Redirigir"); d.marcarRecibidayAceptada(t,e);
        falla(() -> d.reenviar(t,e)); check(d.getCambiosEstado().stream().filter(CambioEstadoDocumentacion::esActual).count() == 1,"Un historial vigente");
        var d2 = doc(primero()); d2.marcarNoRecibida(t,e); d2.darDeBaja(t,e); check(d2.getEstado() instanceof DeBaja,"NoRecibida -> DeBaja");
        falla(() -> d2.incluirEnRemito(t,e));
    }
    static void ordenSecuencia() {
        var sesion = cargarNueva(); var e = sesion.obtenerEmpleadoEnSesion();
        Bolsin b = new Bolsin(999,"PRE",LocalDate.now(),1f,e.getCM(),e.getCM());
        b.agregarCambioEstado(new CambioEstadoBolsin(LocalDateTime.now().minusDays(1),estado("Bolsin","Enviado"),e));
        Remito r = new Remito(999,LocalDate.now()) {
            @Override public void setEstado(Estado estado) {
                if (estado.esRecibidoyAceptado()) {
                    check(obtenerDatosDocumentacion().get(0).obtenerDatosDocumentacion().getEstado() instanceof RecibidayAceptada,"Remito se actualiza después de documentación");
                    check(b.getCambiosEstado().size() == 1 && !b.getCambiosEstado().get(0).esActual(),"Historial bolsín se crea al final");
                }
                super.setEstado(estado);
            }
        };
        r.setEstado(estado("Remito","En Bolsin Enviado")); r.agregarDetalle(new DetalleRemito("",doc(primero()))); b.agregarRemito(r);
        recibir(b,e); check(b.getCambiosEstado().size() == 2,"Historial final creado");
    }
}
