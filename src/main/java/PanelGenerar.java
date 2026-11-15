import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.io.File;

public class PanelGenerar extends JPanel {

    private final ClienteOllama cliente;
    private final JTextField campoArchivo = new JTextField("apuntes/colas.txt");
    private final JButton botonElegir = new JButton("Elegir archivo");
    private final JTextArea campoInstruccion = new JTextArea(2, 40);
    private final JButton botonGenerar = new JButton("Generar");
    private final JTextArea areaResultado = new JTextArea();
    private final JLabel estado = new JLabel(" ");

    public PanelGenerar(ClienteOllama cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        campoInstruccion.setLineWrap(true);
        campoInstruccion.setWrapStyleWord(true);

        areaResultado.setEditable(false);
        areaResultado.setLineWrap(true);
        areaResultado.setWrapStyleWord(true);

        JPanel filaArchivo = new JPanel(new BorderLayout(8, 0));
        filaArchivo.add(new JLabel("Apuntes:"), BorderLayout.WEST);
        filaArchivo.add(campoArchivo, BorderLayout.CENTER);
        filaArchivo.add(botonElegir, BorderLayout.EAST);

        JPanel filaInstruccion = new JPanel(new BorderLayout(8, 4));
        filaInstruccion.add(
                new JLabel("Instrucción (opcional; vacío genera 3 preguntas):"),
                BorderLayout.NORTH);
        filaInstruccion.add(new JScrollPane(campoInstruccion), BorderLayout.CENTER);
        filaInstruccion.add(botonGenerar, BorderLayout.EAST);

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.add(filaArchivo, BorderLayout.NORTH);
        arriba.add(filaInstruccion, BorderLayout.CENTER);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(areaResultado), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        botonElegir.addActionListener(e -> elegirArchivo());
        botonGenerar.addActionListener(e -> generar());
    }

    private void elegirArchivo() {
        JFileChooser selector = new JFileChooser(new File("apuntes"));
        selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto (.txt)", "txt"));

        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campoArchivo.setText(selector.getSelectedFile().getPath());
        }
    }

    private void generar() {
        String ruta = campoArchivo.getText().trim();
        String apuntes = LectorApuntes.leer(ruta);
        if (apuntes == null) {
            estado.setText("No pude leer el archivo: " + ruta);
            return;
        }

        String instruccion = campoInstruccion.getText().trim();

        botonGenerar.setEnabled(false);
        estado.setText("Generando...");
        areaResultado.setText("");

        SwingWorker<String, Void> trabajo = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                Quiz quiz = new Quiz(cliente, apuntes);
                return quiz.generarConInstruccion(instruccion);
            }

            @Override
            protected void done() {
                try {
                    String resultado = get();
                    if (resultado == null) {
                        estado.setText("No pude conectar con Ollama. ¿Está abierta la app?");
                    } else {
                        areaResultado.setText(resultado);
                        areaResultado.setCaretPosition(0);
                        estado.setText("Listo.");
                    }
                } catch (Exception ex) {
                    estado.setText("Error: " + ex.getMessage());
                } finally {
                    botonGenerar.setEnabled(true);
                }
            }
        };
        trabajo.execute();
    }
}