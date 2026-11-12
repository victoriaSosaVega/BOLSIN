package persistencia;

import modelo.Bolsin;
import modelo.CM;
import modelo.CambioEstadoBolsin;
import modelo.CambioEstadoDocumentacion;
import modelo.DetalleRemito;
import modelo.Documentacion;
import modelo.Empleado;
import modelo.Estado;
import modelo.Remito;
import modelo.Sesion;
import modelo.TipoDocumentacion;
import modelo.Usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// Lee la base al arrancar y arma los objetos del modelo. Reemplaza al seed que
// antes hacia MainCU28: los constructores de Estado y Bolsin siguen
// registrandose en sus listas, asi que Estado.getEstados() y
// Bolsin.buscarBolsinesEnEstadoEnviadoParaCMEmpleado() funcionan igual que en
// el diagrama de secuencia, ahora con los datos de la base.
public final class CargadorDeDatos {

    private CargadorDeDatos() {
    }

    // Devuelve la sesion abierta (la mas reciente sin fecha_hora_fin). El login
    // no es parte del CU28, asi que la sesion tiene que existir en la base.
    public static Sesion cargar() {
        try (Connection c = Conexion.abrir()) {
            Map<Integer, CM> cms = cargarCMs(c);
            Map<Integer, Estado> estados = cargarEstados(c);
            Map<Integer, TipoDocumentacion> tipos = cargarTiposDocumentacion(c);
            Map<Integer, Empleado> empleados = cargarEmpleados(c, cms);
            Map<Integer, Usuario> usuarios = cargarUsuarios(c, empleados);
            Map<Integer, Documentacion> documentaciones = cargarDocumentaciones(c, tipos);
            Map<Integer, Bolsin> bolsines = cargarBolsines(c, cms);
            Map<Integer, Remito> remitos = cargarRemitos(c, estados, bolsines);
            cargarDetallesRemito(c, remitos, documentaciones);
            cargarCambiosEstadoBolsin(c, estados, bolsines);
            cargarCambiosEstadoDocumentacion(c, estados, documentaciones);
            return cargarSesionAbierta(c, usuarios);
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron cargar los datos de la base: " + e.getMessage(), e);
        }
    }

