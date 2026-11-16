package persistencia;

import java.sql.*;
import java.time.*;

/** Datos ficticios adicionales para practicar CU28. Solo agrega bolsines inexistentes. */
public final class DatosEjemploCU28 {
    private record Ejemplo(int numero, String origen, String nombreOrigen, int[] documentosPorRemito,
                           boolean recibido, boolean otroDestino) { }
    private static final Ejemplo[] EJEMPLOS = {
        new Ejemplo(600, "CBA", "CM Cordoba", new int[]{1}, false, false),
        new Ejemplo(601, "RC", "CM Río Cuarto", new int[]{3}, false, false),
        new Ejemplo(602, "SFCO", "CM San Francisco", new int[]{2, 2}, false, false),
        new Ejemplo(603, "ROS", "CM Rosario", new int[]{2}, false, false),
        new Ejemplo(604, "MZA", "CM Mendoza", new int[]{1, 2}, false, false),
        new Ejemplo(605, "PAR", "CM Paraná", new int[]{4}, false, false),
        new Ejemplo(606, "SFE", "CM Santa Fe", new int[]{2, 2, 2}, false, false),
        new Ejemplo(607, "TUC", "CM Tucumán", new int[]{2}, false, false),
        new Ejemplo(608, "CBA", "CM Cordoba", new int[]{3, 2}, false, false),
        new Ejemplo(609, "RC", "CM Río Cuarto", new int[]{1}, false, false),
        new Ejemplo(610, "ROS", "CM Rosario", new int[]{1, 2}, false, false),
        new Ejemplo(611, "MZA", "CM Mendoza", new int[]{3, 2, 2}, false, false),
        new Ejemplo(612, "SFCO", "CM San Francisco", new int[]{2}, true, false),
        new Ejemplo(613, "SFE", "CM Santa Fe", new int[]{1}, false, true)
    };
    private static final String[] TIPOS = {"Expediente", "Dictamen", "Estudio médico", "Carta documento", "Informe", "Certificado"};
    private static final String[] ASUNTOS = {
        "Solicitud de evaluación de incapacidad laboral", "Dictamen de revisión de antecedentes",
        "Resultados de estudios complementarios", "Notificación de audiencia médica",
        "Informe de evolución y seguimiento", "Certificado de aptitud laboral"
    };
    private DatosEjemploCU28() { }

