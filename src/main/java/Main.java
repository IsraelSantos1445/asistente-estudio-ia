import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Main {
    private static final String ARCHIVO_POR_DEFECTO = "apuntes/colas.txt";
    private static final ClienteOllama cliente = new ClienteOllama("llama3.2:1b");

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
                    modoQuiz(sc);
                    break;
                case "4":
                    salir = true;
                    System.out.println("Hasta luego.");
                    break;
                default:
                    System.out.println("Opción no válida. Escribe 1, 2, 3 o 4.");
            }
        }
    }

    private static void mostrarMenu() {
        System.out.println("\n¿Qué quieres hacer?");
        System.out.println("1. Hacer una pregunta");
        System.out.println("2. Analizar un archivo de apuntes y generar preguntas de práctica");
        System.out.println("3. Modo quiz: responder preguntas y recibir evaluación");
        System.out.println("4. Salir");
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
        String respuesta = cliente.preguntar(pregunta);

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

        String apuntes = LectorApuntes.leer(ruta);
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
        String respuesta = cliente.preguntar(instruccion);

        if (respuesta != null) {
            System.out.println("\n" + respuesta);
        }
    }
        // Opción 3: quiz con evaluación de respuestas
    private static void modoQuiz(Scanner sc) throws Exception {
        System.out.print("Ruta del archivo (Enter para usar " + ARCHIVO_POR_DEFECTO + "): ");
        String ruta = sc.nextLine().trim();
        if (ruta.isEmpty()) {
            ruta = ARCHIVO_POR_DEFECTO;
        }

        String apuntes = LectorApuntes.leer(ruta);
        if (apuntes == null) {
            return;
        }

        System.out.println("Preparando preguntas...");
        String peticion = "Eres un tutor de ingeniería en informática. "
                + "Con base únicamente en los siguientes apuntes, genera exactamente 3 preguntas cortas. "
                + "Escribe una pregunta por línea, numeradas como 1. 2. 3."
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

            String evaluacion = evaluarRespuesta(apuntes, preguntas.get(i), respuestaUsuario);
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
    private static List<String> extraerPreguntas(String texto) {
        List<String> preguntas = new ArrayList<>();
        for (String linea : texto.split("\n")) {
            String limpia = linea.trim();
            if (limpia.contains("?")) {
                // Quita numeración y marcas de formato al inicio: "1.", "2)", "**", "-", "Pregunta 3:"
                limpia = limpia.replaceFirst("^[\\s*#\\-]*(Pregunta\\s*)?\\d*\\s*[.):]*\\s*", "");
                limpia = limpia.replace("**", "").trim();
                if (limpia.length() > 10) {
                    preguntas.add(limpia);
                }
            }
        }
        return preguntas;
    }

    private static String evaluarRespuesta(String apuntes, String pregunta, String respuesta) throws Exception {
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
    private static double puntajeDe(String evaluacion) {
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