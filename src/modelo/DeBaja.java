package modelo;

/** Estado final: la documentacion se dio de baja. No admite transiciones. */
public final class DeBaja extends Estado {

    public DeBaja() {
        super("De Baja", "Documentacion dada de baja", AMBITO_DOCUMENTACION);
    }
}
