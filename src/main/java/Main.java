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

        Quiz quiz = new Quiz(cliente, apuntes);
        quiz.jugar(sc);
    }
}