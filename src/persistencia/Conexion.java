package persistencia;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Abre la conexion JDBC a la base (Postgres de Supabase). Los datos de
// conexion NO van en el codigo ni en git. Se leen de variables de entorno o,
// si no estan definidas, del archivo .env de la carpeta desde donde se ejecuta
// el programa (ver .env.example):
//
//   SUPABASE_DB_URL       ej: jdbc:postgresql://HOST:5432/postgres?sslmode=require
//   SUPABASE_DB_USER      ej: postgres.<ref-del-proyecto>
//   SUPABASE_DB_PASSWORD  la contrasena de la base
public final class Conexion {

    private static final String ENV_URL = "SUPABASE_DB_URL";
    private static final String ENV_USER = "SUPABASE_DB_USER";
    private static final String ENV_PASSWORD = "SUPABASE_DB_PASSWORD";
    private static final Path ARCHIVO_ENV = Path.of(".env");

    private Conexion() {
    }

    public static Connection abrir() throws SQLException {
        String urlDemo = System.getProperty("bolsin.demo.url");
        if (urlDemo != null) return DriverManager.getConnection(urlDemo, "sa", "");
        Map<String, String> env = leerArchivoEnv();
        return DriverManager.getConnection(
                requerida(ENV_URL, env), requerida(ENV_USER, env), requerida(ENV_PASSWORD, env));
    }

    // La variable de entorno del sistema tiene prioridad sobre el .env.
    private static String requerida(String nombre, Map<String, String> env) {
        String valor = System.getenv(nombre);
        if (valor == null || valor.isBlank()) {
            valor = env.get(nombre);
        }
        if (valor == null || valor.isBlank()) {
            throw new PersistenciaException("Falta configurar " + nombre
                    + " (variable de entorno o archivo .env)");
        }
        return valor;
    }

    // Formato simple CLAVE=valor, una por linea; ignora vacias y comentarios (#).
    private static Map<String, String> leerArchivoEnv() {
        Map<String, String> valores = new HashMap<>();
        if (!Files.isRegularFile(ARCHIVO_ENV)) {
            return valores;
        }
        try {
            List<String> lineas = Files.readAllLines(ARCHIVO_ENV, StandardCharsets.UTF_8);
            for (String linea : lineas) {
                String l = linea.strip();
                int igual = l.indexOf('=');
                if (l.isEmpty() || l.startsWith("#") || igual <= 0) {
                    continue;
                }
                valores.put(l.substring(0, igual).strip(), l.substring(igual + 1).strip());
            }
        } catch (IOException e) {
            throw new PersistenciaException("No se pudo leer el archivo .env: " + e.getMessage(), e);
        }
        return valores;
    }
}
