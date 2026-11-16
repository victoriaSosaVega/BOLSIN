import web.ServidorLocal;
import persistencia.*;
import java.net.*;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

/** Contrato HTTP real sobre una base aislada, sin modificar la demo del usuario. */
public class WebTest {
    static int checks;
    static String base;
    static HttpClient cliente() {
        return HttpClient.newBuilder().cookieHandler(new CookieManager(null, CookiePolicy.ACCEPT_ALL)).build();
    }
    static void check(boolean condition, String mensaje) {
        checks++;
        if (!condition) throw new AssertionError(mensaje);
    }
    static HttpResponse<String> get(HttpClient c, String path) throws Exception {
        return c.send(HttpRequest.newBuilder(URI.create(base + path)).GET().build(), HttpResponse.BodyHandlers.ofString());
    }
    static HttpResponse<String> post(HttpClient c, String action) throws Exception {
        return c.send(HttpRequest.newBuilder(URI.create(base + "/api/accion"))
                .header("Content-Type", "application/x-www-form-urlencoded").header("X-CU28", "1")
                .POST(HttpRequest.BodyPublishers.ofString("accion=" + action, StandardCharsets.UTF_8)).build(), HttpResponse.BodyHandlers.ofString());
    }
    static int count() throws Exception {
        try (var c = Conexion.abrir(); var s = c.createStatement(); var r = s.executeQuery("select count(*) from cambio_estado_bolsin")) { r.next(); return r.getInt(1); }
    }
    public static void main(String[] args) throws Exception {
        TimeZone.setDefault(TimeZone.getTimeZone("America/Argentina/Cordoba"));
        System.setProperty("bolsin.demo.url", "jdbc:h2:mem:webtest;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE;DB_CLOSE_DELAY=-1");
        InicializadorDemo.iniciar();
        try (var server = new ServidorLocal(0)) {
            server.iniciar(); base = "http://127.0.0.1:" + server.getPuerto();
            var c = cliente();
            var html = get(c, "/");
            check(html.statusCode() == 200 && html.body().contains("FLUJO DEL CASO"), "HTML local con diseño anterior");
            check(get(c,"/styles.css").body().contains("#0035ad"), "Paleta azul conservada");
            check(get(c,"/app.js").statusCode() == 200, "Javascript servido");
            check(get(c,"/.env").statusCode() == 404, "Archivos privados no servidos");
            check(get(c,"/api/estado").body().contains("\"paso\":1"), "Inicio en identificación");
            check(post(c,"confirmar").statusCode() == 409 && count() == 2, "No permite saltar confirmación");
            check(post(c,"listar").body().contains("PRE-2026-01"), "Listado real");
            check(post(c,"buscar&precinto=NO-EXISTE").body().contains("A1"), "A1 desde HTTP");
            check(post(c,"buscar&origen=NO-EXISTE").body().contains("A2"), "A2 desde HTTP");
            post(c,"listar");
            check(post(c,"seleccionar&id=99999").statusCode() == 409, "Rechaza ID ajeno al listado");
            check(post(c,"seleccionar&id=1").body().contains("Solicitud de estudio medico"), "Detalles desde la base");
            check(get(c,"/api/estado").body().contains("\"paso\":3"), "Recarga mantiene paso");
            post(c,"revisar");
            check(post(c,"noConfirmar").body().contains("\"paso\":3") && count()==2, "A6 sin escritura");
            post(c,"revisar");
            var otro = cliente(); get(otro,"/api/estado"); post(otro,"listar"); post(otro,"seleccionar&id=1"); post(otro,"revisar");
            check(post(c,"confirmar").body().contains("\"finalizado\":true") && count()==3, "Confirmación HTTP persiste");
            check(post(c,"confirmar").statusCode()==200 && count()==3, "Doble envío idempotente");
            check(post(otro,"confirmar").statusCode()==409 && count()==3, "Otro navegador obsoleto no duplica");
            check(get(c,"/api/estado").body().contains("\"paso\":5"), "Recargar resultado no vuelve a guardar");
            post(c,"iniciar");
            check(!post(c,"listar").body().contains("PRE-2026-01"), "Nueva recepción excluye bolsín recibido");
            check(post(c,"cancelar").body().contains("\"cancelado\":true"), "Cancelación web");
            post(c,"confirmar"); check(count()==3, "Cancelación no cambia la base");
            var sinCookie = cliente();
            check(post(sinCookie,"confirmar").statusCode()==409, "Rechaza sesión inexistente");
            var forbidden = c.send(HttpRequest.newBuilder(URI.create(base + "/api/accion"))
                    .POST(HttpRequest.BodyPublishers.ofString("accion=iniciar")).build(),HttpResponse.BodyHandlers.ofString());
            check(forbidden.statusCode()==403,"Rechaza formulario de otro sitio");
            var origin = c.send(HttpRequest.newBuilder(URI.create(base + "/api/accion"))
                    .header("X-CU28","1").header("Content-Type","application/x-www-form-urlencoded").header("Origin","http://otro.local")
                    .POST(HttpRequest.BodyPublishers.ofString("accion=iniciar")).build(),HttpResponse.BodyHandlers.ofString());
            check(origin.statusCode()==403,"Rechaza origen externo");
        }
        System.out.println("OK: " + checks + " verificaciones HTTP de la aplicación web local.");
    }
}
