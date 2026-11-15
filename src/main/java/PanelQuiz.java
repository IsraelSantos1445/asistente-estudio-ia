import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingWorker;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;

public class PanelQuiz extends JPanel {

    private final ClienteOllama cliente;

    private final JTextField campoArchivo = new JTextField("apuntes/colas.txt");
    private final JButton botonElegir = new JButton("Elegir archivo");
    private final JButton botonIniciar = new JButton("Iniciar quiz");
    private final JSpinner selectorCantidad = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));

    private final JLabel etiquetaProgreso = new JLabel("Elige un archivo y pulsa Iniciar quiz.");
    private final JProgressBar barra = new JProgressBar(0, 1);
    private final JTextArea areaPregunta = crearAreaTexto(false);
    private final JTextArea campoRespuesta = crearAreaTexto(true);
    private final JTextArea areaEvaluacion = crearAreaTexto(false);

    private final JButton botonEnviar = new JButton("Enviar respuesta");
    private final JButton botonSiguiente = new JButton("Siguiente pregunta");
    private final JLabel estado = new JLabel(" ");

    private Quiz quiz;
    private List<String> preguntas = new ArrayList<>();
    private int indice;
    private int respondidas;
    private double puntos;

    public PanelQuiz(ClienteOllama cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        JPanel botonesArchivo = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botonesArchivo.add(botonElegir);
        botonesArchivo.add(new JLabel("Preguntas:"));
        botonesArchivo.add(selectorCantidad);
        botonesArchivo.add(botonIniciar);

        JPanel filaArchivo = new JPanel(new BorderLayout(8, 0));
        filaArchivo.add(new JLabel("Apuntes:"), BorderLayout.WEST);
        filaArchivo.add(campoArchivo, BorderLayout.CENTER);
        filaArchivo.add(botonesArchivo, BorderLayout.EAST);

        JPanel progreso = new JPanel(new BorderLayout(0, 4));
        progreso.add(etiquetaProgreso, BorderLayout.NORTH);
        progreso.add(barra, BorderLayout.CENTER);

        JPanel arriba = new JPanel(new BorderLayout(0, 10));
        arriba.add(filaArchivo, BorderLayout.NORTH);
        arriba.add(progreso, BorderLayout.CENTER);

        JPanel centro = new JPanel(new GridLayout(3, 1, 0, 8));
        centro.add(conTitulo(areaPregunta, "Pregunta"));
        centro.add(conTitulo(campoRespuesta, "Tu respuesta"));
        centro.add(conTitulo(areaEvaluacion, "Evaluación"));

        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
        botones.add(botonEnviar);
        botones.add(botonSiguiente);

        JPanel abajo = new JPanel(new BorderLayout(8, 0));
        abajo.add(estado, BorderLayout.CENTER);
        abajo.add(botones, BorderLayout.EAST);

        add(arriba, BorderLayout.NORTH);
        add(centro, BorderLayout.CENTER);
        add(abajo, BorderLayout.SOUTH);

        botonEnviar.setEnabled(false);
        botonSiguiente.setEnabled(false);

        botonElegir.addActionListener(e -> elegirArchivo());
        botonIniciar.addActionListener(e -> iniciar());
        botonEnviar.addActionListener(e -> enviar());
        botonSiguiente.addActionListener(e -> siguiente());
    }

    private static JTextArea crearAreaTexto(boolean editable) {
        JTextArea area = new JTextArea();
        area.setEditable(editable);
        area.setLineWrap(true);
        area.setWrapStyleWord(true);
        return area;
    }

    private static JScrollPane conTitulo(JTextArea area, String titulo) {
        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createTitledBorder(titulo));
        return scroll;
    }

    private void elegirArchivo() {
        JFileChooser selector = new JFileChooser(new File("apuntes"));
        selector.setFileFilter(new FileNameExtensionFilter("Archivos de texto (.txt)", "txt"));

        if (selector.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            campoArchivo.setText(selector.getSelectedFile().getPath());
        }
    }

    // Lee los apuntes y pide las preguntas al modelo
    private void iniciar() {
        String ruta = campoArchivo.getText().trim();
        String apuntes = LectorApuntes.leer(ruta);
        if (apuntes == null) {
            estado.setText("No pude leer el archivo: " + ruta);
            return;
        }

        quiz = new Quiz(cliente, apuntes);
        final int cantidad = (Integer) selectorCantidad.getValue();
        botonIniciar.setEnabled(false);
        botonEnviar.setEnabled(false);
        botonSiguiente.setEnabled(false);
        areaPregunta.setText("");
        campoRespuesta.setText("");
        areaEvaluacion.setText("");
        estado.setText("Preparando preguntas...");

        SwingWorker<List<String>, Void> trabajo = new SwingWorker<List<String>, Void>() {
            @Override
            protected List<String> doInBackground() throws Exception {
                return quiz.generarPreguntas(cantidad);
            }

            @Override
            protected void done() {
                try {
                    preguntas = get();
                    if (preguntas.isEmpty()) {
                        etiquetaProgreso.setText("Elige un archivo y pulsa Iniciar quiz.");
                        estado.setText("No pude obtener preguntas. ¿Está abierta la app de Ollama? Intenta de nuevo.");
                    } else {
                        indice = 0;
                        respondidas = 0;
                        puntos = 0;
                        barra.setMaximum(preguntas.size());
                        mostrarPregunta();
                    }
                } catch (Exception ex) {
                    estado.setText("Error: " + ex.getMessage());
                } finally {
                    botonIniciar.setEnabled(true);
                }
            }
        };
        trabajo.execute();
    }

    // Muestra la pregunta actual y deja todo listo para responder
    private void mostrarPregunta() {
        areaPregunta.setText(preguntas.get(indice));
        campoRespuesta.setText("");
        campoRespuesta.setEditable(true);
        areaEvaluacion.setText("");
        botonEnviar.setEnabled(true);
        botonSiguiente.setText("Siguiente pregunta");
        botonSiguiente.setEnabled(false);
        actualizarProgreso();
        estado.setText(" ");
        campoRespuesta.requestFocusInWindow();
    }

    private void actualizarProgreso() {
        etiquetaProgreso.setText("Pregunta " + (indice + 1) + " de " + preguntas.size()
                + "   |   Puntaje: " + puntos + " de " + respondidas);
        barra.setValue(indice);
    }

    // Envía la respuesta al modelo para que la evalúe
    private void enviar() {
        String respuesta = campoRespuesta.getText().trim();
        if (respuesta.isEmpty()) {
            estado.setText("Escribe una respuesta primero.");
            return;
        }

        String pregunta = preguntas.get(indice);
        botonEnviar.setEnabled(false);
        campoRespuesta.setEditable(false);
        estado.setText("Evaluando...");

        SwingWorker<String, Void> trabajo = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return quiz.evaluarRespuesta(pregunta, respuesta);
            }

            @Override
            protected void done() {
                try {
                    String evaluacion = get();
                    if (evaluacion == null) {
                        estado.setText("No pude evaluar la respuesta. Revisa que Ollama esté abierto e inténtalo de nuevo.");
                        botonEnviar.setEnabled(true);
                        campoRespuesta.setEditable(true);
                        return;
                    }

                    areaEvaluacion.setText(evaluacion);
                    areaEvaluacion.setCaretPosition(0);
                    respondidas++;
                    puntos += Quiz.puntajeDe(evaluacion);
                    actualizarProgreso();

                    boolean esUltima = indice == preguntas.size() - 1;
                    botonSiguiente.setText(esUltima ? "Ver resultado" : "Siguiente pregunta");
                    botonSiguiente.setEnabled(true);
                    estado.setText("Listo.");
                } catch (Exception ex) {
                    estado.setText("Error: " + ex.getMessage());
                    botonEnviar.setEnabled(true);
                    campoRespuesta.setEditable(true);
                }
            }
        };
        trabajo.execute();
    }

    private void siguiente() {
        indice++;
        if (indice >= preguntas.size()) {
            terminar();
        } else {
            mostrarPregunta();
        }
    }

    private void terminar() {
        botonEnviar.setEnabled(false);
        botonSiguiente.setEnabled(false);
        campoRespuesta.setEditable(false);
        barra.setValue(preguntas.size());
        etiquetaProgreso.setText("Quiz terminado");
        areaPregunta.setText("Quiz terminado.");
        campoRespuesta.setText("");
        areaEvaluacion.setText("Preguntas respondidas: " + respondidas
                + "\nPuntaje: " + puntos + " de " + respondidas
                + "\n\nPulsa Iniciar quiz para intentarlo de nuevo.");
        estado.setText(" ");
    }
}