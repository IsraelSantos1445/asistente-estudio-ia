import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JTabbedPane;
import javax.swing.SwingConstants;

public class VentanaPrincipal extends JFrame {

    public VentanaPrincipal(ClienteOllama cliente) {
        super("Asistente de estudio con IA local");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 560);
        setLocationRelativeTo(null);

        JTabbedPane pestanas = new JTabbedPane();
        pestanas.addTab("Preguntar", new PanelPreguntar(cliente));
        pestanas.addTab("Generar preguntas", new JLabel("Próximamente", SwingConstants.CENTER));
        pestanas.addTab("Quiz", new JLabel("Próximamente", SwingConstants.CENTER));

        add(pestanas);
    }
}