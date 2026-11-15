import java.text.Normalizer;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Quiz {

    private static final String EJEMPLOS = """
            EJEMPLO 1
            Apuntes: Un perro es un mamífero doméstico que ladra.
            Pregunta: ¿Qué es un perro?
            Respuesta del estudiante: Un animal que ladra y vive con las personas.
            Razonamiento: Los apuntes dicen que es un mamífero doméstico que ladra. La respuesta coincide en lo principal.
            VEREDICTO: CORRECTA

            EJEMPLO 2
            Apuntes: Un perro es un mamífero doméstico que ladra.
            Pregunta: ¿Qué es un perro?
            Respuesta del estudiante: Es un tipo de pez.
            Razonamiento: Los apuntes dicen que es un mamífero doméstico. La respuesta lo contradice.
            VEREDICTO: INCORRECTA

            EJEMPLO 3
            Apuntes: Un gato es un mamífero doméstico que maúlla y caza ratones.
            Pregunta: ¿Qué es un gato?
            Respuesta del estudiante: Un mamífero doméstico.
            Razonamiento: Los apuntes añaden que maúlla y caza ratones. La respuesta acierta solo una parte.
            VEREDICTO: PARCIAL
            """;

    private final ClienteOllama cliente;
    private final String apuntes;

    public Quiz(ClienteOllama cliente, String apuntes) {
        this.cliente = cliente;
        this.apuntes = apuntes;
    }

    // Genera texto a partir de los apuntes según una instrucción libre
    public String generarConInstruccion(String tarea) throws Exception {
        String instruccion = tarea.isBlank()
                ? "Genera 5 preguntas de práctica numeradas. No incluyas las respuestas."
                : tarea;

        String peticion = "Eres un tutor de ingeniería en informática. "
                + "Usa únicamente los siguientes apuntes como base. "
                + "Tarea: " + instruccion + " "
                + "Responde en español.\n\nAPUNTES:\n" + apuntes;

        return cliente.preguntar(peticion);
    }

    // Versión con 5 preguntas (la usa la consola)
    public List<String> generarPreguntas() throws Exception {
        return generarPreguntas(5);
    }

    // Pide 'cantidad' preguntas. Reintenta hasta 3 veces si salen menos.
    public List<String> generarPreguntas(int cantidad) throws Exception {
        List<String> preguntas = new ArrayList<>();

        for (int intento = 0; intento < 3 && preguntas.size() < cantidad; intento++) {
            int faltan = cantidad - preguntas.size();

            String peticion = "Eres un tutor de ingeniería en informática. "
                    + "Con base únicamente en los siguientes apuntes, genera exactamente " + faltan
                    + " preguntas cortas y distintas entre sí. "
                    + "Escribe cada pregunta en su propia línea, numeradas como 1. 2. 3. y así sucesivamente. "
                    + "No incluyas respuestas ni texto adicional. Responde en español.";

            if (!preguntas.isEmpty()) {
                peticion += "\nNo repitas estas preguntas:\n" + String.join("\n", preguntas);
            }
            peticion += "\n\nAPUNTES:\n" + apuntes;

            String texto = cliente.preguntar(peticion);
            if (texto == null) {
                break;
            }

            for (String pregunta : extraerPreguntas(texto)) {
                if (!preguntas.contains(pregunta) && preguntas.size() < cantidad) {
                    preguntas.add(pregunta);
                }
            }
        }
        return preguntas;
    }

    // Califica la respuesta. Si no responde nada, ni consulta al modelo.
    public String evaluarRespuesta(String pregunta, String respuesta) throws Exception {
        if (esRespuestaVacia(respuesta)) {
            return "El estudiante no dio una respuesta a la pregunta.\nVEREDICTO: INCORRECTA";
        }

        String peticion = "Eres un tutor de ingeniería en informática que califica respuestas. "
                + "Usa únicamente los APUNTES como fuente de verdad.\n\n"
                + "Para calificar escribe una línea que empiece con Razonamiento: y, en la última línea, "
                + "el veredicto, que debe ser exactamente VEREDICTO: CORRECTA, VEREDICTO: PARCIAL "
                + "o VEREDICTO: INCORRECTA.\n"
                + "Si la respuesta dice lo mismo que los apuntes con otras palabras, es CORRECTA. "
                + "Si solo acierta una parte, es PARCIAL. "
                + "Si contradice los apuntes, no tiene relación con ellos o dice que no sabe, es INCORRECTA.\n\n"
                + "Ejemplos de cómo calificar:\n" + EJEMPLOS + "\n"
                + "Ahora califica este caso.\n"
                + "APUNTES:\n" + apuntes + "\n\n"
                + "Pregunta: " + pregunta + "\n"
                + "Respuesta del estudiante: " + respuesta;

        return cliente.preguntar(peticion, 0.1);
    }

    // Detecta respuestas como "no sé", "ni idea" o texto vacío
    private static boolean esRespuestaVacia(String respuesta) {
        String r = Normalizer.normalize(respuesta, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase()
                .replaceAll("[^a-z0-9 ]", "")
                .trim();

        return r.isEmpty()
                || r.equals("no se")
                || r.equals("nose")
                || r.equals("no lo se")
                || r.equals("no la se")
                || r.equals("no se nada")
                || r.equals("ni idea")
                || r.equals("no tengo idea")
                || r.equals("no recuerdo")
                || r.equals("no entiendo");
    }

    // Convierte el veredicto del modelo en puntos
    public static double puntajeDe(String evaluacion) {
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

    // Versión de consola: usa los métodos de arriba
    public void jugar(Scanner sc) throws Exception {
        System.out.println("Preparando preguntas...");
        List<String> preguntas = generarPreguntas();

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
        // Separa por líneas y también por numeración dentro de una misma línea ("1. ... 2. ...")
        String[] trozos = texto.split("\\n|(?<=[?.])\\s+(?=\\d+[.)]\\s)");

        for (String trozo : trozos) {
            String limpia = trozo.trim();
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
}