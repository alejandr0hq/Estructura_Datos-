# Diseno inicial

## Capas

- `model`: representa vuelos, aeronaves, puertas, equipajes, conexiones y operaciones.
- `structure`: contiene la lista enlazada, cola, pila y cola de prioridades.
- `data`: lee los archivos CSV, almacena los registros y valida referencias.
- `service`: aplica las reglas de consulta, prioridad, procesamiento y asignacion de puertas.
- `ui`: presenta el menu de consola y recibe las opciones del operador.
- `Main.java`: carga los datos, crea el sistema e inicia la interfaz.

## Relaciones principales

- Un vuelo referencia una aeronave y puede tener una puerta.
- Un equipaje pertenece a un vuelo.
- Una puerta puede estar disponible, fuera de servicio u ocupada por un vuelo.
- Una conexion une dos ubicaciones del aeropuerto.
- Una operacion contiene un vuelo, su prioridad y su orden de llegada.

## Flujo implementado

1. `Main` solicita la carga de la carpeta `data`.
2. `CsvRepository` transforma cada fila en una entidad.
3. `AirCtrlSystem` registra los vuelos pendientes en la cola de prioridades.
4. `ConsoleInterface` muestra las operaciones en orden de atencion.
5. Al procesar una operacion, el servicio la retira de pendientes y la agrega al historial.
