package persistencia;

import modelo.Bolsin;
import modelo.CambioEstadoBolsin;
import modelo.CambioEstadoDocumentacion;
import modelo.DetalleRemito;
import modelo.Documentacion;
import modelo.Empleado;
import modelo.Estado;
import modelo.Remito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// Guarda en la base lo que dejo el CU28 sobre el bolsin elegido, sus remitos y
// su documentacion: los CambioEstado nuevos, el cierre (fechaHoraFin) de los
// anteriores y el estado de cada remito. Todo en una sola transaccion: o se
// guarda completo o no se guarda nada.
public class RepositorioRecepcion {

    // Los estados nuevos (crearEstado) no tienen id: se resuelve por ambito y nombre.
    private final Map<String, Integer> idsDeEstado = new HashMap<>();

    public void guardar(Bolsin bolsin, List<Remito> remitos) {
        // Los ids de los registros nuevos se asignan a los objetos recien despues
        // del commit, asi un rollback no deja ids que no existen en la base.
        List<Runnable> asignarIds = new ArrayList<>();
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try {
                verificarVigencia(c, bolsin);
                guardarCambiosEstadoBolsin(c, bolsin, asignarIds);
                for (Remito remito : remitos) {
                    actualizarEstadoRemito(c, remito);
                    for (DetalleRemito detalle : remito.obtenerDatosDocumentacion()) {
                        guardarCambiosEstadoDocumentacion(c, detalle.obtenerDatosDocumentacion(), asignarIds);
                    }
                }
                c.commit();
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudo guardar la recepcion del bolsin: " + e.getMessage(), e);
        }
        asignarIds.forEach(Runnable::run);
    }