    private static Map<Integer, CM> cargarCMs(Connection c) throws SQLException {
        Map<Integer, CM> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_cm, codigo, nombre, direccion, email, telefono from cm")) {
            while (rs.next()) {
                CM cm = new CM(rs.getString("codigo"), rs.getString("nombre"),
                        rs.getString("direccion"), rs.getString("email"), rs.getString("telefono"));
                cm.setId(rs.getInt("id_cm"));
                mapa.put(cm.getId(), cm);
            }
        }
        return mapa;
    }

    private static Map<Integer, Estado> cargarEstados(Connection c) throws SQLException {
        Map<Integer, Estado> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_estado, nombre, descripcion, ambito from estado order by id_estado")) {
            while (rs.next()) {
                Estado e = new Estado(rs.getString("nombre"), rs.getString("descripcion"), rs.getString("ambito"));
                e.setId(rs.getInt("id_estado"));
                mapa.put(e.getId(), e);
            }
        }
        return mapa;
    }

    private static Map<Integer, TipoDocumentacion> cargarTiposDocumentacion(Connection c) throws SQLException {
        Map<Integer, TipoDocumentacion> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_tipo_doc, nombre, descripcion from tipo_documentacion")) {
            while (rs.next()) {
                TipoDocumentacion t = new TipoDocumentacion(rs.getString("nombre"), rs.getString("descripcion"));
                t.setId(rs.getInt("id_tipo_doc"));
                mapa.put(t.getId(), t);
            }
        }
        return mapa;
    }

    private static Map<Integer, Empleado> cargarEmpleados(Connection c, Map<Integer, CM> cms) throws SQLException {
        Map<Integer, Empleado> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_empleado, nombre, apellido, email, id_cm from empleado")) {
            while (rs.next()) {
                Empleado e = new Empleado(rs.getString("nombre"), rs.getString("apellido"),
                        rs.getString("email"), cms.get(rs.getInt("id_cm")));
                e.setId(rs.getInt("id_empleado"));
                mapa.put(e.getId(), e);
            }
        }
        return mapa;
    }

    private static Map<Integer, Usuario> cargarUsuarios(Connection c, Map<Integer, Empleado> empleados)
            throws SQLException {
        Map<Integer, Usuario> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_usuario, nombre, contrasena, id_empleado from usuario")) {
            while (rs.next()) {
                mapa.put(rs.getInt("id_usuario"), new Usuario(rs.getString("nombre"),
                        rs.getString("contrasena"), empleados.get(rs.getInt("id_empleado"))));
            }
        }
        return mapa;
    }

    private static Map<Integer, Documentacion> cargarDocumentaciones(Connection c,
            Map<Integer, TipoDocumentacion> tipos) throws SQLException {
        Map<Integer, Documentacion> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_documentacion, numero, asunto, fecha_pase, id_tipo_doc from documentacion")) {
            while (rs.next()) {
                Documentacion d = new Documentacion(rs.getInt("numero"), rs.getString("asunto"),
                        rs.getDate("fecha_pase").toLocalDate(), tipos.get(rs.getInt("id_tipo_doc")));
                d.setId(rs.getInt("id_documentacion"));
                mapa.put(d.getId(), d);
            }
        }
        return mapa;
    }

    // El constructor de Bolsin lo agrega solo a la lista estatica de bolsines.
    private static Map<Integer, Bolsin> cargarBolsines(Connection c, Map<Integer, CM> cms) throws SQLException {
        Map<Integer, Bolsin> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_bolsin, numero_bolsin, numero_precinto, fecha, peso, id_cm_origen, id_cm_destino "
                             + "from bolsin order by id_bolsin")) {
            while (rs.next()) {
                double peso = rs.getDouble("peso"); // peso admite null en la base: queda en 0
                Bolsin b = new Bolsin(rs.getInt("numero_bolsin"), rs.getString("numero_precinto"),
                        rs.getDate("fecha").toLocalDate(), peso,
                        cms.get(rs.getInt("id_cm_origen")), cms.get(rs.getInt("id_cm_destino")));
                b.setId(rs.getInt("id_bolsin"));
                mapa.put(b.getId(), b);
            }
        }
        return mapa;
    }

    private static Map<Integer, Remito> cargarRemitos(Connection c, Map<Integer, Estado> estados,
            Map<Integer, Bolsin> bolsines) throws SQLException {
        Map<Integer, Remito> mapa = new HashMap<>();
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_remito, numero, fecha, id_bolsin, id_estado from remito order by id_remito")) {
            while (rs.next()) {
                Remito r = new Remito(rs.getInt("numero"), rs.getDate("fecha").toLocalDate());
                r.setId(rs.getInt("id_remito"));
                int idEstado = rs.getInt("id_estado");
                if (!rs.wasNull()) {
                    r.setEstado(estados.get(idEstado));
                }
                int idBolsin = rs.getInt("id_bolsin");
                if (!rs.wasNull()) {
                    bolsines.get(idBolsin).agregarRemito(r);
                }
                mapa.put(r.getId(), r);
            }
        }
        return mapa;
    }

    private static void cargarDetallesRemito(Connection c, Map<Integer, Remito> remitos,
            Map<Integer, Documentacion> documentaciones) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_remito, id_documentacion, area_cmc_destino from detalle_remito "
                             + "order by id_detalle_remito")) {
            while (rs.next()) {
                remitos.get(rs.getInt("id_remito")).agregarDetalle(new DetalleRemito(
                        rs.getString("area_cmc_destino"), documentaciones.get(rs.getInt("id_documentacion"))));
            }
        }
    }

    private static void cargarCambiosEstadoBolsin(Connection c, Map<Integer, Estado> estados,
            Map<Integer, Bolsin> bolsines) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_cambio_estado_bolsin, id_bolsin, id_estado, fecha_hora_inicio, fecha_hora_fin "
                             + "from cambio_estado_bolsin order by fecha_hora_inicio, id_cambio_estado_bolsin")) {
            while (rs.next()) {
                CambioEstadoBolsin cambio = new CambioEstadoBolsin(estados.get(rs.getInt("id_estado")),
                        fecha(rs, "fecha_hora_inicio"), fecha(rs, "fecha_hora_fin"));
                cambio.setId(rs.getInt("id_cambio_estado_bolsin"));
                bolsines.get(rs.getInt("id_bolsin")).agregarCambioEstado(cambio);
            }
        }
    }

    private static void cargarCambiosEstadoDocumentacion(Connection c, Map<Integer, Estado> estados,
            Map<Integer, Documentacion> documentaciones) throws SQLException {
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery(
                     "select id_cambio_estado_doc, id_documentacion, id_estado, fecha_hora_inicio, fecha_hora_fin "
                             + "from cambio_estado_documentacion order by fecha_hora_inicio, id_cambio_estado_doc")) {
            while (rs.next()) {
                CambioEstadoDocumentacion cambio = new CambioEstadoDocumentacion(
                        estados.get(rs.getInt("id_estado")),
                        fecha(rs, "fecha_hora_inicio"), fecha(rs, "fecha_hora_fin"));
                cambio.setId(rs.getInt("id_cambio_estado_doc"));
                documentaciones.get(rs.getInt("id_documentacion")).agregarCambioEstado(cambio);
            }
        }
    }

    private static Sesion cargarSesionAbierta(Connection c, Map<Integer, Usuario> usuarios) throws SQLException {
        String sql = "select id_usuario from sesion where fecha_hora_fin is null "
                + "order by fecha_hora_inicio desc limit 1";
        try (PreparedStatement ps = c.prepareStatement(sql); ResultSet rs = ps.executeQuery()) {
            if (!rs.next()) {
                throw new PersistenciaException(
                        "No hay una sesion abierta en la base (tabla sesion con fecha_hora_fin vacia).");
            }
            return new Sesion(usuarios.get(rs.getInt("id_usuario")));
        }
    }

    private static LocalDateTime fecha(ResultSet rs, String columna) throws SQLException {
        Timestamp t = rs.getTimestamp(columna);
        return t == null ? null : t.toLocalDateTime();
    }
}
