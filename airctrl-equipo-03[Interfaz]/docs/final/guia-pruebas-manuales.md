# Guia de pruebas manuales

## Preparacion

Desde la raiz del proyecto:

```bash
mkdir -p bin
javac -encoding UTF-8 -d bin $(find src tests -name "*.java")
java -cp bin tests.AllTests
```

El resultado debe terminar con:

```text
TOTAL: 34 | PASSED: 34 | FAILED: 0
```

Iniciar el menu:

```bash
java -cp bin Main
```

## Caso 1: resumen y validaciones

1. Elegir `1`.
2. Confirmar 50 vuelos, 33 aeronaves validas, 45 puertas, 140 equipajes, 35 conexiones, 39 ubicaciones, 29 rutas validas, 26 operaciones pendientes y 12 advertencias.
3. Elegir `18` para revisar el detalle de las advertencias.

## Caso 2: consultas

1. Elegir `5` e ingresar `AM101`.
2. Confirmar que aparece el vuelo con su estado, puerta y prioridad.
3. Elegir `6` e ingresar `AirMex`.
4. Confirmar que todos los resultados pertenecen a esa aerolinea.
5. Elegir `7` e ingresar `DELAYED`.
6. Confirmar que todos los resultados tienen ese estado.
7. Elegir `8` e ingresar `BAG999`.
8. Confirmar que se muestra un mensaje controlado de equipaje no encontrado.

## Caso 3: prioridad e historial

1. Elegir `3`.
2. Confirmar que `XA901`, prioridad 5, aparece primero.
3. Elegir `4` para procesar la siguiente operacion.
4. Elegir `14` y confirmar que `XA901` aparece en el historial.

## Caso 4: puertas

1. Elegir `9`, ingresar vuelo `AM101` y puerta `G03`.
2. Confirmar que la operacion se rechaza porque `G03` esta ocupada.
3. Elegir `9`, ingresar vuelo `AM101` y puerta `G01`.
4. Confirmar que la puerta disponible se asigna correctamente.

## Caso 5: nuevos registros

1. Elegir `11` y registrar `AC900`, modelo `Test Model`, capacidad `120`, estado `ACTIVE`.
2. Elegir `10` y registrar `TF900`, aerolinea `TestAir`, origen `MEX`, destino `TIJ`, aeronave `AC900`, puerta vacia, estado `SCHEDULED`, prioridad `2`.
3. Elegir `12` y registrar `BAG900`, vuelo `TF900`, pasajero `PAX900`, estado `CHECKED`.
4. Elegir `13` y registrar `INC900`, tipo `WEATHER`, descripcion `Heavy rain`, prioridad `3`.
5. Elegir `17` y confirmar que `WEATHER: 1` aparece en incidentes.

## Caso 6: grafo

1. Elegir `15`, origen `G19` y destino `BELT02`.
2. Confirmar la ruta `G19 -> G06 -> BELT02` con distancia total de `1304 m`.
3. Elegir `16` e ingresar `BAGROOM-1`.
4. Confirmar que el recorrido BFS incluye `BAGROOM-1` y `BELT03`, aunque su conexion no tenga distancia.
5. Elegir `15` e ingresar una ubicacion desconocida.
6. Confirmar que el sistema muestra un mensaje controlado.

## Cierre

Elegir `0`. Los registros agregados durante la sesion viven en memoria y se reinician al ejecutar nuevamente el programa.
