package boundary;

import control.GestorRecepcionDeBolsin;
import modelo.Bolsin;
import modelo.CM;
import modelo.Documentacion;
import modelo.Remito;
import modelo.Sesion;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.Timer;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.util.ArrayList;
import java.util.List;

// Widgets del diagrama de clases (lblCMEmpleado, btnBolsin, cmbOpciones,
// lblOpciones, btnConfirmar) mapeados a componentes Swing reales.
public class PantallaRecepcionDeBolsin extends JFrame {

    private static final String CARD_CM = "CM";
    private static final String CARD_SELECCION = "SELECCION";
    private static final String CARD_OPCIONES = "OPCIONES";
    private static final String CARD_SELECCION_DOCS = "SELECCION_DOCS";
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
    private final JRadioButton opcion1 = new JRadioButton("1. El contenido del bolsin es igual al registrado", true);
    private final JRadioButton opcion2 = new JRadioButton("2. No se recibe toda la documentacion asociada a los remitos");
    private final JRadioButton opcion3 = new JRadioButton("3. Existe documentacion que no corresponde al destino (CM del usuario logueado)");
    private final JRadioButton opcion4 = new JRadioButton("4. La documentacion se debe redirigir a otra area");

    private final JLabel lblInstruccionSeleccionDocs = new JLabel(" ");
    private final JPanel panelChecksDocs = new JPanel();
    private final List<JCheckBox> checkBoxesSeleccionDocs = new ArrayList<>();
    private List<Documentacion> documentosParaSeleccion;

    private final JTextArea areaFinal = new JTextArea();

    public PantallaRecepcionDeBolsin() {
        super("CU28 - Registrar Recepcion de Bolsin");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setSize(600, 420);
        setLocationRelativeTo(null);

        panelContenedor.add(crearPanelCM(), CARD_CM);
        panelContenedor.add(crearPanelSeleccion(), CARD_SELECCION);
        panelContenedor.add(crearPanelOpciones(), CARD_OPCIONES);
        panelContenedor.add(crearPanelSeleccionDocs(), CARD_SELECCION_DOCS);
        panelContenedor.add(crearPanelFin(), CARD_FIN);
        add(panelContenedor);
    }

