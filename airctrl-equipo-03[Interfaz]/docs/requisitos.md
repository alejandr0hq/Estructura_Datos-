# Requisitos funcionales

El sistema debe permitir:

| # | Requisito | Descripción |
|---|---|---|
| 1 | Registrar vuelos | Cargar el itinerario desde `flights.csv` y dar de alta vuelos nuevos |
| 2 | Consultar vuelos | Listar vuelos con su estado, puerta y prioridad |
| 3 | Buscar vuelos | Encontrar un vuelo por su `flight_id`, aerolínea o estado |
| 4 | Registrar aeronaves | Cargar y dar de alta aeronaves desde `aircraft.csv` |
| 5 | Asignar puertas | Vincular un vuelo a una puerta validando su estado |
| 6 | Registrar equipaje | Cargar y dar de alta equipaje asociado a vuelos |
| 7 | Registrar incidencias | Documentar retrasos, cambios de puerta, problemas de equipaje, cierres |
| 8 | Procesar operaciones | Atender la siguiente operación pendiente y actualizar estados |
| 9 | Priorizar situaciones | Determinar el orden de atención según las reglas de prioridad |
| 10 | Consultar historial | Revisar operaciones ya procesadas sin mezclarlas con lo pendiente |
| 11 | Analizar conexiones | Recorrer las conexiones internas del aeropuerto desde cualquier punto |
| 12 | Generar reportes | Vuelos por estado, uso de puertas, equipaje por estado, incidencias por tipo |

## Casos límite a contemplar

1. Vuelo inexistente (búsqueda de `flight_id` desconocido).
2. Aeronave inexistente (referencia rota desde un vuelo).
3. Puerta inexistente.
4. Equipaje inexistente (búsqueda de `bag_id` desconocido).
5. Cola vacía (intentar procesar cuando no hay operaciones pendientes).
6. Vuelo duplicado (registrar dos veces el mismo `flight_id`).
7. Asignación a puerta no disponible (`OCCUPIED`, `MAINTENANCE`, `CLOSED`).
8. Dos situaciones con la misma prioridad (criterio de desempate).
9. Equipaje sin vuelo válido (referencia rota).

En todos los casos: mensaje claro al usuario, **nunca** un error no controlado.
