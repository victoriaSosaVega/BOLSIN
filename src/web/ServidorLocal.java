package web;

import boundary.PantallaRecepcionDeBolsin;
import control.GestorRecepcionDeBolsin;
import persistencia.CargadorDeDatos;
import com.sun.net.httpserver.*;
import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;

/** Servidor local, sin framework ni servicios externos. Un flujo por cookie del navegador. */
public final class ServidorLocal implements AutoCloseable {
    private final HttpServer servidor;
    private final Map<String, Flujo> flujos = new HashMap<>();
    private static final long DURACION = 2 * 60 * 60 * 1000L;
    private static final class Flujo {
        final PantallaRecepcionDeBolsin pantalla = new PantallaRecepcionDeBolsin();
        final GestorRecepcionDeBolsin gestor;
        long ultimoUso = System.currentTimeMillis();
        Flujo() {
            gestor = new GestorRecepcionDeBolsin(CargadorDeDatos.cargar(), pantalla);
            gestor.iniciarCU28();
            pantalla.paso = 1;
        }
    }
    public ServidorLocal(int puerto) throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", puerto), 0);
        servidor.createContext("/", this::atender);
    }
    public void iniciar() { servidor.start(); }
    public int getPuerto() { return servidor.getAddress().getPort(); }
    public void close() { servidor.stop(0); }

    private synchronized void atender(HttpExchange ex) throws IOException {
        try {
            String host = ex.getRequestHeaders().getFirst("Host");
            if (!("127.0.0.1:" + getPuerto()).equals(host) && !("localhost:" + getPuerto()).equals(host)) {
                responder(ex, 403, Map.of("error", "Host no permitido")); return;
            }
            String ruta = ex.getRequestURI().getPath();
            if (!ruta.startsWith("/api/")) { archivo(ex, ruta); return; }
            boolean consulta = ruta.equals("/api/estado") && ex.getRequestMethod().equals("GET");
            boolean accion = ruta.equals("/api/accion") && ex.getRequestMethod().equals("POST");
            if (!consulta && !accion) { responder(ex, 405, Map.of("error", "Método o ruta no permitidos")); return; }
            if (accion && (!"1".equals(ex.getRequestHeaders().getFirst("X-CU28"))
                    || !Objects.toString(ex.getRequestHeaders().getFirst("Content-Type"), "").startsWith("application/x-www-form-urlencoded"))) {
                responder(ex, 403, Map.of("error", "Solicitud no permitida")); return;
            }
            String origen = ex.getRequestHeaders().getFirst("Origin");
            if (accion && origen != null && !origen.equals("http://" + host)) {
                responder(ex, 403, Map.of("error", "Origen no permitido")); return;
            }
            flujos.entrySet().removeIf(e -> System.currentTimeMillis() - e.getValue().ultimoUso > DURACION);
            String id = cookie(ex);
            Flujo flujo = flujos.get(id);
            if (flujo == null) {
                if (accion) { responder(ex, 409, Map.of("error", "La sesión venció. Recargá la página para continuar.")); return; }
                if (flujos.size() >= 128) { responder(ex, 503, Map.of("error", "Hay demasiadas sesiones abiertas.")); return; }
                flujo = new Flujo(); id = UUID.randomUUID().toString(); flujos.put(id, flujo);
                ex.getResponseHeaders().add("Set-Cookie", "CU28=" + id + "; Path=/; HttpOnly; SameSite=Strict");
            }
            flujo.ultimoUso = System.currentTimeMillis();
            if (accion) {
                byte[] body = ex.getRequestBody().readNBytes(8193);
                if (body.length > 8192) { responder(ex, 413, Map.of("error", "Solicitud demasiado grande")); return; }
                Map<String, String> campos = formulario(new String(body, StandardCharsets.UTF_8));
                if ("iniciar".equals(campos.get("accion"))) {
                    flujo = new Flujo(); flujos.put(id, flujo);
                } else ejecutar(flujo, campos);
            }
            responder(ex, flujo.pantalla.error.isEmpty() ? 200 : 409, flujo.pantalla.datos());
        } catch (Exception e) {
            // El flujo se valida antes de enviar cabeceras; diagnósticos completos quedan en consola.
            System.err.println("Solicitud CU28: " + e.getMessage());
            responder(ex, 500, Map.of("error", "No se pudo completar la operación. Revisá la consola de la aplicación y reintentá."));
        } finally {
            ex.close();
        }
    }

    private void ejecutar(Flujo f, Map<String, String> campos) {
        var p = f.pantalla;
        p.limpiarMensajes();
        String accion = campos.getOrDefault("accion", "");
        if (p.finalizado) return; // Doble confirmación no repite el guardado.
        try {
            switch (accion) {
                case "listar" -> { if (p.paso != 1 && p.paso != 2 && p.paso != 3) throw new IllegalArgumentException("Volvé a la selección de bolsín."); f.gestor.filtrarBolsines("", ""); p.seleccionado = null; }
                case "buscar" -> { if (p.paso != 2) throw new IllegalArgumentException("Primero abrí la selección de bolsín."); f.gestor.filtrarBolsines(campos.getOrDefault("precinto", ""), campos.getOrDefault("origen", "")); p.seleccionado = null; }
                case "seleccionar" -> {
                    if (p.paso != 2) throw new IllegalArgumentException("Primero abrí la selección de bolsín.");
                    int id = Integer.parseInt(campos.getOrDefault("id", "0"));
                    var b = p.getBolsinesMostrados().stream().filter(x -> x.getId() == id).findFirst()
                            .orElseThrow(() -> new IllegalArgumentException("Seleccioná un bolsín del listado."));
                    f.gestor.tomarBolsinSeleccionado(b); p.seleccionado = b;
                }
                case "revisar" -> { if (p.paso != 3) throw new IllegalArgumentException("Primero revisá la documentación."); f.gestor.tomarSeleccionRecepcionBolsin(); }
                case "confirmar" -> { if (p.paso != 4) throw new IllegalArgumentException("Primero revisá y confirmá la selección."); f.gestor.tomarConfirmacion(true); }
                case "noConfirmar" -> { if (p.paso != 4) throw new IllegalArgumentException("No hay confirmación pendiente."); f.gestor.tomarConfirmacion(false); p.paso = 3; }
                case "cancelar" -> f.gestor.cancelarCU();
                default -> throw new IllegalArgumentException("Acción desconocida.");
            }
        } catch (IllegalArgumentException e) { p.error = e.getMessage(); }
    }

    private static String cookie(HttpExchange ex) {
        for (String c : Objects.toString(ex.getRequestHeaders().getFirst("Cookie"), "").split(";")) {
            if (c.strip().startsWith("CU28=")) return c.strip().substring(5);
        }
        return "";
    }
    private static Map<String, String> formulario(String body) {
        Map<String, String> campos = new HashMap<>();
        for (String par : body.split("&")) {
            String[] partes = par.split("=", 2);
            campos.put(URLDecoder.decode(partes[0], StandardCharsets.UTF_8), partes.length == 2 ? URLDecoder.decode(partes[1], StandardCharsets.UTF_8) : "");
        }
        return campos;
    }
    private void archivo(HttpExchange ex, String ruta) throws IOException {
        if (!ex.getRequestMethod().equals("GET")) { responder(ex, 405, Map.of("error", "Método no permitido")); return; }
        String archivo = switch (ruta) { case "/" -> "index.html"; case "/styles.css" -> "styles.css"; case "/app.js" -> "app.js"; default -> null; };
        if (archivo == null) { responder(ex, 404, Map.of("error", "No encontrado")); return; }
        String tipo = archivo.endsWith("html") ? "text/html" : archivo.endsWith("css") ? "text/css" : "text/javascript";
        enviar(ex, 200, tipo, Files.readAllBytes(Path.of("web", archivo)));
    }
    private static void responder(HttpExchange ex, int estado, Object datos) throws IOException {
        enviar(ex, estado, "application/json", Json.escribir(datos).getBytes(StandardCharsets.UTF_8));
    }
    private static void enviar(HttpExchange ex, int estado, String tipo, byte[] datos) throws IOException {
        var h = ex.getResponseHeaders();
        h.set("Content-Type", tipo + "; charset=utf-8"); h.set("Cache-Control", "no-store");
        h.set("X-Content-Type-Options", "nosniff");
        h.set("Content-Security-Policy", "default-src 'self'; style-src 'self' 'unsafe-inline'; script-src 'self'; frame-ancestors 'none'; base-uri 'self'");
        ex.sendResponseHeaders(estado, datos.length); ex.getResponseBody().write(datos);
    }
}
