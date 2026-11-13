import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Quiz {

    private final ClienteOllama cliente;
    private final String apuntes;

    public Quiz(ClienteOllama cliente, String apuntes) {
        this.cliente = cliente;
        this.apuntes = apuntes;
    }

    // Hace todo el quiz: genera preguntas, las plantea y da el resultado
    public void jugar(Scanner sc) throws Exception {
        System.out.println("Preparando preguntas...");
        String peticion = "Eres un tutor de ingeniería en informática. "
                + "Con base únicamente en los siguientes apuntes, genera exactamente 5 preguntas cortas. "
                + "Escribe una pregunta por línea, numeradas como 1. 2. 3. 4. 5. "
                + "No incluyas respuestas ni texto adicional. Responde en español.\n\nAPUNTES:\n" + apuntes;

        String texto = cliente.preguntar(peticion);
        if (texto == null) {
            return;
        }

        List<String> preguntas = extraerPreguntas(texto);
        if (preguntas.isEmpty()) {
            System.out.println("No pude obtener preguntas del modelo. Intenta de nuevo.");
            return;
        }

        double puntos = 0;
        int respondidas = 0;

        for (int i = 0; i < preguntas.size(); i++) {
            System.out.println("\nPregunta " + (i + 1) + " de " + preguntas.size());
            System.out.println(preguntas.get(i));
            System.out.print("Tu respuesta (escribe 'salir' para terminar): ");
            String respuestaUsuario = sc.nextLine().trim();

            if (respuestaUsuario.equalsIgnoreCase("salir")) {
                break;
            }

            String evaluacion = evaluarRespuesta(preguntas.get(i), respuestaUsuario);
            if (evaluacion == null) {
                continue;
            }

            respondidas++;
            System.out.println("\n" + evaluacion);
            puntos += puntajeDe(evaluacion);
        }

        System.out.println("\n=== Resultado ===");
        System.out.println("Preguntas respondidas: " + respondidas);
        System.out.println("Puntaje: " + puntos + " de " + respondidas);
    }

    // Separa el texto del modelo en una lista de preguntas
    private List<String> extraerPreguntas(String texto) {
        List<String> preguntas = new ArrayList<>();
        for (String linea : texto.split("\n")) {
            String limpia = linea.trim();
            if (limpia.contains("?")) {
                limpia = limpia.replaceFirst("^[\\s*#\\-]*(Pregunta\\s*)?\\d*\\s*[.):]*\\s*", "");
                limpia = limpia.replace("**", "").trim();
                if (limpia.length() > 10) {
                    preguntas.add(limpia);
                }
            }
        }
        return preguntas;
    }

    // Pide al modelo que razone primero y dé el veredicto al final
    private String evaluarRespuesta(String pregunta, String respuesta) throws Exception {
        String peticion = "Eres un tutor de ingeniería en informática que califica respuestas. "
                + "Usa únicamente los APUNTES como fuente de verdad.\n\n"
                + "Sigue estos pasos:\n"
                + "1. En una línea, escribe qué dicen los apuntes sobre la pregunta.\n"
                + "2. En una línea, escribe si la respuesta del estudiante coincide con los apuntes.\n"
                + "3. En la última línea escribe exactamente una de estas tres opciones: "
                + "VEREDICTO: CORRECTA, VEREDICTO: PARCIAL o VEREDICTO: INCORRECTA.\n\n"
                + "Reglas: si la respuesta dice lo mismo que los apuntes con otras palabras, es CORRECTA. "
                + "Si solo acierta una parte, es PARCIAL. Si dice que no sabe o contradice los apuntes, es INCORRECTA.\n\n"
                + "APUNTES:\n" + apuntes + "\n\n"
                + "PREGUNTA: " + pregunta + "\n"
                + "RESPUESTA DEL ESTUDIANTE: " + respuesta;

        return cliente.preguntar(peticion, 0.1);
    }

    // Lee el veredicto de la última parte de la evaluación
    private double puntajeDe(String evaluacion) {
        String texto = evaluacion.toUpperCase();
        int pos = texto.lastIndexOf("VEREDICTO");
        if (pos == -1) {
            return 0;
        }
        String veredicto = texto.substring(pos);
        if (veredicto.contains("INCORRECTA")) {
            return 0;
        } else if (veredicto.contains("PARCIAL")) {
            return 0.5;
        } else if (veredicto.contains("CORRECTA")) {
            return 1;
        }
        return 0;
    }
}