package persistencia;

import modelo.DeBaja;
import modelo.EnBolsinEnviadoDocumentacion;
import modelo.EnBolsinEnviadoRemito;
import modelo.EnBolsinSaliente;
import modelo.EnRemito;
import modelo.Enviado;
import modelo.Estado;
import modelo.NoRecibida;
import modelo.ParaRedirigir;
import modelo.RecibidaYRechazada;
import modelo.RecibidayAceptada;
import modelo.RecibidoEnCMDestino;
import modelo.RecibidoyAceptado;
import modelo.Registrada;

// Arma el Estado concreto que corresponde a una fila de la tabla estado (por
// ambito y nombre). Devuelve null si el estado de la base no tiene clase en el
// modelo (por ejemplo "Recibido y Aceptado Parcial"): mientras nadie lo use no molesta.
final class FabricaDeEstados {

    private FabricaDeEstados() {
    }

    static Estado crear(String ambito, String nombre) {
        switch (nombre) {
            case "Enviado":
                return ambito.equals(Estado.AMBITO_BOLSIN) ? new Enviado() : null;
            case "Recibido en CM Destino":
                return ambito.equals(Estado.AMBITO_BOLSIN) ? new RecibidoEnCMDestino() : null;
            case "Recibido y Aceptado":
                return ambito.equals(Estado.AMBITO_REMITO) ? new RecibidoyAceptado() : null;
            case "Recibida y Aceptada":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new RecibidayAceptada() : null;
            case "Recibida y Rechazada":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new RecibidaYRechazada() : null;
            case "No Recibida":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new NoRecibida() : null;
            case "Para Redirigir":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new ParaRedirigir() : null;
            case "Registrada":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new Registrada() : null;
            case "En Remito":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new EnRemito() : null;
            case "En Bolsin Saliente":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new EnBolsinSaliente() : null;
            case "En Bolsin Enviado":
                // Son dos estados distintos, uno por ambito.
                if (ambito.equals(Estado.AMBITO_REMITO)) {
                    return new EnBolsinEnviadoRemito();
                }
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new EnBolsinEnviadoDocumentacion() : null;
            case "De Baja":
                return ambito.equals(Estado.AMBITO_DOCUMENTACION) ? new DeBaja() : null;
            default:
                return null;
        }
    }
}
