# Analisis del problema

## Contexto

Un centro de operaciones aeroportuarias recibe informacion de vuelos, aeronaves, puertas, equipajes, incidentes y conexiones internas. Estos eventos no tienen la misma urgencia y pueden ocurrir al mismo tiempo. El sistema debe conservar la informacion, validar sus relaciones y determinar un orden de atencion consistente.

AIRCTRL resuelve este problema mediante una aplicacion de consola que carga los datos proporcionados y permite consultar, registrar, priorizar y procesar operaciones. Tambien representa las conexiones internas como un grafo para recorrer ubicaciones y calcular rutas de menor distancia.

## Alcance

El sistema incluye:

- Carga de todos los archivos CSV entregados.
- Validacion de identificadores, valores numericos y referencias cruzadas.
- Consulta y registro de vuelos, aeronaves, equipajes e incidentes.
- Asignacion de puertas disponibles.
- Priorizacion conjunta de vuelos e incidentes pendientes.
- Historial de operaciones procesadas.
- Reportes agrupados por estado o tipo.
- Recorrido y ruta minima dentro del mapa de conexiones.
- Pruebas automatizadas normales y limite.

Quedan fuera del alcance las reservaciones, venta de boletos, autenticacion de pasajeros, pagos, control aereo en tiempo real y persistencia de cambios posteriores al cierre del programa.

## Usuarios

### Operador del centro de control

Consulta vuelos, revisa pendientes, procesa operaciones, asigna puertas y registra incidentes.

### Coordinador de puertas

Verifica disponibilidad y evita asignaciones sobre puertas ocupadas, cerradas o en mantenimiento.

### Personal de equipaje

Consulta y registra equipajes vinculados a vuelos, con atencion especial a los estados pendientes y extraviados.

### Supervisor operativo

Revisa resumenes, advertencias, historial, reportes y rutas internas para tomar decisiones.

## Reglas de negocio

- La prioridad valida se encuentra entre 1 y 5.
- El valor 5 representa la urgencia mas alta.
- La prioridad se atiende antes que el orden de llegada.
- Dos operaciones con la misma prioridad conservan el orden FIFO.
- Los vuelos `DEPARTED`, `LANDED` o `CANCELLED` no entran a pendientes.
- Los identificadores de vuelos, aeronaves, equipajes e incidentes no se duplican.
- Un vuelo nuevo debe referir a una aeronave existente.
- Un equipaje nuevo debe referir a un vuelo existente.
- Solo una puerta `AVAILABLE` puede recibir un vuelo.
- Al reasignar una puerta se libera la anterior cuando pertenece al mismo vuelo.
- Los errores de usuario y datos se comunican con mensajes controlados.
- Las conexiones sin distancia se conservan para el analisis, pero no forman aristas validas del grafo.

## Flujo principal

1. `Main` solicita la carga de la carpeta `data`.
2. `CsvRepository` valida encabezados, campos numericos, duplicados y referencias.
3. `DataStore` conserva las entidades validas y las advertencias.
4. `AirCtrlSystem` crea los indices, las operaciones pendientes y el grafo.
5. `ConsoleInterface` presenta las funciones disponibles.
6. El operador consulta, registra, procesa, asigna, recorre o genera reportes.
7. Los casos invalidos se rechazan sin finalizar inesperadamente el programa.
