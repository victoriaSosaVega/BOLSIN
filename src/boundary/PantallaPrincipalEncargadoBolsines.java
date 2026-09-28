package boundary;

import modelo.Sesion;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.GridLayout;

// Menu del Encargado de Bolsines (ERS 3.2 Vista de Casos de Uso Esencial):
// CU19 Generar Bolsin, CU27 Registrar el retiro, CU28 Registrar recepcion,
// CU36 Consultar seguimiento. Solo CU28 esta implementado en este demo;
// las demas opciones quedan deshabilitadas como puntos de extension.
public class PantallaPrincipalEncargadoBolsines extends JFrame {

    public PantallaPrincipalEncargadoBolsines(Sesion sesionActiva) {
        super("Menu - Encargado de Bolsines");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(420, 260);
        setLocationRelativeTo(null);

        JPanel panel = new JPanel(new GridLayout(5, 1, 8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        panel.add(new JLabel("Seleccione una opcion:"));

        JButton btnRecepcion = new JButton("1. Registrar recepcion de bolsin");
        btnRecepcion.addActionListener(e -> {
            dispose();
            PantallaRecepcionDeBolsin pantalla = new PantallaRecepcionDeBolsin();
            pantalla.opcionRegRecepcionBolsin(sesionActiva);
        });
        panel.add(btnRecepcion);

        JButton btnGenerar = new JButton("2. Generar bolsin (no implementado)");
        btnGenerar.setEnabled(false);
        panel.add(btnGenerar);

        JButton btnRetiro = new JButton("3. Registrar el retiro de bolsines (no implementado)");
        btnRetiro.setEnabled(false);
        panel.add(btnRetiro);

        JButton btnSeguimiento = new JButton("4. Consultar seguimiento de bolsines (no implementado)");
        btnSeguimiento.setEnabled(false);
        panel.add(btnSeguimiento);

        add(panel);
        setVisible(true);
    }
}
