import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {

    private static final String URL_OLLAMA = "http://localhost:11434/api/chat";
    private static final String MODELO = "llama3.2:1b";
    private static final String ARCHIVO_APUNTES = "apuntes/colas.txt";

    public static void main(String[] args) throws Exception {
        String apuntes = leerApuntes(ARCHIVO_APUNTES);
        if (apuntes == null) {
            return;
        }

        String instruccion = "Eres un tutor de ingeniería en informática. "
                + "Con base únicamente en los siguientes apuntes, genera 5 preguntas "
                + "de práctica numeradas. No incluyas las respuestas. "
                + "Responde en español.\n\nAPUNTES:\n" + apuntes;

        System.out.println("Generando preguntas...");
        String respuesta = preguntarAlModelo(instruccion);

        if (respuesta != null) {
            System.out.println("\n" + respuesta);
        }
    }

    // Lee el archivo de apuntes y devuelve su contenido
    private static String leerApuntes(String ruta) {
        try {
            return Files.readString(Path.of(ruta));
        } catch (IOException e) {
            System.out.println("No pude leer el archivo: " + ruta);
            System.out.println("Detalle: " + e.getMessage());
            return null;
        }
    }

    // Envía un texto al modelo local y devuelve su respuesta
    private static String preguntarAlModelo(String texto) throws Exception {
        JsonObject mensaje = new JsonObject();
        mensaje.addProperty("role", "user");
        mensaje.addProperty("content", texto);

        JsonArray mensajes = new JsonArray();
        mensajes.add(mensaje);

        JsonObject cuerpo = new JsonObject();
        cuerpo.addProperty("model", MODELO);
        cuerpo.add("messages", mensajes);
        cuerpo.addProperty("stream", false);

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(URL_OLLAMA))
                .header("content-type", "application/json")
                .timeout(Duration.ofMinutes(3))
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString()))
                .build();

        HttpResponse<String> respuesta;
        try {
            respuesta = HttpClient.newHttpClient()
                    .send(peticion, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("No pude conectar con Ollama. ¿Está abierta la app?");
            return null;
        }

        if (respuesta.statusCode() != 200) {
            System.out.println("Error " + respuesta.statusCode() + ": " + respuesta.body());
            return null;
        }

        JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
        return json.getAsJsonObject("message").get("content").getAsString();
    }
}