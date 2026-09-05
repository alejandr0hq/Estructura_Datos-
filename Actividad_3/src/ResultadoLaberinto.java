import java.util.List;

public record ResultadoLaberinto(
        List<Posicion> ruta,
        List<Posicion> posicionesVisitadas,
        int retrocesos,
        int estados) {

    public ResultadoLaberinto {
        ruta = List.copyOf(ruta);
        posicionesVisitadas = List.copyOf(posicionesVisitadas);
    }

    public boolean tieneSolucion() {
        return !ruta.isEmpty();
    }

    public int movimientos() {
        return tieneSolucion() ? ruta.size() - 1 : -1;
    }
}
