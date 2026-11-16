package app;

import persistencia.*;
import web.ServidorLocal;

public class MainCU28 {
    public static void main(String[] args) throws Exception {
        java.util.TimeZone.setDefault(java.util.TimeZone.getTimeZone("America/Argentina/Cordoba"));
        if (!java.util.Arrays.asList(args).contains("--postgres")) {
            InicializadorDemo.iniciar();
            int agregados = DatosEjemploCU28.cargar();
            if (agregados > 0) System.out.println("Ejemplos nuevos de bolsines: " + agregados + " (sin modificar recepciones anteriores).");
        }
        CargadorDeDatos.cargar(); // Detectar problemas de base antes de anunciar que la app está lista.
        int puerto = Integer.getInteger("bolsin.port", 8080);
        var servidor = new ServidorLocal(puerto);
        Runtime.getRuntime().addShutdownHook(new Thread(servidor::close));
        servidor.iniciar();
        System.out.println("Bolsines web: http://localhost:" + servidor.getPuerto());
        System.out.println("Ctrl+C para detener. Los datos quedan guardados en la base.");
    }
}
