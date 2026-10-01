# Datos — carpeta `data/`

Este directorio contiene los conjuntos de datos de trabajo del proyecto AIRCTRL.

## Archivos

| Archivo | Contenido | Registros aprox. |
|---|---|---|
| `flights.csv` | Itinerario del día con estados y prioridades | ~50 |
| `aircraft.csv` | Flota de aeronaves | ~35 |
| `gates.csv` | Puertas por terminal con estados | ~45 |
| `baggage.csv` | Equipaje asociado a vuelos | ~140 |
| `connections.csv` | Conexiones internas del aeropuerto | ~35 |

## Formato

- Codificación: UTF-8
- Separador: coma (`,`), sin comillas
- Primera fila: encabezados (no la elimines)
- Saltos de línea: `\n` (Unix)

## Descripción detallada de campos

Ver [`docs/datos.md`](../docs/datos.md) para el diccionario completo de cada archivo, ejemplos y observaciones.

## Advertencias útiles

1. **Hay puertas OCCUPIED, MAINTENANCE y CLOSED a propósito**: las reglas de asignación deben validarlas todas.
2. **Hay equipajes DELIVERED y MISSING a propósito**: los primeros no deben volver a aparecer como pendientes; los segundos sí requieren seguimiento.
3. **El campo `current_flight` de una puerta libre está vacío**: tu código debe tolerar campos vacíos.
4. No edites estos archivos para "arreglarlos": parte del ejercicio es manejar datos reales imperfectos.

## Agregar tus propios datos

Puedes generar registros adicionales para pruebas, pero conserva siempre estos archivos originales: los casos de prueba oficiales asumen su contenido.
