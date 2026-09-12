public class ArbolBinario {
    private Nodo raiz;

    public ArbolBinario() {
        raiz = null;
    }

    public boolean estaVacio() {
        return raiz == null;
    }

    public Nodo getRaiz() {
        return raiz;
    }

    // ===== FASE 1: INSERCIÓN =====
    public void insertar(int d) {
        // TODO: implementa la inserción en un ABB.
        // Sugerencia:
        //   raiz = insertarRec(raiz, d);
        raiz = insertarRec(raiz, d);
    }

    private Nodo insertarRec(Nodo n, int d) {
        // TODO: si n es null crea un Nodo(d) y devuélvelo.
        // Si d < n.getDato() enlaza por la izquierda, si es mayor por la derecha.
        if (n == null) {
            return new Nodo(d);
        }
        if (d < n.getDato()) {
            n.setIzq(insertarRec(n.getIzq(), d));
        } else if (d > n.getDato()) {
            n.setDer(insertarRec(n.getDer(), d));
        }
        return n;
    }

    // ===== FASE 2: RECORRIDOS =====
    public String inorden() {
        // TODO: recorre en inorden (izq, nodo, der) y devuelve los valores
        // separados por coma, ej: "1,3,5,9". Usa el helper inordenRec.
        StringBuilder sb = new StringBuilder();
        inordenRec(raiz, sb);
        return sb.toString();
    }

    private void inordenRec(Nodo n, StringBuilder sb) {
        // TODO: agrega un valor y una coma (si ya hay algo) y recursiona.
        if (n == null) {
            return;
        }
        inordenRec(n.getIzq(), sb);
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(n.getDato());
        inordenRec(n.getDer(), sb);
    }

    public String preorden() {
        // TODO: recorrido preorden (nodo, izq, der) separado por comas.
        StringBuilder sb = new StringBuilder();
        preordenRec(raiz, sb);
        return sb.toString();
    }

    private void preordenRec(Nodo n, StringBuilder sb) {
        // TODO
        if (n == null) {
            return;
        }
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(n.getDato());
        preordenRec(n.getIzq(), sb);
        preordenRec(n.getDer(), sb);
    }

    public String postorden() {
        // TODO: recorrido postorden (izq, der, nodo) separado por comas.
        StringBuilder sb = new StringBuilder();
        postordenRec(raiz, sb);
        return sb.toString();
    }

    private void postordenRec(Nodo n, StringBuilder sb) {
        // TODO
        if (n == null) {
            return;
        }
        postordenRec(n.getIzq(), sb);
        postordenRec(n.getDer(), sb);
        if (sb.length() > 0) {
            sb.append(",");
        }
        sb.append(n.getDato());
    }

    // ===== FASE 3: BÚSQUEDA =====
    public boolean buscar(int d) {
        // TODO: devuelve true si el valor existe en el árbol.
        return buscarRec(raiz, d);
    }

    private boolean buscarRec(Nodo n, int d) {
        // TODO: búsqueda binaria recursiva.
        if (n == null) {
            return false;
        }
        if (d == n.getDato()) {
            return true;
        }
        if (d < n.getDato()) {
            return buscarRec(n.getIzq(), d);
        }
        return buscarRec(n.getDer(), d);
    }

    public String claseNodo(int d) {
        // TODO: usa buscarNodo(raiz, d) para localizar el nodo que contiene d
        //   y devuelve su clasificación: "RAIZ", "HOJA", "UN_HIJO",
        //   "DOS_HIJOS" o "NO_EXISTE".
        Nodo n = buscarNodo(raiz, d);
        if (n == null) {
            return "NO_EXISTE";
        }
        if (n == raiz) {
            return "RAIZ";
        }
        if (n.getIzq() == null && n.getDer() == null) {
            return "HOJA";
        }
        if (n.getIzq() != null && n.getDer() != null) {
            return "DOS_HIJOS";
        }
        return "UN_HIJO";
    }

    private Nodo buscarNodo(Nodo n, int d) {
        // TODO: busca el nodo que contiene d (búsqueda binaria recursiva)
        //   y devuélvelo, o null si no existe.
        if (n == null || d == n.getDato()) {
            return n;
        }
        if (d < n.getDato()) {
            return buscarNodo(n.getIzq(), d);
        }
        return buscarNodo(n.getDer(), d);
    }

    // ===== FASE 4: ELIMINACIÓN =====
    public boolean eliminar(int d) {
        // TODO: elimina el valor d del árbol si existe.
        // Devuelve true si lo encontró y eliminó, false si no existía.
        // Uso: raiz = eliminarRec(raiz, d); return encontrado(o false).
        if (!buscar(d)) {
            return false;
        }
        raiz = eliminarRec(raiz, d);
        return true;
    }

    private Nodo eliminarRec(Nodo n, int d) {
        // TODO: los 3 casos:
        //   - HOJA: devuelve null.
        //   - UN hijo: devuelve ese hijo.
        //   - DOS hijos: reemplaza por el mínimo del subárbol derecho
        //     (sucesor inorden) y elimina ese sucesor.
        if (n == null) {
            return null;
        }
        if (d < n.getDato()) {
            n.setIzq(eliminarRec(n.getIzq(), d));
        } else if (d > n.getDato()) {
            n.setDer(eliminarRec(n.getDer(), d));
        } else {
            if (n.getIzq() == null && n.getDer() == null) {
                return null;
            }
            if (n.getIzq() == null) {
                return n.getDer();
            }
            if (n.getDer() == null) {
                return n.getIzq();
            }
            Nodo sucesor = minimo(n.getDer());
            n.setDato(sucesor.getDato());
            n.setDer(eliminarRec(n.getDer(), sucesor.getDato()));
        }
        return n;
    }

    private Nodo minimo(Nodo n) {
        // TODO: devuelve el nodo con el valor más pequeño del subárbol.
        if (n == null) {
            return null;
        }
        while (n.getIzq() != null) {
            n = n.getIzq();
        }
        return n;
    }
}
