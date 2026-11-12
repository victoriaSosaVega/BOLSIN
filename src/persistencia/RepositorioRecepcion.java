package persistencia;

import modelo.Bolsin;
import modelo.CambioEstadoBolsin;
import modelo.CambioEstadoDocumentacion;
import modelo.DetalleRemito;
import modelo.Documentacion;
import modelo.Remito;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

// Guarda en la base lo que dejo el CU28 sobre el bolsin elegido, sus remitos y
// su documentacion: los CambioEstado nuevos, el cierre (fechaHoraFin) de los
// anteriores y el estado de cada remito. Todo en una sola transaccion: o se
// guarda completo o no se guarda nada.
public class RepositorioRecepcion {

    public void guardar(Bolsin bolsin, List<Remito> remitos) {
        // Los ids de los registros nuevos se asignan a los objetos recien despues
        // del commit, asi un rollback no deja ids que no existen en la base.
        List<Runnable> asignarIds = new ArrayList<>();
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try {
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

    private void guardarCambiosEstadoBolsin(Connection c, Bolsin bolsin, List<Runnable> asignarIds)
            throws SQLException {
        for (CambioEstadoBolsin cambio : bolsin.getCambiosEstado()) {
            if (cambio.getId() == null) {
                int id = insertar(c,
                        "insert into cambio_estado_bolsin (id_bolsin, id_estado, fecha_hora_inicio, fecha_hora_fin) "
                                + "values (?, ?, ?, ?) returning id_cambio_estado_bolsin",
                        requerido(bolsin.getId(), "bolsin"), requerido(cambio.getEstado().getId(), "estado"),
                        cambio.getFechaHoraInicio(), cambio.getFechaHoraFin());
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
                                + "(id_documentacion, id_estado, fecha_hora_inicio, fecha_hora_fin) "
                                + "values (?, ?, ?, ?) returning id_cambio_estado_doc",
                        requerido(doc.getId(), "documentacion"), requerido(cambio.getEstado().getId(), "estado"),
                        cambio.getFechaHoraInicio(), cambio.getFechaHoraFin());
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
            ps.setInt(1, requerido(remito.getEstado().getId(), "estado"));
            ps.setInt(2, requerido(remito.getId(), "remito"));
            ps.executeUpdate();
        }
    }

    private int insertar(Connection c, String sql, int idPadre, int idEstado,
            LocalDateTime inicio, LocalDateTime fin) throws SQLException {
        try (PreparedStatement ps = c.prepareStatement(sql)) {
            ps.setInt(1, idPadre);
            ps.setInt(2, idEstado);
            ps.setTimestamp(3, Timestamp.valueOf(inicio));
            ps.setTimestamp(4, fin == null ? null : Timestamp.valueOf(fin));
            try (ResultSet rs = ps.executeQuery()) {
                rs.next();
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

    private int requerido(Integer id, String entidad) {
        if (id == null) {
            throw new PersistenciaException("El objeto " + entidad + " no tiene id: no se cargo desde la base.");
        }
        return id;
    }
}
