# Analisis de datos

## flights.csv

Contiene 50 vuelos.

| Campo | Descripcion | Validacion |
|---|---|---|
| `flight_id` | Identificador unico | Obligatorio y no duplicado |
| `airline` | Aerolinea | Se utiliza en busquedas |
| `origin` | Aeropuerto de origen | Texto |
| `destination` | Aeropuerto de destino | Texto |
| `aircraft_id` | Aeronave asignada | Debe existir en `aircraft.csv` |
| `gate` | Puerta asignada | Puede estar vacia; si existe debe aparecer en `gates.csv` |
| `status` | Estado del vuelo | Determina si queda pendiente |
| `priority` | Urgencia | Entero entre 1 y 5 |

## aircraft.csv

Contiene 35 filas recibidas y 33 aeronaves validas. `AC018` y `AC019` tienen capacidad `0`, por lo que se reportan y no se incorporan como aeronaves validas.

| Campo | Descripcion | Validacion |
|---|---|---|
| `aircraft_id` | Identificador unico | Obligatorio y no duplicado |
| `model` | Modelo | Texto |
| `capacity` | Capacidad | Entero mayor que cero |
| `status` | Estado operativo | Texto normalizado por los datos |

## gates.csv

Contiene 45 puertas.

| Campo | Descripcion | Validacion |
|---|---|---|
| `gate_id` | Identificador unico | Obligatorio y no duplicado |
| `terminal` | Terminal | Texto |
| `status` | Disponibilidad | `AVAILABLE`, `OCCUPIED`, `MAINTENANCE` o `CLOSED` |
| `current_flight` | Vuelo actual | Debe existir cuando contiene un valor |

## baggage.csv

Contiene 140 equipajes.

| Campo | Descripcion | Validacion |
|---|---|---|
| `bag_id` | Identificador unico | Obligatorio y no duplicado |
| `flight_id` | Vuelo relacionado | Debe existir en `flights.csv` |
| `passenger_code` | Codigo anonimo | Texto |
| `status` | Estado del equipaje | `DELIVERED` finaliza el flujo pendiente |

## connections.csv

Contiene 35 conexiones recibidas. Seis filas no tienen distancia y se reportan como advertencias. El recorrido BFS conserva las 35 conexiones y sus 39 ubicaciones. Dijkstra utiliza solamente las 29 aristas con distancia positiva.

| Campo | Descripcion | Validacion |
|---|---|---|
| `source_location` | Ubicacion inicial | No vacia |
| `target_location` | Ubicacion final | No vacia |
| `distance_m` | Distancia en metros | Entero mayor que cero para entrar al grafo |

## Relaciones

```text
flights.aircraft_id  -> aircraft.aircraft_id
flights.gate         -> gates.gate_id
gates.current_flight -> flights.flight_id
baggage.flight_id    -> flights.flight_id
connections          -> vertices y aristas del grafo interno
```

## Resultado de validacion

La carga genera 12 advertencias controladas:

- Dos capacidades invalidas.
- Seis distancias vacias.
- Cuatro vuelos que referencian las dos aeronaves invalidas.

Los problemas se conservan para consulta y no provocan el cierre del programa.
