import java.util.ArrayList;
import java.util.List;


public final class SolucionadorLaberinto {

    private static final int[][] DIRECCIONES = {
        {0, 1},  // derecha
        {1, 0},  // Abajo
        {0, -1}, // izquierda
        {-1, 0}  // arriba
    };

    private char[][] laberinto;
    private boolean[][] enRutaActual;
    private final List<Posicion> rutaActual = new ArrayList<>();
    private final List<Posicion> visitadas = new ArrayList<>();
    private List<Posicion> mejorRuta = List.of();
    private Posicion salida;
    private int retrocesos;
    private int estados;

    public ResultadoLaberinto resolver(char[][] entrada) {
        Posicion inicio = validarYCopiar(entrada);
        enRutaActual = new boolean[laberinto.length][laberinto[0].length];
        rutaActual.clear();
        visitadas.clear();
        mejorRuta = List.of();
        retrocesos = 0;
        estados = 0;

        explorar(inicio.fila(), inicio.columna());
        return new ResultadoLaberinto(mejorRuta, visitadas, retrocesos, estados);
    }

    private void explorar(int fila, int columna) {

        if (!mejorRuta.isEmpty() && rutaActual.size() + 1 >= mejorRuta.size()) {
            return;
        }

        Posicion actual = new Posicion(fila, columna);
        estados++;
        visitadas.add(actual);
        rutaActual.add(actual);
        enRutaActual[fila][columna] = true;

        if (actual.equals(salida)) {
            mejorRuta = List.copyOf(rutaActual);
        } else {
            for (int[] direccion : DIRECCIONES) {
                int nuevaFila = fila + direccion[0];
                int nuevaColumna = columna + direccion[1];
                if (esMovimientoValido(nuevaFila, nuevaColumna)) {
                    explorar(nuevaFila, nuevaColumna);
                }
            }
        }


        enRutaActual[fila][columna] = false;
        rutaActual.remove(rutaActual.size() - 1);
        retrocesos++;
    }

    private boolean esMovimientoValido(int fila, int columna) {
        return fila >= 0 && fila < laberinto.length
                && columna >= 0 && columna < laberinto[0].length
                && laberinto[fila][columna] != '#'
                && !enRutaActual[fila][columna];
    }

    private Posicion validarYCopiar(char[][] entrada) {
        if (entrada == null || entrada.length == 0 || entrada[0] == null
                || entrada[0].length == 0) {
            throw new IllegalArgumentException("El laberinto no puede estar vacío");
        }

        int columnas = entrada[0].length;
        laberinto = new char[entrada.length][columnas];
        Posicion inicio = null;
        salida = null;

        for (int fila = 0; fila < entrada.length; fila++) {
            if (entrada[fila] == null || entrada[fila].length != columnas) {
                throw new IllegalArgumentException("El laberinto debe ser rectangular");
            }
            for (int columna = 0; columna < columnas; columna++) {
                char celda = entrada[fila][columna];
                if (celda != 'S' && celda != 'E' && celda != '.' && celda != '#') {
                    throw new IllegalArgumentException("Símbolo inválido: " + celda);
                }
                laberinto[fila][columna] = celda;
                if (celda == 'S') {
                    if (inicio != null) throw new IllegalArgumentException("Debe existir un solo S");
                    inicio = new Posicion(fila, columna);
                } else if (celda == 'E') {
                    if (salida != null) throw new IllegalArgumentException("Debe existir una sola E");
                    salida = new Posicion(fila, columna);
                }
            }
        }
        if (inicio == null || salida == null) {
            throw new IllegalArgumentException("El laberinto debe contener S y E");
        }
        return inicio;

    }

    
}