    // Serializa recepciones del mismo bolsín y detecta una pantalla desactualizada.
    private void verificarVigencia(Connection c, Bolsin bolsin) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement("select id_bolsin from bolsin where id_bolsin = ? for update")) {
            ps.setInt(1, requerido(bolsin.getId(), "bolsin"));
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) throw new PersistenciaException("El bolsín ya no existe.");
            }
        }
        var anterior = bolsin.getCambiosEstado().stream()
                .filter(h -> h.getId() != null && h.getFechaHoraFin() != null)
                .max(java.util.Comparator.comparing(CambioEstadoBolsin::getFechaHoraInicio))
                .orElseThrow(() -> new PersistenciaException("Falta el historial previo del bolsín."));
        verificarHistorial(c, "cambio_estado_bolsin", "id_bolsin", "id_cambio_estado_bolsin",
                bolsin.getId(), anterior.getId());
        var documentos = bolsin.buscarRemitos().stream().flatMap(r -> r.obtenerDatosDocumentacion().stream())
                .map(DetalleRemito::obtenerDatosDocumentacion).distinct()
                .sorted(java.util.Comparator.comparing(Documentacion::getId)).toList();
        for (Documentacion doc : documentos) {
            try (PreparedStatement ps = c.prepareStatement("select id_documentacion from documentacion where id_documentacion = ? for update")) {
                ps.setInt(1, requerido(doc.getId(), "documentacion"));
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) throw new PersistenciaException("La documentación ya no existe.");
                }
            }
            var previo = doc.getCambiosEstado().stream().filter(h -> h.getId() != null && h.getFechaHoraFin() != null)
                    .max(java.util.Comparator.comparing(CambioEstadoDocumentacion::getFechaHoraInicio))
                    .orElseThrow(() -> new PersistenciaException("Falta el historial previo de la documentación."));
            verificarHistorial(c, "cambio_estado_documentacion", "id_documentacion", "id_cambio_estado_doc",
                    doc.getId(), previo.getId());
        }
    }

    private void verificarHistorial(Connection c, String tabla, String padre, String clave, int id, int esperado)
            throws SQLException {
        // Identificadores internos constantes; los valores se parametrizan.
        try (PreparedStatement ps = c.prepareStatement("select " + clave + " from " + tabla
                + " where " + padre + " = ? and fecha_hora_fin is null")) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next() || rs.getInt(1) != esperado || rs.next()) {
                    throw new PersistenciaException("Los datos cambiaron en otra sesión. Vuelva a abrir la aplicación.");
                }
            }
        }
    }

    private void guardarCambiosEstadoBolsin(Connection c, Bolsin bolsin, List<Runnable> asignarIds)
            throws SQLException {
        for (CambioEstadoBolsin cambio : bolsin.getCambiosEstado()) {
            if (cambio.getId() == null) {
                int id = insertar(c,
                        "insert into cambio_estado_bolsin "
                                + "(id_bolsin, id_estado, fecha_hora_inicio, fecha_hora_fin, id_empleado) "
                                + "values (?, ?, ?, ?, ?)",
                        requerido(bolsin.getId(), "bolsin"), idEstado(c, cambio.getEstado()),
                        cambio.getFechaHoraInicio(), cambio.getFechaHoraFin(), cambio.getResponsable());
                asignarIds.add(() -> cambio.setId(id));
            } else if (cambio.getFechaHoraFin() != null) {
                cerrar(c, "update cambio_estado_bolsin set fecha_hora_fin = ? where id_cambio_estado_bolsin = ?",
                        cambio.getFechaHoraFin(), cambio.getId());
            }
        }
    }

    private void guardarCambiosEstadoDocumentacion(Connection c, Documentacion doc, List<Runnable> asignarIds)
            throws SQLException {
        for (CambioEstadoDocumentacion cambio : doc.getCambiosEstado()) {
            if (cambio.getId() == null) {
                int id = insertar(c,
                        "insert into cambio_estado_documentacion "
                                + "(id_documentacion, id_estado, fecha_hora_inicio, fecha_hora_fin, id_empleado) "
                                + "values (?, ?, ?, ?, ?)",
                        requerido(doc.getId(), "documentacion"), idEstado(c, cambio.getEstado()),
                        cambio.getFechaHoraInicio(), cambio.getFechaHoraFin(), cambio.getResponsable());
                asignarIds.add(() -> cambio.setId(id));
            } else if (cambio.getFechaHoraFin() != null) {
                cerrar(c, "update cambio_estado_documentacion set fecha_hora_fin = ? where id_cambio_estado_doc = ?",
                        cambio.getFechaHoraFin(), cambio.getId());
            }
        }
    }

    private void actualizarEstadoRemito(Connection c, Remito remito) throws SQLException {
        if (remito.getEstado() == null) {
            return;
        }
        try (PreparedStatement ps = c.prepareStatement("update remito set id_estado = ? where id_remito = ?")) {
            ps.setInt(1, idEstado(c, remito.getEstado()));
            ps.setInt(2, requerido(remito.getId(), "remito"));
            if (ps.executeUpdate() != 1) throw new SQLException("El remito ya no existe");
        }
    }

    private int insertar(Connection c, String sql, int idPadre, int idEstado,
            LocalDateTime inicio, LocalDateTime fin, Empleado responsable) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql, java.sql.Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, idPadre);
            ps.setInt(2, idEstado);
            ps.setTimestamp(3, Timestamp.valueOf(inicio));
            ps.setTimestamp(4, fin == null ? null : Timestamp.valueOf(fin));
            if (responsable == null || responsable.getId() == null) {
                ps.setNull(5, java.sql.Types.INTEGER);
            } else {
                ps.setInt(5, responsable.getId());
            }
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("No se generó la clave del historial");
                return rs.getInt(1);
            }
        }
    }

    private void cerrar(Connection c, String sql, LocalDateTime fin, int id) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setTimestamp(1, Timestamp.valueOf(fin));
            ps.setInt(2, id);
            ps.executeUpdate();
        }
    }

    private int idEstado(Connection c, Estado estado) throws SQLException {
        if (estado.getId() != null) {
            return estado.getId();
        }
        String clave = estado.getAmbito() + "/" + estado.getNombre();
        Integer id = idsDeEstado.get(clave);
        if (id == null) {
            try (PreparedStatement ps = c.prepareStatement(
                    "select id_estado from estado where nombre = ? and ambito = ?")) {
                ps.setString(1, estado.getNombre());
                ps.setString(2, estado.getAmbito());
                try (ResultSet rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        throw new PersistenciaException("No existe el estado '" + estado.getNombre() + "' ("
                                + estado.getAmbito() + ") en la base. Revisá el catálogo de estados.");
                    }
                    id = rs.getInt(1);
                }
            }
            idsDeEstado.put(clave, id);
        }
        return id;
    }

    private int requerido(Integer id, String entidad) {
        if (id == null) {
            throw new PersistenciaException("El objeto " + entidad + " no tiene id: no se cargo desde la base.");
        }
        return id;
    }
}
