package persistencia;

// Error al leer o escribir en la base. Es unchecked para no ensuciar las firmas
// de las clases del diagrama de secuencia.
public class PersistenciaException extends RuntimeException {

    public PersistenciaException(String mensaje) {
        super(mensaje);
    }

    public PersistenciaException(String mensaje, Throwable causa) {
        super(mensaje, causa);
    }
}
