# Decisiones tecnicas

## Lista simplemente enlazada

Los vuelos se conservan en una lista propia porque el conjunto puede crecer durante la ejecucion. La referencia al ultimo nodo permite agregar en tiempo constante. Recorrer o buscar por un criterio requiere tiempo lineal.

## Cola enlazada

Cada nivel de prioridad utiliza una cola FIFO. Insertar al final, consultar el frente y retirar el frente tienen costo constante. Esto conserva el orden de llegada dentro de la misma prioridad.

## Cola de prioridad

Se mantienen cinco colas, una por prioridad, compartidas por vuelos e incidentes. Como la cantidad de niveles es fija, encontrar la prioridad disponible mas alta tiene costo constante respecto al volumen de operaciones. Una emergencia puede adelantarse sin alterar el orden interno de su nivel.

## Pila enlazada

El historial usa una pila porque la ultima operacion procesada debe mostrarse primero. Insertar y consultar el elemento superior tienen costo constante.

## Tabla hash propia

El indice de vuelos usa `HashTable`, implementada con arreglo de cubetas, encadenamiento para colisiones y redimensionamiento. La busqueda por identificador tiene costo promedio constante y costo lineal en el peor caso.

## Mapas y TreeMap

Los registros por identificador usan mapas para evitar recorridos completos. Los reportes utilizan `TreeMap`, basado en un arbol balanceado, para entregar categorias ordenadas alfabeticamente con inserciones de costo logaritmico.

## Grafo ponderado

Las ubicaciones son vertices y las conexiones son aristas no dirigidas. La lista de adyacencia conserva las 35 conexiones para los recorridos. Las 29 aristas con peso positivo tambien participan en rutas minimas. El espacio utilizado es proporcional a `V + E`, adecuado para un mapa disperso como el proporcionado.

## BFS

El recorrido en anchura permite conocer todas las ubicaciones alcanzables desde un punto. Usa una cola y tiene complejidad `O(V + E)`.

## Dijkstra

La ruta minima utiliza Dijkstra porque las distancias son positivas. La cola de prioridad de Java selecciona la ubicacion con menor distancia acumulada. La complejidad es `O((V + E) log V)`.

## Validacion sin detener la carga

Los errores estructurales del archivo, como ausencia de una columna, impiden continuar porque no existe forma segura de interpretar los datos. Los errores aislados de una fila se agregan a advertencias para permitir que el resto del sistema funcione.

## Tabla de uso

| Necesidad | Estructura o algoritmo | Motivo |
|---|---|---|
| Conservar vuelos | Lista enlazada | Crecimiento e insercion final |
| Orden por llegada | Cola | Comportamiento FIFO |
| Historial reciente | Pila | Ultimo procesado primero |
| Atencion urgente | Cola de prioridad | Prioridad mayor y desempate FIFO |
| Buscar vuelo por ID | Tabla hash | Acceso promedio constante |
| Ordenar categorias | TreeMap | Arbol balanceado y orden natural |
| Conectar ubicaciones | Grafo | Modela relaciones y distancias |
| Recorrer ubicaciones | BFS | Visita un componente completo |
| Calcular ruta minima | Dijkstra | Distancias positivas |
