public class Main {
    public static void main(String[] args) {
        int[] secuencia = {
            89, 38, 11, 98, 17, 84, 83, 21, 94, 20, 82,
            96, 39, 70, 73, 71, 5, 14, 16, 4, 48, 64
        };
        ArbolBinario arbol = new ArbolBinario();
        System.out.println("MATRICULA=al07184441");
        System.out.println("RETO=V5FEPBK");
        System.out.println("VACIO_INICIAL=" + arbol.estaVacio());
        for (int dato : secuencia) {
            arbol.insertar(dato);
        }
        System.out.println("VACIO_TRAS_INSERTAR=" + arbol.estaVacio());
        System.out.println("INORDEN=" + arbol.inorden());
        System.out.println("PREORDEN=" + arbol.preorden());
        System.out.println("POSTORDEN=" + arbol.postorden());
        for (int dato : new int[] {89, 4, 14, 38, 100}) {
            System.out.println("BUSCAR:" + dato + "=" + arbol.buscar(dato));
            System.out.println("CLASE:" + dato + "=" + arbol.claseNodo(dato));
        }
        for (int dato : new int[] {4, 14, 38, 89}) {
            System.out.println("ELIMINADO:" + dato + "=" + arbol.eliminar(dato));
            System.out.println("PREORDEN=" + arbol.preorden());
            System.out.println("BUSCAR:" + dato + "=" + arbol.buscar(dato));
        }
        System.out.println("RAIZ_FINAL=" + arbol.getRaiz().getDato());
        System.out.println("INORDEN_FINAL=" + arbol.inorden());
        System.out.println("ELIMINADO:100=" + arbol.eliminar(100));
    }
}
