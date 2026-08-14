import java.util.Scanner;

public class Main {
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        
        LinkedList library = new LinkedList();
        int option;

        do {
            showMenu();
            option = readInt("Seleccione una opción: ");

            switch (option) {
                case 1:
                    library.insertAtBeginning(readBook());
                    System.out.println("Libro agregado al inicio.");
                    break;

                case 2:
                    library.insertAtEnd(readBook());
                    System.out.println("Libro agregado al final.");
                    break;

                case 3:
                    Book newBook = readBook();
                    int insertPosition =
                            readInt("Posición donde desea insertarlo: ");

                    if (library.insertAtPosition(
                            newBook, insertPosition)) {
                        System.out.println("Libro insertado.");
                    } else {
                        System.out.println("Posición inválida.");
                    }
                    break;

                case 4:
                    System.out.println("\nLIBROS REGISTRADOS");
                    library.display();
                    break;

                case 5:
                    String searchCode =
                            readText("Código del libro: ");

                    Book foundBook =
                            library.searchByCode(searchCode);

                    if (foundBook != null) {
                        System.out.println("Libro encontrado:");
                        System.out.println(foundBook);
                    } else {
                        System.out.println("El libro no existe.");
                    }
                    break;

                case 6:
                    int consultPosition =
                            readInt("Posición que desea consultar: ");

                    Book consultedBook =
                            library.getByPosition(consultPosition);

                    if (consultedBook != null) {
                        System.out.println(consultedBook);
                    } else {
                        System.out.println("Posición inválida.");
                    }
                    break;

                case 7:
                    String deleteCode =
                            readText("Código del libro a eliminar: ");

                    if (library.deleteByCode(deleteCode)) {
                        System.out.println("Libro eliminado.");
                    } else {
                        System.out.println("El libro no existe.");
                    }
                    break;

                case 8:
                    int deletePosition =
                            readInt("Posición que desea eliminar: ");

                    if (library.deleteByPosition(deletePosition)) {
                        System.out.println("Libro eliminado.");
                    } else {
                        System.out.println("Posición inválida.");
                    }
                    break;

                case 9:
                    System.out.println(
                            "Cantidad de libros: "
                                    + library.getSize()
                    );
                    break;

                case 10:
                    System.out.println("Programa finalizado.");
                    break;

                default:
                    System.out.println("Opción no válida.");
            }

        } while (option != 10);
    }

    private static void showMenu() {
        System.out.println("\n===== BIBLIOTECA =====");
        System.out.println("1. Agregar libro al inicio");
        System.out.println("2. Agregar libro al final");
        System.out.println("3. Insertar libro en una posición");
        System.out.println("4. Mostrar libros");
        System.out.println("5. Buscar libro");
        System.out.println("6. Consultar libro por posición");
        System.out.println("7. Eliminar libro por código");
        System.out.println("8. Eliminar libro por posición");
        System.out.println("9. Mostrar cantidad de libros");
        System.out.println("10. Salir");
    }

    private static Book readBook() {
        System.out.println("\nDATOS DEL LIBRO");

        String title = readText("Título: ");
        String author = readText("Autor: ");
        String code = readText("Código: ");

        return new Book(title, author, code);
    }

    private static String readText(String message) {
        String text;

        do {
            System.out.print(message);
            text = scanner.nextLine().trim();

            if (text.isEmpty()) {
                System.out.println(
                        "El dato no puede estar vacío."
                );
            }
        } while (text.isEmpty());

        return text;
    }

    private static int readInt(String message) {
        while (true) {
            try {
                System.out.print(message);
                return Integer.parseInt(
                        scanner.nextLine().trim()
                );
            } catch (NumberFormatException exception) {
                System.out.println(
                        "Ingrese un número entero válido."
                );

            }


        }


    }



}