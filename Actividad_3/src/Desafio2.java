
public final class Desafio2 {
    private Desafio2() {
    }

    public static int maximo(int[] datos) {
        if (datos == null || datos.length == 0) {
            throw new IllegalArgumentException("El arreglo no puede estar vacío");
        }
        return maximo(datos, 0, datos.length - 1);
    }

    private static int maximo(int[] datos, int inicio, int fin) {
        if (inicio == fin) {        
            return datos[inicio];
        }

        int medio = inicio + (fin - inicio) / 2;
        int maximoIzquierdo = maximo(datos, inicio, medio);
        int maximoDerecho = maximo(datos, medio + 1, fin);
        return Math.max(maximoIzquierdo, maximoDerecho); 
    }
}
