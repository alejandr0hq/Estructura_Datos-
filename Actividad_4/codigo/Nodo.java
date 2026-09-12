public class Nodo {
    private int dato;
    private Nodo izq;
    private Nodo der;

    public Nodo(int d) {
        dato = d;
        izq = null;
        der = null;
    }

    public int getDato() {
        return dato;
    }

    public void setDato(int d) {
        dato = d;
    }

    public Nodo getIzq() {
        return izq;
    }

    public void setIzq(Nodo n) {
        izq = n;
    }

    public Nodo getDer() {
        return der;
    }

    public void setDer(Nodo n) {
        der = n;
    }
}
