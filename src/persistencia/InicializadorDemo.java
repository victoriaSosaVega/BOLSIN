package persistencia;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;

/** Base H2 persistente de demostración, usada por defecto salvo que se solicite --postgres. */
public final class InicializadorDemo {
    private InicializadorDemo() { }

    public static void iniciar() {
        if (System.getProperty("bolsin.demo.url") == null) {
            System.setProperty("bolsin.demo.url", "jdbc:h2:./data/cu28;MODE=PostgreSQL;DATABASE_TO_LOWER=TRUE");
        }
        try (Connection c = Conexion.abrir()) {
            ejecutar(c, "db/schema.sql");
            c.setAutoCommit(false);
            try (var st = c.createStatement(); var rs = st.executeQuery("select count(*) from estado")) {
                rs.next();
                if (rs.getInt(1) == 0) ejecutar(c, "db/demo.sql");
                c.commit();
            } catch (Exception e) {
                c.rollback();
                throw e;
            }
        } catch (Exception e) {
            throw new PersistenciaException("No se pudo preparar la base de demostración: " + e.getMessage(), e);
        }
    }

    private static void ejecutar(Connection c, String archivo) throws Exception {
        String sql = Files.readString(Path.of(archivo)).replaceAll("(?m)--[^\\r\\n]*", "");
        for (String sentencia : sql.split(";")) {
            if (!sentencia.isBlank()) {
                try (var st = c.createStatement()) { st.execute(sentencia); }
            }
        }
    }
}