    private JPanel crearPanelCM() {
        // Paso 2 del CU28: "Sistema busca y muestra la CM del usuario logueado".
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(40, 20, 40, 20));
        lblCMEmpleado.setFont(lblCMEmpleado.getFont().deriveFont(Font.BOLD, 16f));
        panel.add(lblCMEmpleado, BorderLayout.CENTER);
        JLabel lblBuscando = new JLabel("Buscando bolsines en estado Enviado para esa CM...", JLabel.CENTER);
        panel.add(lblBuscando, BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelSeleccion() {
        // Pasos 3-4 del CU28: "Sistema busca los bolsines en estado enviado..." y
        // "muestra de cada bolsin su CM origen y numero de precinto".
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(lblCMEnListado, BorderLayout.NORTH);
        panel.add(new JScrollPane(listaBolsines), BorderLayout.CENTER);

        JButton btnSeleccionar = new JButton("Seleccionar bolsin"); // btnBolsin
        btnSeleccionar.addActionListener(e -> {
            int idx = listaBolsines.getSelectedIndex();
            if (idx < 0 || bolsinesMostrados == null || bolsinesMostrados.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Seleccione un bolsin de la lista.");
                return;
            }
            tomarBolsinSeleccionado(bolsinesMostrados.get(idx));
        });
        panel.add(crearBarraBotones(btnSeleccionar), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelOpciones() {
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        areaRemitos.setEditable(false);
        panel.add(new JScrollPane(areaRemitos), BorderLayout.CENTER);

        JPanel panelOpciones = new JPanel(new GridLayout(4, 1)); // cmbOpciones/lblOpciones
        ButtonGroup grupo = new ButtonGroup();
        for (JRadioButton rb : new JRadioButton[]{opcion1, opcion2, opcion3, opcion4}) {
            grupo.add(rb);
            panelOpciones.add(rb);
        }
        panel.add(panelOpciones, BorderLayout.NORTH);

        JButton btnConfirmarOpcion = new JButton("Confirmar opcion"); // btnConfirmar
        btnConfirmarOpcion.addActionListener(e -> {
            int opcion = opcion1.isSelected() ? 1 : opcion2.isSelected() ? 2 : opcion3.isSelected() ? 3 : 4;
            tomarSeleccionRecepcionBolsin(opcion);
        });
        panel.add(crearBarraBotones(btnConfirmarOpcion), BorderLayout.SOUTH);
        return panel;
    }

    private JPanel crearPanelSeleccionDocs() {
        // A3/A4/A5 (opciones 2, 3 y 4): el EB marca a que documentacion le
        // aplica la situacion elegida. No estaba en la secuencia original de
        // CU28 (esa solo cubria el flujo principal, opcion 1).
        JPanel panel = new JPanel(new BorderLayout(8, 8));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        panel.add(lblInstruccionSeleccionDocs, BorderLayout.NORTH);

        panelChecksDocs.setLayout(new BoxLayout(panelChecksDocs, BoxLayout.Y_AXIS));
        panel.add(new JScrollPane(panelChecksDocs), BorderLayout.CENTER);

        JButton btnConfirmarSeleccion = new JButton("Confirmar seleccion");
        btnConfirmarSeleccion.addActionListener(e -> {
            List<Documentacion> seleccionadas = new ArrayList<>();
            for (int i = 0; i < checkBoxesSeleccionDocs.size(); i++) {
                if (checkBoxesSeleccionDocs.get(i).isSelected()) {
                    seleccionadas.add(documentosParaSeleccion.get(i));
                }
            }
            gestor.tomarDocumentacionSeleccionada(seleccionadas);
        });
        panel.add(crearBarraBotones(btnConfirmarSeleccion), BorderLayout.SOUTH);
        return panel;
    }

    // Observacion 1 del CU28: "El EB puede cancelar el caso de uso cuando lo
    // decida, seleccionando la opcion para tal fin". Se agrega ese boton en
    // cada pantalla intermedia junto a la accion principal.
    private JPanel crearBarraBotones(JButton botonPrincipal) {
        JButton btnCancelar = new JButton("Cancelar CU");
        btnCancelar.addActionListener(e -> cancelarCU());
        JPanel barra = new JPanel(new GridLayout(1, 2, 8, 0));
        barra.add(botonPrincipal);
        barra.add(btnCancelar);
        return barra;
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
        gestor.registrarRecepcionBolsin();
    }

    public void habilitarPantalla() {
        setVisible(true);
        cardLayout.show(panelContenedor, CARD_CM);
    }

    public void mostrarCMEmpleadoLogueado(CM cm) {
        // El CM destino de los bolsines a buscar es, por definicion, el CM del
        // empleado logueado (el que esta recibiendo el bolsin en su CM).
        String texto = "CM destino (CM del usuario logueado): " + cm.getNombre();
        lblCMEmpleado.setText(texto);
        lblCMEnListado.setText(texto);
        opcion3.setText("3. Existe documentacion que no corresponde al destino " + cm.getNombre());
        cardLayout.show(panelContenedor, CARD_CM);
    }

    public void mostrarCMSeleccion(List<Bolsin> bolsines) {
        this.bolsinesMostrados = bolsines;
        modeloListaBolsines.clear();
        if (bolsines.isEmpty()) {
            modeloListaBolsines.addElement("(no hay bolsines en estado Enviado para su CM)");
        } else {
            for (Bolsin b : bolsines) {
                modeloListaBolsines.addElement("Bolsin #" + b.getNumeroBolsin()
                        + " | CM origen: " + b.getCMOrigen().getNombre()
                        + " | Precinto: " + b.getNumeroPrecinto());
            }
        }
        // Se muestra primero la CM (paso 2) y recien despues, tras una breve
        // pausa, el listado de bolsines (pasos 3-4): son dos pasos distintos
        // del CU aunque el sistema los resuelva de forma automatica y seguida.
        Timer timer = new Timer(900, e -> cardLayout.show(panelContenedor, CARD_SELECCION));
        timer.setRepeats(false);
        timer.start();
    }

    public void tomarBolsinSeleccionado(Bolsin seleccionado) {
        gestor.tomarBolsinSeleccionado(seleccionado);
    }

    public void mostrarOpcionesRecepcionBolsin(List<Remito> remitos) {
        StringBuilder sb = new StringBuilder("Remitos y documentacion del bolsin seleccionado:\n\n");
        for (Remito r : remitos) {
            sb.append("Remito #").append(r.getNumero()).append("\n");
            for (var detalle : r.obtenerDatosDocumentacion()) {
                Documentacion doc = detalle.obtenerDatosDocumentacion();
                sb.append("     Asunto: ").append(doc.getAsunto()).append("\n");
                sb.append("     Tipo de documentacion: ").append(doc.getTipo().getNombre()).append("\n\n");
            }
        }
        areaRemitos.setText(sb.toString());
        cardLayout.show(panelContenedor, CARD_OPCIONES);
    }

    public void tomarSeleccionRecepcionBolsin(int opcion) {
        gestor.tomarSeleccionRecepcionBolsin(opcion);
    }

    public void mostrarDocumentacionParaSeleccion(int opcion, List<Documentacion> documentos) {
        lblInstruccionSeleccionDocs.setText(instruccionParaOpcion(opcion));
        this.documentosParaSeleccion = documentos;
        panelChecksDocs.removeAll();
        checkBoxesSeleccionDocs.clear();
        for (Documentacion doc : documentos) {
            JCheckBox checkBox = new JCheckBox(doc.getAsunto() + " (" + doc.getTipo().getNombre() + ")");
            checkBoxesSeleccionDocs.add(checkBox);
            panelChecksDocs.add(checkBox);
        }
        panelChecksDocs.revalidate();
        panelChecksDocs.repaint();
        cardLayout.show(panelContenedor, CARD_SELECCION_DOCS);
    }

    private String instruccionParaOpcion(int opcion) {
        return switch (opcion) {
            case 2 -> "Tilde la documentacion que NO llego (no recibida):";
            case 3 -> "Tilde la documentacion que NO corresponde a esta CM destino:";
            case 4 -> "Tilde la documentacion que se debe redirigir a otra area:";
            default -> "Tilde la documentacion correspondiente:";
        };
    }

    public void solicitarConfirmacion() {
        int resultado = JOptionPane.showConfirmDialog(this,
                "Confirma el registro de la recepcion?",
                "Confirmar",
                JOptionPane.YES_NO_OPTION);
        tomarConfirmacion(resultado == JOptionPane.YES_OPTION);
    }

    public void tomarConfirmacion(boolean confirma) {
        gestor.tomarConfirmacion(confirma);
    }

    public void finCU() {
        areaFinal.setText("Ejecucion exitosa del caso de uso.\nCU28 finalizado.");
        cardLayout.show(panelContenedor, CARD_FIN);
    }

    public void cancelarCU() {
        gestor.cancelarCU();
    }

    public void mostrarCancelacion() {
        areaFinal.setText("CU28 cancelado por el Encargado de Bolsines.");
        cardLayout.show(panelContenedor, CARD_FIN);
    }
}
