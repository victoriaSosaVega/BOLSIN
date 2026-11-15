package modelo;

/** Estado concreto que el CU28 crea al registrar la recepción correcta. */
public final class RecibidayAceptada extends Estado {

    public RecibidayAceptada() {
        this(buscarEstadoExistente());
    }

    private RecibidayAceptada(Estado estadoExistente) {
        super(estadoExistente.getNombre(), estadoExistente.getDescripcion(), RecibidayAceptada.AMBITO_DOCUMENTACION, false);
        setId(estadoExistente.getId());
    }

    private static Estado buscarEstadoExistente() {
        return Estado.getEstados().stream()
                .filter(estado -> estado.esAmbitoDocumentacion() && estado.esRecibidayAceptada())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el estado Recibida y Aceptada"));
    }
}
