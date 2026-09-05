
public final class Desafio1 {
    private Desafio1() {
    }

    public static int reducirAUnDigito(long numero) {
        if (numero > -10 && numero < 10) { 
            return (int) Math.abs(numero);
        }
        return reducirAUnDigito(sumarDigitos(numero)); 
    }

    private static long sumarDigitos(long numero) {
        if (numero > -10 && numero < 10) {   
            return Math.abs(numero);
        }
        return Math.abs(numero % 10) + sumarDigitos(numero / 10);
    }
}
