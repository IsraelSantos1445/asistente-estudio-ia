import javax.swing.SwingUtilities;

public class MainGUI {

    public static void main(String[] args) {
        ClienteOllama cliente = new ClienteOllama("llama3.2:1b");
        SwingUtilities.invokeLater(() -> new VentanaPrincipal(cliente).setVisible(true));
    }
}