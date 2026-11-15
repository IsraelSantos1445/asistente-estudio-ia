import javax.swing.JFrame;
import javax.swing.JTabbedPane;


public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(ClienteOllama cliente) {
        super("Asistente de estudio con IA local");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 560);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Preguntar", new PanelPreguntar(cliente));
        pestanas.addTab("Generar preguntas", new PanelGenerar(cliente));
                pestanas.addTab("Quiz", new PanelQuiz(cliente));

        add(pestanas);
    }
}