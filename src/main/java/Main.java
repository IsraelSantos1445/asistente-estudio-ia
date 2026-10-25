import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.Scanner;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class Main {

    private static final String URL_OLLAMA = "http://localhost:11434/api/chat";
    private static final String MODELO = "llama3.2:1b";
    private static final String ARCHIVO_POR_DEFECTO = "apuntes/colas.txt";

    public static void main(String[] args) throws Exception {
        Scanner sc = new Scanner(System.in);
        boolean salir = false;

        System.out.println("=== Asistente de estudio con IA local ===");

        while (!salir) {
            mostrarMenu();
            String opcion = sc.nextLine().trim();

            switch (opcion) {
                case "1":
                    hacerPregunta(sc);
                    break;
                case "2":
                    generarPreguntasDeApuntes(sc);
                    break;
                case "3":
                    salir = true;
                    System.out.println("Hasta luego.");
                    break;
                default:
                    System.out.println("Opción no válida. Escribe 1, 2 o 3.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n¿Qué quieres hacer?");
        System.out.println("1. Hacer una pregunta");
        System.out.println("2. Analizar un archivo de apuntes y generar preguntas de práctica");
        System.out.println("3. Salir");
        System.out.print("Elige una opción: ");
    }

    // Opción 1: pregunta libre
    private static void hacerPregunta(Scanner sc) throws Exception {
        System.out.print("Escribe tu pregunta: ");
        String pregunta = sc.nextLine().trim();

        if (pregunta.isEmpty()) {
            System.out.println("No escribiste ninguna pregunta.");
            return;
        }

        System.out.println("Pensando...");
        String respuesta = preguntarAlModelo(pregunta);

        if (respuesta != null) {
            System.out.println("\n" + respuesta);
        }
    }
        // Opción 2: leer apuntes y generar preguntas con instrucción opcional
    private static void generarPreguntasDeApuntes(Scanner sc) throws Exception {
        System.out.print("Ruta del archivo (Enter para usar " + ARCHIVO_POR_DEFECTO + "): ");
        String ruta = sc.nextLine().trim();

        if (ruta.isEmpty()) {
            ruta = ARCHIVO_POR_DEFECTO;
        }

        String apuntes = leerApuntes(ruta);
        if (apuntes == null) {
            return;
        }

        System.out.println("\nPuedes indicar una instrucción específica:");
        System.out.print("Tu instrucción (Enter para usar la predeterminada): ");
        String personalizada = sc.nextLine().trim();

        String tarea;
        if (personalizada.isEmpty()) {
            tarea = "Genera 5 preguntas de práctica numeradas. No incluyas las respuestas.";
        } else {
            tarea = personalizada;
        }

        String instruccion = "Eres un tutor de ingeniería en informática. "
                + "Usa únicamente los siguientes apuntes como base. "
                + "Tarea: " + tarea + " "
                + "Responde en español.\n\nAPUNTES:\n" + apuntes;

        System.out.println("Generando...");
        String respuesta = preguntarAlModelo(instruccion);

        if (respuesta != null) {
            System.out.println("\n" + respuesta);
        }
    }
    // Lee un archivo de texto y devuelve su contenido
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