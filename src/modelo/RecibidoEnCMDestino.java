package modelo;

/** El bolsin ya fue recibido en la CM destino. No admite mas transiciones en el CU28. */
public final class RecibidoEnCMDestino extends Estado {

    public RecibidoEnCMDestino() {
        super("Recibido en CM Destino", "Bolsin recibido en CM destino", AMBITO_BOLSIN);
    }
}
