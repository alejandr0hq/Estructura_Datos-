public final class Pruebas {
    private static int ejecutadas;

    private Pruebas() {
    }

    public static void main(String[] args) {
        probarDesafio1();
        probarDesafio2();
        probarRutaDirecta();
        probarCallejonSinSalida();
        probarVariasRutas();
        probarPrimeraRutaNoEsLaMasCorta();
        probarSinSolucion();
        System.out.println("PRUEBAS SUPERADAS: " + ejecutadas);
    }

    private static void probarDesafio1() {
        verificar(Desafio1.reducirAUnDigito(84729) == 3, "bloque 1");
        verificar(Desafio1.reducirAUnDigito(56318) == 5, "bloque 2");
        verificar(Desafio1.reducirAUnDigito(92746) == 1, "bloque 3");
        verificar(Desafio1.reducirAUnDigito(0) == 0, "caso base cero");
        verificar(Desafio1.reducirAUnDigito(-12345) == 6, "entero negativo");
        verificar(Desafio1.reducirAUnDigito(Long.MIN_VALUE) == 8, "long mínimo");
        System.out.println("OK - Desafío 1: clave 351 y casos especiales");
    }

    private static void probarDesafio2() {
        int[] lecturas = {418, 732, 156, 894, 327, 641, 285, 519,
                763, 204, 947, 386, 675, 128, 856, 493};
        verificar(Desafio2.maximo(lecturas) == 947, "clave 2");
        verificar(Desafio2.maximo(new int[] {-8}) == -8, "un elemento");
        verificar(Desafio2.maximo(new int[] {-9, -2, -20}) == -2, "negativos");
        System.out.println("OK - Desafío 2: clave 947 y casos especiales");
    }

    private static void probarRutaDirecta() {
        ResultadoLaberinto r = resolver("S..E");
        verificar(r.tieneSolucion() && r.movimientos() == 3, "ruta directa");
        System.out.println("OK - Caso 1, ruta directa: 3 movimientos");
    }

    private static void probarCallejonSinSalida() {
        ResultadoLaberinto r = resolver("S..#", ".#.#", "...E");
        verificar(r.tieneSolucion() && r.movimientos() == 5, "callejón");
        verificar(r.retrocesos() > r.ruta().size(), "debe efectuar retrocesos");
        System.out.println("OK - Caso 2, callejón: retrocede y encuentra 5 movimientos");
    }

    private static void probarVariasRutas() {
        ResultadoLaberinto r = resolver("S..", ".#.", "..E");
        verificar(r.tieneSolucion() && r.movimientos() == 4, "varias rutas");
        System.out.println("OK - Caso 3, varias rutas: el mínimo es 4 movimientos");
    }

    private static void probarPrimeraRutaNoEsLaMasCorta() {
        // Derecha se explora primero: produce 10 movimientos. Después se halla la ruta de 2.
        ResultadoLaberinto r = resolver("S....", ".###.", "E....");
        verificar(r.tieneSolucion() && r.movimientos() == 2,
                "la primera ruta no es la más corta");
        verificar(r.estados() > r.ruta().size(), "debe explorar más que la ruta final");
        System.out.println("OK - Caso 4, primera ruta=10; ruta final=2 movimientos");
    }

    private static void probarSinSolucion() {
        ResultadoLaberinto r = resolver("S#E", ".##", "...");
        verificar(!r.tieneSolucion() && r.movimientos() == -1, "sin solución");
        System.out.println("OK - Caso 5, sin solución detectado correctamente");
    }

    private static ResultadoLaberinto resolver(String... filas) {
        char[][] laberinto = new char[filas.length][];
        for (int i = 0; i < filas.length; i++) laberinto[i] = filas[i].toCharArray();
        return new SolucionadorLaberinto().resolver(laberinto);
    }

    private static void verificar(boolean condicion, String mensaje) {
        ejecutadas++;
        if (!condicion) throw new AssertionError("Falló: " + mensaje);
    }
}
