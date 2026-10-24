import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {

    private static final String URL_OLLAMA = "http://localhost:11434/api/chat";
    private static final String MODELO = "llama3.2:1b";

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        System.out.print("Escribe tu pregunta: ");
        String pregunta = sc.nextLine();

        // Armar el JSON que se envía
        JsonObject mensaje = new JsonObject();
        mensaje.addProperty("role", "user");
        mensaje.addProperty("content", pregunta);

        JsonArray mensajes = new JsonArray();
        mensajes.add(mensaje);

        JsonObject cuerpo = new JsonObject();
        cuerpo.addProperty("model", MODELO);
        cuerpo.add("messages", mensajes);
        cuerpo.addProperty("stream", false);

        // Armar la petición HTTP
        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(URL_OLLAMA))
                .header("content-type", "application/json")
                .timeout(Duration.ofMinutes(2))
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString()))
                .build();

        System.out.println("Pensando...");

        HttpResponse<String> respuesta;
        try {
            respuesta = HttpClient.newHttpClient()
                    .send(peticion, HttpResponse.BodyHandlers.ofString());
        } catch (IOException e) {
            System.out.println("No pude conectar con Ollama. ¿Está abierta la app?");
            System.out.println("Detalle: " + e.getMessage());
            return;
        }

        if (respuesta.statusCode() != 200) {
            System.out.println("Error " + respuesta.statusCode() + ": " + respuesta.body());
            return;
        }

        // Leer el texto de la respuesta
        JsonObject json = JsonParser.parseString(respuesta.body()).getAsJsonObject();
        String texto = json.getAsJsonObject("message")
                .get("content").getAsString();

        System.out.println("\n" + texto);
        sc.close();
    }
}