package app;

import boundary.PantallaPrincipalEncargadoBolsines;
import modelo.Sesion;
import persistencia.CargadorDeDatos;
import persistencia.PersistenciaException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MainCU28 {

    public static void main(String[] args) {
        // Los datos (estados, CMs, bolsines, remitos, documentacion y la sesion
        // abierta del Encargado de Bolsines) se leen de la base. Para cargar
        // datos de prueba ver db/seed_cu28.sql.
        Sesion sesion;
        try {
            sesion = CargadorDeDatos.cargar();
        } catch (PersistenciaException e) {
            System.err.println(e.getMessage());
            JOptionPane.showMessageDialog(null, e.getMessage(), "No se pudo iniciar", JOptionPane.ERROR_MESSAGE);
            return;
        }

        // --- Se abre el menu del Encargado de Bolsines; CU28 es la opcion 1 ---
        SwingUtilities.invokeLater(() -> new PantallaPrincipalEncargadoBolsines(sesion));
    }
}
