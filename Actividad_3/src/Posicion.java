
public record Posicion(int fila, int columna) {
    @Override
    public String toString() {
        return fila + "," + columna;
    }
}
