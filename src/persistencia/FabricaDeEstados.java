package persistencia;

import modelo.DeBaja;
import modelo.EnBolsinEnviado;
import modelo.EnBolsinSaliente;
import modelo.EnRemito;
import modelo.Estado;
import modelo.NoRecibida;
import modelo.ParaRedirigir;
import modelo.RecibidaYRechazada;
import modelo.RecibidayAceptada;
import modelo.Registrada;

// Arma el objeto que corresponde a una fila de la tabla estado. Los estados de
// Bolsin y Remito son Estado simples (sin patron State); los de Documentacion son
// la subclase concreta de EstadoDocumentacion que le toca por nombre. Devuelve
// null si un estado de documentacion de la base no tiene clase en el modelo.
final class FabricaDeEstados {

    private FabricaDeEstados() {
    }

    static Estado crear(String ambito, String nombre, String descripcion) {
        if (!Estado.AMBITO_DOCUMENTACION.equals(ambito)) {
            return new Estado(nombre, descripcion, ambito);
        }
        switch (nombre) {
            case "Registrada":
                return new Registrada();
            case "En Remito":
                return new EnRemito();
            case "En Bolsin Saliente":
                return new EnBolsinSaliente();
            case "En Bolsin Enviado":
                return new EnBolsinEnviado();
            case "Recibida y Aceptada":
                return new RecibidayAceptada();
            case "Recibida y Rechazada":
                return new RecibidaYRechazada();
            case "No Recibida":
                return new NoRecibida();
            case "Para Redirigir":
                return new ParaRedirigir();
            case "De Baja":
                return new DeBaja();
            default:
                return null;
        }
    }
}
