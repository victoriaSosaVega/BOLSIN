package boundary;

import control.GestorRecepcionDeBolsin;
import modelo.Bolsin;
import modelo.CM;
import modelo.Empleado;
import modelo.Remito;
import modelo.Sesion;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.List;

public class PantallaRecepcionDeBolsin extends JFrame {

    private static final String CARD_CM = "CM";
    private static final String CARD_SELECCION = "SELECCION";
    private static final String CARD_OPCIONES = "OPCIONES";
    private static final String CARD_FIN = "FIN";

    private GestorRecepcionDeBolsin gestor;
    private final CardLayout cardLayout = new CardLayout();
    private final JPanel panelContenedor = new JPanel(cardLayout);
    private final JLabel lblCMEmpleado = new JLabel(" ", JLabel.CENTER);
    private final JLabel lblCMEnListado = new JLabel(" ");
    private final DefaultListModel<String> modeloListaBolsines = new DefaultListModel<>();
    private final JList<String> listaBolsines = new JList<>(modeloListaBolsines);
    private List<Bolsin> bolsinesMostrados;
    private final JTextArea areaRemitos = new JTextArea();
    private final JTextArea areaFinal = new JTextArea();

    public PantallaRecepcionDeBolsin() {
        super("CU28 - Registrar Recepcion de Bolsin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 420);
        setLocationRelativeTo(null);
        panelContenedor.add(crearPanelCM(), CARD_CM);
        panelContenedor.add(crearPanelSeleccion(), CARD_SELECCION);
        panelContenedor.add(crearPanelOpciones(), CARD_OPCIONES);
        panelContenedor.add(crearPanelFin(), CARD_FIN);
        add(panelContenedor);
    }

    private JPanel crearPanelCM() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        lblCMEmpleado.setFont(lblCMEmpleado.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(lblCMEmpleado, BorderLayout.CENTER);
        return panel;
    }

    private JPanel crearPanelSeleccion() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(lblCMEnListado, BorderLayout.NORTH);
        panel.add(new JScrollPane(listaBolsines), BorderLayout.CENTER);
        JButton btnSeleccionar = new JButton("Seleccionar bolsin");
        btnSeleccionar.addActionListener(e -> {
            int idx = listaBolsines.getSelectedIndex();
            if (idx < 0 || bolsinesMostrados == null || bolsinesMostrados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un bolsin de la lista.");
                return;
            }
            tomarBolsinSeleccionado(bolsinesMostrados.get(idx));
        });
        panel.add(btnSeleccionar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        areaRemitos.setEditable(false);
        panel.add(new JScrollPane(areaRemitos), BorderLayout.CENTER);
        JButton btnContinuar = new JButton("Seleccionar recepción correcta");
        btnContinuar.addActionListener(e -> tomarSeleccionRecepcionBolsin());
        panel.add(btnContinuar, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelFin() {
        JPanel panel = new JPanel(new BorderLayout());
        areaFinal.setEditable(false);
        panel.add(new JScrollPane(areaFinal), BorderLayout.CENTER);
        JButton btnCerrar = new JButton("Cerrar");
        btnCerrar.addActionListener(e -> dispose());
        panel.add(btnCerrar, BorderLayout.SOUTH);
        return panel;
    }

    public void opcionRegRecepcionBolsin(Sesion sesionActiva) {
        habilitarPantalla();
        gestor = new GestorRecepcionDeBolsin(sesionActiva, this);
        gestor.iniciarCU28();
    }

    public void habilitarPantalla() {
        setVisible(true);
        cardLayout.show(panelContenedor, CARD_CM);
    }

    public void mostrarCMEmpleadoLogueado(Empleado logueado) {
        CM cm = logueado.getCM();
        String texto = "CM destino del empleado logueado: " + cm.getNombre();
        lblCMEmpleado.setText(texto);
        lblCMEnListado.setText(texto);
        cardLayout.show(panelContenedor, CARD_CM);
    }

    public void mostrarCMSeleccion(List<Bolsin> bolsines) {
        bolsinesMostrados = bolsines;
        modeloListaBolsines.clear();
        if (bolsines.isEmpty()) {
            modeloListaBolsines.addElement("(no hay bolsines en estado Enviado para su CM)");
        } else {
            for (Bolsin bolsin : bolsines) {
                modeloListaBolsines.addElement("Bolsin #" + bolsin.getNumeroBolsin()
                        + " | CM origen: " + bolsin.getCMOrigen().getNombre()
                        + " | Precinto: " + bolsin.getNumeroPrecinto());
            }
        }
        Timer timer = new Timer(900, e -> cardLayout.show(panelContenedor, CARD_SELECCION));
        timer.setRepeats(false);
        timer.start();
    }

    public void tomarBolsinSeleccionado(Bolsin seleccionado) {
        gestor.tomarBolsinSeleccionado(seleccionado);
    }

    public void mostrarOpcionesRecepcionBolsin(List<Remito> remitos) {
        StringBuilder texto = new StringBuilder("Remitos y documentacion del bolsin seleccionado:\n\n");
        for (Remito remito : remitos) {
            texto.append("Remito #").append(remito.getNumero()).append('\n');
            for (var detalle : remito.obtenerDatosDocumentacion()) {
                var documentacion = detalle.obtenerDatosDocumentacion();
                texto.append("     Asunto: ").append(documentacion.getAsunto()).append('\n');
                texto.append("     Tipo de documentacion: ")
                        .append(documentacion.getTipo().getNombre()).append("\n\n");
            }
        }
        areaRemitos.setText(texto.toString());
        cardLayout.show(panelContenedor, CARD_OPCIONES);
    }

    public void tomarSeleccionRecepcionBolsin() {
        gestor.tomarSeleccionRecepcionBolsin();
    }

    public void solicitarConfirmacion() {
        int resultado = JOptionPane.showConfirmDialog(this,
                "Confirma el registro de la recepcion?", "Confirmar", JOptionPane.YES_NO_OPTION);
        tomarConfirmacion(resultado == JOptionPane.YES_OPTION);
    }

    public void tomarConfirmacion(boolean confirma) {
        gestor.tomarConfirmacion(confirma);
    }

    public void finCU() {
        areaFinal.setText("Ejecucion exitosa del caso de uso.\nCU28 finalizado.");
        cardLayout.show(panelContenedor, CARD_FIN);
    }

    public void mostrarErrorGuardado(String detalle) {
        areaFinal.setText("No se pudo guardar la recepcion en la base de datos.\n"
                + "El CU28 no se completo y no se notifico a la CM origen.\n\n" + detalle);
        cardLayout.show(panelContenedor, CARD_FIN);
    }
}
