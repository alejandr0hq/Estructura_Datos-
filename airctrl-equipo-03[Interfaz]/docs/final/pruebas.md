# Pruebas automatizadas

## Ejecucion

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src tests -name "*.java")
java -cp bin tests.AllTests
```

La suite imprime para cada caso su resultado esperado y obtenido. Una diferencia incrementa el contador de fallos y termina la ejecucion con error.

## Cobertura

| Grupo | Casos |
|---|---|
| Estructuras | Lista, cola, cola vacia, pila, prioridades, desempate y tabla hash |
| Datos | Cantidades validas y doce inconsistencias controladas |
| Busqueda | Identificador, aerolinea, estado y equipaje inexistente |
| Registro | Vuelo normal, emergencia, duplicado, aeronave inexistente, equipaje e incidente |
| Puertas | Rechazo de ocupada y asignacion de disponible |
| Operaciones | Procesamiento, historial y cola vacia |
| Grafo | Dijkstra, BFS, ubicacion desconocida y componentes desconectados |

## Resultado obtenido

```text
TOTAL: 34 | PASSED: 34 | FAILED: 0
```

Entre los resultados verificados se encuentran:

```text
PASS | Priority five is served first | expected=EMERGENCY | obtained=EMERGENCY
PASS | Occupied gate is rejected | expected=AirCtrlException | obtained=AirCtrlException
PASS | Dijkstra finds the shortest route | expected=G19>G06>BELT02:1304 | obtained=G19>G06>BELT02:1304
PASS | CSV validation reports inconsistencies | expected=12 | obtained=12
```

## Casos limite

- Estructura vacia.
- Prioridad fuera de rango.
- Colision de hash.
- Vuelo duplicado.
- Aeronave inexistente.
- Puerta ocupada.
- Equipaje asociado a vuelo inexistente.
- Operacion pendiente inexistente.
- Ubicacion desconocida.
- Ruta entre componentes desconectados.
