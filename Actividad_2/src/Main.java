
import java.util.Scanner;

public class Main {
    private static final Scanner INPUT = new Scanner(System.in);
    private static final Stack ACTION_HISTORY = new Stack();
    private static final Queue PENDING_TASKS = new Queue();

    public static void main(String[] args) {
        int option;

        do {
            showMenu();
            option = readOption();
            System.out.println();
            executeOption(option);

            if (option != 0) {
                pause();
            }
        } while (option != 0);

        INPUT.close();
    }

    private static void showMenu() {
        System.out.println("\n===== CENTRO DE OPERACIONES =====");
        System.out.println("1. Registrar accion");
        System.out.println("2. Deshacer ultima accion");
        System.out.println("3. Ver ultima accion");
        System.out.println("4. Mostrar historial");
        System.out.println("5. Agregar tarea");
        System.out.println("6. Procesar siguiente tarea");
        System.out.println("7. Ver siguiente tarea");
        System.out.println("8. Mostrar tareas pendientes");
        System.out.println("9. Mostrar estado del sistema");
        System.out.println("0. Salir");
        System.out.print("Seleccione una opcion: ");
    }

    private static int readOption() {
        while (true) {
            String input = INPUT.nextLine().trim();

            try {
                int option = Integer.parseInt(input);

                if (option >= 0 && option <= 9) {
                    return option;
                }
            } catch (NumberFormatException ignored) {
            }

            System.out.print(
                "Opcion invalida. Ingrese un numero del 0 al 9: "
            );
        }


    }

    private static void executeOption(int option) {
        switch (option) {
            case 1:
                registerAction();
                break;
            case 2:
                undoAction();
                break;
            case 3:
                viewLastAction();
                break;
            case 4:
                showHistory();
                break;
            case 5:
                addTask();
                break;
            case 6:
                processTask();
                break;
            case 7:
                viewNextTask();
                break;
            case 8:
                showPendingTasks();
                break;
            case 9:
                showSystemStatus();
                break;
            case 0:
                System.out.println(
                    "Gracias por utilizar el Centro de Operaciones."
                );
                break;
            default:
                break;
        }
    }

    private static void registerAction() {
        String action = readNonEmptyText(
            "Ingrese la accion realizada: "
        );

        ACTION_HISTORY.push(action);

        System.out.println(
            "Accion registrada correctamente: " + action
        );
    }

    private static void undoAction() {
        String action = ACTION_HISTORY.pop();

        if (action == null) {
            System.out.println(
                "No hay acciones para deshacer: la Stack esta vacia."
            );
        } else {
            System.out.println("Ultima accion: " + action);
            System.out.println("Accion deshecha correctamente.");

        }
    }

    private static void viewLastAction() {
        String action = ACTION_HISTORY.peek();

        if (action == null) {
            System.out.println(
                "No existe una ultima accion: la Stack esta vacia."
            );
        } else {
            System.out.println(
                "Ultima accion registrada: " + action
            );

        }
    }

    private static void showHistory() {
        System.out.println("HISTORIAL DE ACCIONES");
        ACTION_HISTORY.display();
    }

    private static void addTask() {
        String task = readNonEmptyText("Ingrese la tarea: ");
        PENDING_TASKS.enqueue(task);

        System.out.println(
            "Tarea agregada correctamente: " + task
        );
    }

    private static void processTask() {
        String task = PENDING_TASKS.dequeue();

        if (task == null) {
            System.out.println(
                "No hay tareas para procesar: la Queue esta vacia."
            );
        } else {
            System.out.println("Procesando: " + task);
            System.out.println("Tarea procesada correctamente.");
        }
    }

    private static void viewNextTask() {
        String task = PENDING_TASKS.peek();

        if (task == null) {
            System.out.println(
                "No existe una siguiente tarea: la Queue esta vacia."
            );
        } else {
            System.out.println("Siguiente tarea: " + task);
        }
    }

    private static void showPendingTasks() {
        System.out.println("TAREAS PENDIENTES");
        PENDING_TASKS.display();
    }

    private static void showSystemStatus() {
        System.out.println("ESTADO DEL SISTEMA");

        System.out.println(
            "Acciones en el historial: " + ACTION_HISTORY.size()
        );

        System.out.println(
            "Stack vacia: "
                + (ACTION_HISTORY.isEmpty() ? "Si" : "No")
        );

        System.out.println(
            "Tareas pendientes: " + PENDING_TASKS.size()
        );

        System.out.println(
            "Queue vacia: "
                + (PENDING_TASKS.isEmpty() ? "Si" : "No")
        );
        
    }

    private static String readNonEmptyText(String prompt) {
        while (true) {
            System.out.print(prompt);
            String text = INPUT.nextLine().trim();

            if (!text.isEmpty()) {
                return text;
            }

            System.out.println("El texto no puede estar vacio.");
        }
    }

    private static void pause() {
        System.out.print("\nPresione Enter para continuar...");
        INPUT.nextLine();


    }

}