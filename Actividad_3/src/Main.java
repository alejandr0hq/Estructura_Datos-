

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public final class Main {
    private static final int[] BLOQUES = {84729, 56318, 92746};
    private static final int[] LECTURAS = {
        418, 732, 156, 894, 327, 641, 285, 519,
        763, 204, 947, 386, 675, 128, 856, 493
    };

    private Main() {
    }

    public static void main(String[] args) throws IOException {
        String clave1 = "";
        for (int bloque : BLOQUES) {
            clave1 += Desafio1.reducirAUnDigito(bloque);
        }
        int clave2 = Desafio2.maximo(LECTURAS);

        System.out.println("CLAVE 1: " + clave1);
        System.out.println("CLAVE 2: " + clave2);
        System.out.println("CONTRASEÑA: " + clave1 + "-" + clave2);

        char[][] laberinto = args.length == 0 ? laberintoDemostracion() : leerLaberinto(args[0]);
        ResultadoLaberinto resultado = new SolucionadorLaberinto().resolver(laberinto);
        imprimir(resultado);
    }

    private static char[][] leerLaberinto(String archivo) throws IOException {
        List<String> lineas = Files.readAllLines(Path.of(archivo));
        if (lineas.isEmpty()) throw new IllegalArgumentException("El archivo está vacío");
        char[][] laberinto = new char[lineas.size()][];
        for (int i = 0; i < lineas.size(); i++) {
            laberinto[i] = lineas.get(i).toCharArray();
        }
        return laberinto;
    }

    private static char[][] laberintoDemostracion() {
        return new char[][] {
            "S....".toCharArray(),
            ".###.".toCharArray(),
            "E....".toCharArray()
        };
    }

    private static void imprimir(ResultadoLaberinto resultado) {
        System.out.println("\nPosiciones visitadas:");
        for (Posicion posicion : resultado.posicionesVisitadas()) {
            System.out.println(posicion);
        }

        System.out.println("\nRuta final más corta:");
        if (!resultado.tieneSolucion()) {
            System.out.println("RUTA:SIN_SOLUCION");

        } else 
            
            {
                System.out.print("RUTA:");
            for (int i = 0; i < resultado.ruta().size(); i++) {
                if (i > 0) System.out.print(";");
                System.out.print(resultado.ruta().get(i));

            }
            System.out.println();
            System.out.println("MOVIMIENTOS:" + resultado.movimientos());
        }
        System.out.printf("%nMETRICAS:retrocesos=%d;estados=%d%n",
                resultado.retrocesos(), resultado.estados());

    }
}
