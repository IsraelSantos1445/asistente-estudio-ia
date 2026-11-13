import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ClienteOllama {

    private static final String URL_OLLAMA = "http://localhost:11434/api/chat";

    private final String modelo;
    private final HttpClient http;

    public ClienteOllama(String modelo) {
        this.modelo = modelo;
        this.http = HttpClient.newHttpClient();
    }

    public String preguntar(String texto) throws Exception {
        return preguntar(texto, 0.7);
    }

    public String preguntar(String texto, double temperatura) throws Exception {
        JsonObject mensaje = new JsonObject();
        mensaje.addProperty("role", "user");
        mensaje.addProperty("content", texto);

        JsonArray mensajes = new JsonArray();
        mensajes.add(mensaje);

        JsonObject opciones = new JsonObject();
        opciones.addProperty("temperature", temperatura);
        opciones.addProperty("num_predict", 600);

        JsonObject cuerpo = new JsonObject();
        cuerpo.addProperty("model", modelo);
        cuerpo.add("messages", mensajes);
        cuerpo.addProperty("stream", false);
        cuerpo.addProperty("keep_alive", "30m");
        cuerpo.add("options", opciones);

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(URL_OLLAMA))
                .header("content-type", "application/json")
                .timeout(Duration.ofMinutes(3))
                .POST(HttpRequest.BodyPublishers.ofString(cuerpo.toString()))
                .build();

        HttpResponse<String> respuesta;
        try {
            respuesta = http.send(peticion, HttpResponse.BodyHandlers.ofString());
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