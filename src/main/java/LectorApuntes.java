import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class LectorApuntes {

    // Lee un archivo de texto y devuelve su contenido (o null si falla)
    public static String leer(String ruta) {
        try {
            return Files.readString(Path.of(ruta));
        } catch (IOException e) {
            System.out.println("No pude leer el archivo: " + ruta);
            System.out.println("Detalle: " + e.getMessage());
            return null;
        }
    }
}

