package modelo;

import java.util.ArrayList;
import java.util.List;

public class Estado {

    public static final String AMBITO_BOLSIN = "Bolsin";
    public static final String AMBITO_REMITO = "Remito";
    public static final String AMBITO_DOCUMENTACION = "Documentacion";

    // "Persistencia" simulada: todo Estado creado queda registrado acá,
    // así el Gestor puede buscarlo por ámbito + condición (Polimorfismo/Experto).
    private static List<Estado> estados = new ArrayList<>();

    private String nombre;
    private String descripcion;
    private String ambito;

    public Estado(String nombre, String descripcion, String ambito) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.ambito = ambito;
        estados.add(this);
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public boolean esAmbitoBolsin() {
        return AMBITO_BOLSIN.equals(ambito);
    }

    public boolean esAmbitoRemito() {
        return AMBITO_REMITO.equals(ambito);
    }

    public boolean esAmbitoDocumentacion() {
        return AMBITO_DOCUMENTACION.equals(ambito);
    }

    public boolean esEnviado() {
        return "Enviado".equals(nombre);
    }

    public boolean esRecibidoEnCMDestino() {
        return "Recibido en CM Destino".equals(nombre);
    }

    public boolean esRecibidoyAceptado() {
        return "Recibido y Aceptado".equals(nombre);
    }

    public boolean esRecibidayAceptada() {
        return "Recibida y Aceptada".equals(nombre);
    }

    public boolean esRecibidayRechazada() {
        return "Recibida y Rechazada".equals(nombre);
    }

    public boolean esNoRecibida() {
        return "No Recibida".equals(nombre);
    }

    public boolean esRecibidoyAceptadoParcial() {
        return "Recibido y Aceptado Parcial".equals(nombre);
    }

    public boolean esParaRedirigir() {
        return "Para Redirigir".equals(nombre);
    }

    public static List<Estado> getEstados() {
        return estados;
    }
}
