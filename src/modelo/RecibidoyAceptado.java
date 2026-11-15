package modelo;

/** Estado concreto que el CU28 crea al registrar la recepción correcta. */
public final class RecibidoyAceptado extends Estado {

    public RecibidoyAceptado() {
        this(buscarEstadoExistente());
    }

    private RecibidoyAceptado(Estado estadoExistente) {
        super(estadoExistente.getNombre(), estadoExistente.getDescripcion(), RecibidoyAceptado.AMBITO_REMITO, false);
        setId(estadoExistente.getId());
    }

    private static Estado buscarEstadoExistente() {
        return Estado.getEstados().stream()
                .filter(estado -> estado.esAmbitoRemito() && estado.esRecibidoyAceptado())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el estado Recibido y Aceptado"));
    }
}