    public static int cargar() {
        if (System.getProperty("bolsin.demo.url") == null) {
            throw new PersistenciaException("Los ejemplos adicionales solo se cargan en la base local de demostración.");
        }
        try (Connection c = Conexion.abrir()) {
            c.setAutoCommit(false);
            try {
                int destinoVM = id(c, "select id_cm from cm where codigo = ?", "VM");
                int empleadoDestino = id(c, "select id_empleado from empleado where email = ?", "ana.perez@organismo.gob.ar");
                int enviado = estado(c, "Enviado", "Bolsin");
                int recibido = estado(c, "Recibido en CM Destino", "Bolsin");
                int remitoEnviado = estado(c, "En Bolsin Enviado", "Remito");
                int remitoRecibido = estado(c, "Recibido y Aceptado", "Remito");
                int docEnviado = estado(c, "En Bolsin Enviado", "Documentacion");
                int docRecibida = estado(c, "Recibida y Aceptada", "Documentacion");
                int[] tipos = new int[TIPOS.length];
                for (int i = 0; i < tipos.length; i++) {
                    tipos[i] = buscar(c, "select id_tipo_doc from tipo_documentacion where nombre = ?", TIPOS[i]);
                    if (tipos[i] == 0) tipos[i] = insertar(c, "insert into tipo_documentacion(nombre, descripcion) values (?, ?)", TIPOS[i], "Tipo de documentación para la demostración CU28");
                }
                int agregados = 0;
                for (Ejemplo e : EJEMPLOS) {
                    if (buscar(c, "select id_bolsin from bolsin where numero_bolsin = ?", e.numero) != 0) continue;
                    int origen = buscar(c, "select id_cm from cm where codigo = ?", e.origen);
                    if (origen == 0) origen = insertar(c, "insert into cm(codigo,nombre,direccion,email,telefono) values (?,?,?,?,?)",
                            e.origen, e.nombreOrigen, "Dirección ficticia para demostración", e.origen.toLowerCase() + "@ejemplo.invalid", "Sin teléfono de contacto");
                    String email = "encargado." + e.origen.toLowerCase() + "@ejemplo.invalid";
                    int responsableOrigen = buscar(c, "select id_empleado from empleado where email = ?", email);
                    if (responsableOrigen == 0) responsableOrigen = insertar(c,
                            "insert into empleado(nombre,apellido,email,id_cm) values (?,?,?,?)", "Empleado", "Demo " + e.origen, email, origen);
                    int destino = e.otroDestino ? id(c, "select id_cm from cm where codigo = ?", "CBA") : destinoVM;
                    LocalDate fecha = LocalDate.now().minusDays(1 + (e.numero % 4));
                    LocalDateTime envio = fecha.atTime(9, 0);
                    LocalDateTime recepcion = LocalDate.now().atStartOfDay();
                    String precinto = "PRE-2026-" + e.numero;
                    int bolsin = insertar(c, "insert into bolsin(numero_bolsin,numero_precinto,fecha,peso,id_cm_origen,id_cm_destino) values (?,?,?,?,?,?)",
                            e.numero, precinto, Date.valueOf(fecha), 150f + (e.numero % 12) * 75f, origen, destino);
                    insertar(c, "insert into cambio_estado_bolsin(id_bolsin,id_estado,fecha_hora_inicio,fecha_hora_fin,id_empleado) values (?,?,?,?,?)",
                            bolsin, enviado, Timestamp.valueOf(envio), e.recibido ? Timestamp.valueOf(recepcion) : null, responsableOrigen);
                    if (e.recibido) insertar(c, "insert into cambio_estado_bolsin(id_bolsin,id_estado,fecha_hora_inicio,id_empleado) values (?,?,?,?)",
                            bolsin, recibido, Timestamp.valueOf(recepcion), empleadoDestino);
                    int documento = 0;
                    for (int r = 0; r < e.documentosPorRemito.length; r++) {
                        int remito = insertar(c, "insert into remito(numero,fecha,id_bolsin,id_estado,id_cm_origen,id_cm_destino) values (?,?,?,?,?,?)",
                                e.numero * 10 + r + 1, Date.valueOf(fecha), bolsin, e.recibido ? remitoRecibido : remitoEnviado, origen, destino);
                        for (int d = 0; d < e.documentosPorRemito[r]; d++) {
                            int n = ++documento;
                            int tipo = (e.numero + n - 1) % tipos.length;
                            int doc = insertar(c, "insert into documentacion(numero,asunto,fecha_pase,id_tipo_doc,id_cm_origen) values (?,?,?,?,?)",
                                    e.numero * 100 + n, ASUNTOS[tipo] + " · DEMO-" + e.numero + "-" + n,
                                    Date.valueOf(fecha.minusDays(1)), tipos[tipo], origen);
                            insertar(c, "insert into detalle_remito(id_remito,id_documentacion,area_cmc_destino) values (?,?,?)", remito, doc, null);
                            insertar(c, "insert into cambio_estado_documentacion(id_documentacion,id_estado,fecha_hora_inicio,fecha_hora_fin,id_empleado) values (?,?,?,?,?)",
                                    doc, docEnviado, Timestamp.valueOf(envio), e.recibido ? Timestamp.valueOf(recepcion) : null, responsableOrigen);
                            if (e.recibido) insertar(c, "insert into cambio_estado_documentacion(id_documentacion,id_estado,fecha_hora_inicio,id_empleado) values (?,?,?,?)",
                                    doc, docRecibida, Timestamp.valueOf(recepcion), empleadoDestino);
                        }
                    }
                    agregados++;
                }
                c.commit();
                return agregados;
            } catch (SQLException | RuntimeException e) {
                c.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new PersistenciaException("No se pudieron cargar los ejemplos de bolsines: " + e.getMessage(), e);
        }
    }
    private static int estado(Connection c, String nombre, String ambito) throws SQLException {
        return id(c, "select id_estado from estado where nombre = ? and ambito = ?", nombre, ambito);
    }
    private static int id(Connection c, String sql, Object... valores) throws SQLException {
        int resultado = buscar(c, sql, valores);
        if (resultado == 0) throw new PersistenciaException("Faltan datos base necesarios para cargar los ejemplos.");
        return resultado;
    }
    private static int buscar(Connection c, String sql, Object... valores) throws SQLException {
        try (var ps = c.prepareStatement(sql)) {
            for (int i=0; i<valores.length; i++) ps.setObject(i+1, valores[i]);
            try (var rs = ps.executeQuery()) { return rs.next() ? rs.getInt(1) : 0; }
        }
    }
    private static int insertar(Connection c, String sql, Object... valores) throws SQLException {
        try (var ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            for (int i=0; i<valores.length; i++) ps.setObject(i+1, valores[i]);
            ps.executeUpdate();
            try (var rs = ps.getGeneratedKeys()) {
                if (!rs.next()) throw new SQLException("No se generó el identificador del ejemplo.");
                return rs.getInt(1);
            }
        }
    }
}
