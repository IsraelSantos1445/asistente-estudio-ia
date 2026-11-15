import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingWorker;
import java.awt.BorderLayout;

public class PanelPreguntar extends JPanel {

    private final ClienteOllama cliente;
    private final JTextArea campoPregunta = new JTextArea(3, 40);
    private final JTextArea areaRespuesta = new JTextArea();
    private final JButton botonEnviar = new JButton("Enviar");
    private final JLabel estado = new JLabel(" ");

    public PanelPreguntar(ClienteOllama cliente) {
        this.cliente = cliente;
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        campoPregunta.setLineWrap(true);
        campoPregunta.setWrapStyleWord(true);

        areaRespuesta.setEditable(false);
        areaRespuesta.setLineWrap(true);
        areaRespuesta.setWrapStyleWord(true);

        JPanel arriba = new JPanel(new BorderLayout(8, 8));
        arriba.add(new JLabel("Escribe tu pregunta:"), BorderLayout.NORTH);
        arriba.add(new JScrollPane(campoPregunta), BorderLayout.CENTER);
        arriba.add(botonEnviar, BorderLayout.EAST);

        add(arriba, BorderLayout.NORTH);
        add(new JScrollPane(areaRespuesta), BorderLayout.CENTER);
        add(estado, BorderLayout.SOUTH);

        botonEnviar.addActionListener(e -> enviar());
    }

    private void enviar() {
        String pregunta = campoPregunta.getText().trim();
        if (pregunta.isEmpty()) {
            estado.setText("Escribe una pregunta primero.");
            return;
        }

        botonEnviar.setEnabled(false);
        estado.setText("Pensando...");
        areaRespuesta.setText("");

        SwingWorker<String, Void> trabajo = new SwingWorker<String, Void>() {
            @Override
            protected String doInBackground() throws Exception {
                return cliente.preguntar(pregunta);
            }

            @Override
            protected void done() {
                try {
                    String respuesta = get();
                    if (respuesta == null) {
                        estado.setText("No pude conectar con Ollama. ¿Está abierta la app?");
                    } else {
                        areaRespuesta.setText(respuesta);
                        areaRespuesta.setCaretPosition(0);
                        estado.setText("Listo.");
                    }
                } catch (Exception ex) {
                    estado.setText("Error: " + ex.getMessage());
                } finally {
                    botonEnviar.setEnabled(true);
                }
            }
        };
        trabajo.execute();
    }
}

