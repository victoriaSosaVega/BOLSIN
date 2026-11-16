package modelo;

import java.util.ArrayList;
import java.util.List;

/**
 * Estado simple (sin patron State): lo usan el Bolsin y el Remito, cuyos estados
 * se registran en el historial o en el propio objeto y no cambian el comportamiento.
 * El patron State se aplica solo a la Documentacion (ver EstadoDocumentacion).
 */
public class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";
    public static final String AMBITO_DOCUMENTACION = "Documentacion";

    // "Persistencia" simulada: todo Estado simple creado (por la carga desde la base)
    // queda registrado aca, asi el Gestor puede buscarlo por ambito + condicion.
    private static final List<Estado> estados = new ArrayList<>();

    // Clave de la tabla estado (solo para persistencia; no forma parte del diagrama).
    // Los estados de documentacion creados por crearEstado() no la tienen: se resuelve
    // por nombre y ambito al guardar.
    private Integer id;
    private final String nombre;
    private final String descripcion;
    private final String ambito;

    public Estado(String nombre, String descripcion, String ambito) {
        this(nombre, descripcion, ambito, true);
    }

    // Las subclases (EstadoDocumentacion) no se registran: se crean al cambiar de estado.
    protected Estado(String nombre, String descripcion, String ambito, boolean registrar) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ambito = ambito;
        if (registrar) {
            estados.add(this);
        }
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public String getAmbito() {
        return ambito;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equals(ambito);
    }

    public boolean esAmbitoRemito() {
        return AMBITO_REMITO.equals(ambito);
    }

    public boolean esEnviado() {
        return esAmbitoBolsin() && "Enviado".equals(nombre);
    }

    public boolean esRecibidoEnCMDestino() {
        return "Recibido en CM Destino".equals(nombre);
    }

    public boolean esRecibidoyAceptado() {
        return "Recibido y Aceptado".equals(nombre);
    }

    public static void limpiarCatalogo() { estados.clear(); }

    // Lista de estados simples cargados: el Gestor la recorre para buscar los que necesita.
    public static List<Estado> getEstados() {
        return List.copyOf(estados);
    }
}
