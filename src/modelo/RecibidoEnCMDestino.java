package modelo;

/** Estado concreto que el CU28 crea al registrar la recepción correcta. */
public final class RecibidoEnCMDestino extends Estado {

    public RecibidoEnCMDestino() {
        this(buscarEstadoExistente());
    }

    private RecibidoEnCMDestino(Estado estadoExistente) {
        super(estadoExistente.getNombre(), estadoExistente.getDescripcion(), RecibidoEnCMDestino.AMBITO_BOLSIN, false);
        setId(estadoExistente.getId());
    }

    private static Estado buscarEstadoExistente() {
        return Estado.getEstados().stream()
                .filter(estado -> estado.esAmbitoBolsin() && estado.esRecibidoEnCMDestino())
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("No existe el estado Recibido en CM Destino"));
    }
}
