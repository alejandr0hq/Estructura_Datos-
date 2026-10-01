# Analisis de datos

## Archivos recibidos

### aircraft.csv

Contiene 35 aeronaves. Sus campos son `aircraft_id`, `model`, `capacity` y `status`. El identificador de aeronave se relaciona con `aircraft_id` de los vuelos.

### flights.csv

Contiene 50 vuelos. Sus campos son `flight_id`, `airline`, `origin`, `destination`, `aircraft_id`, `gate`, `status` y `priority`. Es el archivo central porque se relaciona con aeronaves, puertas y equipajes.

### gates.csv

Contiene 45 puertas. Sus campos son `gate_id`, `terminal`, `status` y `current_flight`. Una puerta puede contener el identificador del vuelo que la ocupa.

### baggage.csv

Contiene 140 equipajes. Sus campos son `bag_id`, `flight_id`, `passenger_code` y `status`. Cada equipaje debe referirse a un vuelo existente.

### connections.csv

Contiene 35 conexiones. Sus campos son `source_location`, `target_location` y `distance_m`. Representa enlaces entre ubicaciones y puede contener distancias vacias que deben reportarse como advertencias.

## Validaciones aplicadas

- El identificador de cada vuelo no debe repetirse.
- La prioridad de un vuelo debe ser un numero entre 1 y 5.
- La capacidad de una aeronave debe ser numerica.
- Las aeronaves y puertas mencionadas por los vuelos deben existir.
- Los vuelos mencionados por equipajes y puertas deben existir.
- Una distancia vacia o no numerica genera una advertencia.

Los registros validos se conservan aunque otros registros generen advertencias. Esto permite mostrar el estado de los datos sin detener toda la carga por una inconsistencia aislada.
